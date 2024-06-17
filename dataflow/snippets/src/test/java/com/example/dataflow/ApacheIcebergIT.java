/*
 * Copyright 2024 Google LLC
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

package com.example.dataflow;

import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.CatalogUtil;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DataFiles;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Table;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.IcebergGenerics;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.data.parquet.GenericParquetWriter;
import org.apache.iceberg.hadoop.HadoopInputFile;
import org.apache.iceberg.hadoop.HadoopOutputFile;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.FileAppender;
import org.apache.iceberg.parquet.Parquet;
import org.apache.iceberg.types.Types;
import org.apache.iceberg.types.Types.NestedField;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ApacheIcebergIT {
  private ByteArrayOutputStream bout;
  private PrintStream out;

  private static final String CATALOG_NAME = "local";
  private static final String TABLE_NAME = "table1";
  private static final TableIdentifier TABLE_IDENTIFIER = TableIdentifier.of(TABLE_NAME);

  // The output file that the Dataflow pipeline writes.
  private static final String OUTPUT_FILE_NAME_PREFIX = UUID.randomUUID().toString();
  private static final String OUTPUT_FILE_NAME = OUTPUT_FILE_NAME_PREFIX + "-00000-of-00001.txt";

  private Configuration hadoopConf = new Configuration();
  private java.nio.file.Path warehouseDirectory;
  private String warehouseLocation;
  private Catalog catalog;
  private Table table;


  private void createIcebergTable(Catalog catalog, TableIdentifier tableId) {

    // This schema represents an Iceberg table schema. It needs to match the
    // org.apache.beam.sdk.schemas.Schema that is defined in ApacheIcebergWrite. However, these
    // are unrelated types so there isn't a straightforward conversion from one to the other.
    var schema = new Schema(
        NestedField.required(1, "id", Types.LongType.get()),
        NestedField.optional(2, "name", Types.StringType.get()));

    table = catalog.createTable(tableId, schema);
  }

  private void writeTableRecord()
      throws IOException {
    GenericRecord record = GenericRecord.create(table.schema());
    record.setField("id", 0L);
    record.setField("name", "Person-0");

    Path path = new Path(warehouseLocation, "file1.parquet");

    FileAppender<Record> appender =
        Parquet.write(HadoopOutputFile.fromPath(path, hadoopConf))
            .createWriterFunc(GenericParquetWriter::buildWriter)
            .schema(table.schema())
            .overwrite()
            .build();
    appender.add(record);
    appender.close();

    DataFile dataFile = DataFiles.builder(PartitionSpec.unpartitioned())
        .withInputFile(HadoopInputFile.fromPath(path, hadoopConf))
        .withMetrics(appender.metrics())
        .build();

    table.newFastAppend()
        .appendFile(dataFile)
        .commit();
  }

  @Before
  public void setUp() throws IOException {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);

    // Create an Apache Iceberg catalog with a table.
    warehouseDirectory = Files.createTempDirectory("test-warehouse");
    warehouseLocation = "file:" + warehouseDirectory.toString();
    System.out.println(warehouseLocation);
    catalog =
        CatalogUtil.loadCatalog(
            CatalogUtil.ICEBERG_CATALOG_HADOOP,
            CATALOG_NAME,
            ImmutableMap.of(CatalogProperties.WAREHOUSE_LOCATION, warehouseLocation),
            hadoopConf);
    createIcebergTable(catalog, TABLE_IDENTIFIER);
  }

  @After
  public void tearDown() throws IOException {
    Files.deleteIfExists(Paths.get(OUTPUT_FILE_NAME));
    System.setOut(null);
  }

  @Test
  public void testApacheIcebergWrite() {
    // Run the Dataflow pipeline.
    ApacheIcebergWrite.main(
        new String[] {
            "--runner=DirectRunner",
            "--warehouseLocation=" + warehouseLocation,
            "--catalogName=" + CATALOG_NAME,
            "--tableName=" + TABLE_NAME
        });

    // Verify that the pipeline wrote records to the table.
    Table table = catalog.loadTable(TABLE_IDENTIFIER);
    CloseableIterable<Record> records = IcebergGenerics.read(table)
        .build();
    for (Record r : records) {
      System.out.println(r);
    }

    String got = bout.toString();
    assertTrue(got.contains("0, Alice"));
    assertTrue(got.contains("1, Bob"));
    assertTrue(got.contains("2, Charles"));
  }

  @Test
  public void testApacheIcebergRead() throws IOException {
    // Seed the Apache Iceberg table with data.
    writeTableRecord();

    // Run the Dataflow pipeline.
    ApacheIcebergRead.main(
        new String[] {
            "--runner=DirectRunner",
            "--warehouseLocation=" + warehouseLocation,
            "--catalogName=" + CATALOG_NAME,
            "--tableName=" + TABLE_NAME,
            "--outputPath=" + OUTPUT_FILE_NAME_PREFIX
        });

    // Verify the pipeline wrote the table data to a local file.
    String output = Files.readString(Paths.get(OUTPUT_FILE_NAME));
    assertTrue(output.contains("0:Person-0"));
  }
}
