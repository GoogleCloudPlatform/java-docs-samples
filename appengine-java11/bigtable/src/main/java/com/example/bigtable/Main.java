/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.bigtable;

import com.google.cloud.bigtable.hbase.BigtableConfiguration;
import static java.nio.charset.StandardCharsets.UTF_8;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import org.apache.hadoop.hbase.client.Connection;

/**
 * Trivial http server that listens to port 8080
 */
public class Main {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT)");

  private static final String INSTANCE_ID = "test-instance";

  private static final int PORT = 8080;
  static Connection connection = null;
  // Refer to table metadata names by byte array in the HBase API
  private static final byte[] TABLE_NAME = Bytes.toBytes("Hello-Bigtable");
  private static final byte[] COLUMN_FAMILY_NAME = Bytes.toBytes("cf1");
  private static final byte[] COLUMN_NAME = Bytes.toBytes("greeting");

  // Write some friendly greetings to Cloud Bigtable
  private static final String[] GREETINGS = {
    "Hello World!", "Hello Cloud Bigtable!", "Hello HBase!"
  };

  public static void main(String[] args) throws Exception {
    System.err.println("Starting Main");
    try {
      connection = BigtableConfiguration.connect(PROJECT_ID, INSTANCE_ID);
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.err.println("Starting: connection is: " + connection);

    HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
    server.createContext("/", new RootHandler());
    server.setExecutor(null);
    System.err.println("Starting Simple Web Server.");
    server.start();
  }

  /**
   * Create a table -- first time only.
   *
   * @param connection to Bigtable
   * @return the status
   */
  public static String create(Connection connection) {
    try {
      // The admin API lets us create, manage and delete tables
      Admin admin = connection.getAdmin();

      // Create a table with a single column family
      HTableDescriptor descriptor = new HTableDescriptor(TableName.valueOf(TABLE_NAME));
      descriptor.addFamily(new HColumnDescriptor(COLUMN_FAMILY_NAME));

      admin.createTable(descriptor);
      // [END creating_a_table]
    } catch (IOException e) {
      return "Table exists.";
    }
    return "Create table " + Bytes.toString(TABLE_NAME);
  }

  /**
   * Connects to Cloud Bigtable, runs some basic operations and prints the
   * results.
   */
  static String doHelloWorld() {

    StringBuilder result = new StringBuilder();

    // Create the Bigtable connection, use try-with-resources to make sure it gets closed
    result.append(create(connection));
    result.append("<br><br>");
    try (Table table = connection.getTable(TableName.valueOf(TABLE_NAME))) {

      // Retrieve the table we just created so we can do some reads and writes
      // Write some rows to the table
      result.append("Write some greetings to the table<br>");
      for (int i = 0; i < GREETINGS.length; i++) {
        // Each row has a unique row key.
        //
        // Note: This example uses sequential numeric IDs for simplicity, but
        // this can result in poor performance in a production application.
        // Since rows are stored in sorted order by key, sequential keys can
        // result in poor distribution of operations across nodes.
        //
        // For more information about how to design a Bigtable schema for the
        // best performance, see the documentation:
        //
        //     https://cloud.google.com/bigtable/docs/schema-design
        String rowKey = "greeting" + i;

        // Put a single row into the table. We could also pass a list of Puts to write a batch.
        Put put = new Put(Bytes.toBytes(rowKey));
        put.addColumn(COLUMN_FAMILY_NAME, COLUMN_NAME, Bytes.toBytes(GREETINGS[i]));
        table.put(put);
      }

      // Get the first greeting by row key
      String rowKey = "greeting0";
      Result getResult = table.get(new Get(Bytes.toBytes(rowKey)));
      String greeting = Bytes.toString(getResult.getValue(COLUMN_FAMILY_NAME, COLUMN_NAME));
      result.append("Get a single greeting by row key<br>");
      // [END getting_a_row]
      result.append("     ");
      result.append(rowKey);
      result.append("= ");
      result.append(greeting);
      result.append("<br>");

      // Now scan across all rows.
      Scan scan = new Scan();

      result.append("Scan for all greetings:");
      ResultScanner scanner = table.getScanner(scan);
      for (Result row : scanner) {
        byte[] valueBytes = row.getValue(COLUMN_FAMILY_NAME, COLUMN_NAME);
        result.append("    ");
        result.append(Bytes.toString(valueBytes));
        result.append("<br>");
      }

    } catch (IOException e) {
      result.append("Exception while running HelloWorld: " + e.getMessage() + "<br>");
      result.append(e.toString());
      return result.toString();
    }

    return result.toString();
  }

  static class RootHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
      String response = doHelloWorld();
      exchange.getResponseHeaders().set("Content-Type", "text/html");
      exchange.sendResponseHeaders(200, response.length());
      try (OutputStream os = exchange.getResponseBody()) {
        os.write(response.getBytes(UTF_8));
      }
    }
  }
}
