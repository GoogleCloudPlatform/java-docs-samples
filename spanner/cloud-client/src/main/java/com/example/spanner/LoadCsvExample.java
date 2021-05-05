/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.spanner;

// Imports the Google Cloud client library
import com.google.cloud.spanner.Mutation;
import com.google.cloud.spanner.Mutation.WriteBuilder;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerOptions;
import com.google.cloud.spanner.jdbc.CloudSpannerJdbcConnection;
import java.io.FileReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/** Sample showing how to load CSV file data into Spanner */
public class LoadCsvExample {

  enum SpannerDataTypes {
    INT64,
    FLOAT64,
    BOOL,
    DATE,
    TIMESTAMP,
    STRING
  }

  public static ArrayList<SpannerDataTypes> spannerSchema = new ArrayList<>();

  public static SpannerDataTypes parseSpannerDataType(String columnType) {
    if (columnType.matches("STRING(?:\\((?:MAX|[0-9]+)\\))?")) {
      return SpannerDataTypes.STRING;
    } else if (columnType.equalsIgnoreCase("FLOAT64")) {
      return SpannerDataTypes.FLOAT64;
    } else if (columnType.equalsIgnoreCase("INT64")) {
      return SpannerDataTypes.INT64;
    } else if (columnType.equalsIgnoreCase("BOOL")) {
      return SpannerDataTypes.BOOL;
    } else if (columnType.equalsIgnoreCase("DATE")) {
      return SpannerDataTypes.DATE;
    } else if (columnType.equalsIgnoreCase("TIMESTAMP")) {
      return SpannerDataTypes.TIMESTAMP;
    } else {
      throw new IllegalArgumentException(
          "Unrecognized or unsupported column data type: " + columnType);
    }
  }

  public static CSVFormat setFormat(CommandLine cmd) {
    CSVFormat parseFormat;
    if (cmd.hasOption("f")) {
      switch (cmd.getOptionValue("f").toUpperCase()) {
        case ("EXCEL"):
          parseFormat = CSVFormat.EXCEL;
          break;
        case ("POSTGRESQL_CSV"):
          parseFormat = CSVFormat.POSTGRESQL_CSV;
          break;
        case("POSTGRESQL_TEXT"):
          parseFormat = CSVFormat.POSTGRESQL_TEXT;
          break;
        default:
          parseFormat = CSVFormat.DEFAULT;
      }
    } else {
      parseFormat = CSVFormat.DEFAULT;
    }

    if (cmd.hasOption("n")) {
      parseFormat = parseFormat.withNullString(cmd.getOptionValue("n"));
    }

    if (cmd.hasOption("d")) {
      if (cmd.getOptionValue("d").length() != 1) {
        throw new IllegalArgumentException("Invalid delimiter character entered.");
      }
      parseFormat = parseFormat.withDelimiter(cmd.getOptionValue("d").charAt(0));
    }

    if (cmd.hasOption("e")) {
      if (cmd.getOptionValue("e").length() != 1) {
        throw new IllegalArgumentException("Invalid escape character entered.");
      }
      parseFormat = parseFormat.withEscape(cmd.getOptionValue("e").charAt(0));
    }

    if (cmd.hasOption("h") &&  cmd.getOptionValue("h").equalsIgnoreCase("True")) {
      parseFormat = parseFormat.withFirstRecordAsHeader();
    }

    return parseFormat;
  }

  public static void main(String... args) throws Exception {

    // Set command line option flags
    Options opt = new Options();
    opt.addOption("h", true, "File Contains Header");
    opt.addOption("f", true, "Format Type of Input File");
    opt.addOption("n", true, "String Representing Null Value");
    opt.addOption("d", true, "Character Separating Columns");
    opt.addOption("e", true, "Character To Escape");

    if (args.length < 4) {
      System.err.println("LoadCSVExample <instance_id> <database_id> <table_id> <path_to_csv>");
      return;
    }

    // Instantiates a client
    SpannerOptions options = SpannerOptions.newBuilder().build();
    Spanner spanner = options.getService();
    String projectId = options.getProjectId();
    String instanceId = args[0];
    String databaseId = args[1];
    String tableId = args[2];
    String filepath = args[3];
    CommandLineParser clParser = new DefaultParser();
    CommandLine cmd = clParser.parse(opt, args);

    try {
      // Set up JDBC connection with Cloud Spanner
      Connection connection = DriverManager.getConnection(
          String.format(
              "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
              projectId, instanceId, databaseId));
      CloudSpannerJdbcConnection cs = connection.unwrap(CloudSpannerJdbcConnection.class);

      // Parse the datatype schema of the file
      ResultSet spannerType = connection.createStatement().executeQuery("SELECT spanner_type FROM information_schema.columns WHERE table_name = \"" + tableId
          + "\"");
      while (spannerType.next()) {
        spannerSchema.add(parseSpannerDataType(spannerType.getString("spanner_type")));
      }

      // Get headers from table in database
      List<String> headers = new ArrayList<>();
      ResultSet tableHeader = connection.createStatement().executeQuery("SELECT column_name FROM information_schema.columns WHERE table_name = \"" + tableId
              + "\"");
      while (tableHeader.next()) {
        headers.add(tableHeader.getString("column_name"));
      }

      Reader in = new FileReader(filepath);
      CSVFormat parseFormat = setFormat(cmd);
      CSVParser parser = CSVParser.parse(in, parseFormat);
      List<Mutation> mutations = new ArrayList<>();
      Iterable<CSVRecord> records = parser;

      // Verify header names with column names
      if (cmd.hasOption("h")) {
        List<String> allHeaders = parser.getHeaderNames();
        for (int i = 0; i < allHeaders.size(); i++) {
          if (!allHeaders.get(i).equals(headers.get(i))) {
            System.err.println(
                "Header " + allHeaders.get(i) + " does not match database table header " + headers
                    .get(i));
            return;
          }
        }
      }

      // Upload records into database table
      System.out.println("Writing data into table...");
      for (CSVRecord record : records) {
        WriteBuilder builder = Mutation.newInsertOrUpdateBuilder(tableId);
        for (int i = 0; i < headers.size(); i++) {
          if (record.get(i) != null) {
            switch (spannerSchema.get(i)) {
              case BOOL:
                builder.set(headers.get(i)).to(Boolean.parseBoolean(record.get(i)));
                break;
              case INT64:
                builder.set(headers.get(i)).to(Integer.parseInt(record.get(i).trim()));
                break;
              case FLOAT64:
                builder.set(headers.get(i)).to(Float.parseFloat(record.get(i).trim()));
                break;
              case STRING:
                builder.set(headers.get(i)).to(record.get(i).trim());
                break;
              case DATE:
                builder.set(headers.get(i))
                    .to(com.google.cloud.Date.parseDate(record.get(i).trim()));
                break;
              case TIMESTAMP:
                builder.set(headers.get(i))
                    .to(com.google.cloud.Timestamp.parseTimestamp(record.get(i)));
                break;
              default:
                System.err.print("Invalid Type. This type is not supported.");
            }
          }
        }
        mutations.add(builder.build());
      }

      cs.write(mutations);
      cs.close();
      System.out.println("Data successfully written into table.");
    } finally {
      // Closes the client which will free up the resources used
      spanner.close();
    }
  }
}


