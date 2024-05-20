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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.beam.vendor.guava.v32_1_2_jre.com.google.common.collect.ImmutableList;
import org.apache.beam.vendor.guava.v32_1_2_jre.com.google.common.collect.ImmutableMap;
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ApacheIcebergIT {
  private ByteArrayOutputStream bout;
  private PrintStream out;
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private String location;

  private Configuration hadoopConf = new Configuration();
  private Catalog catalog;
  private static final TableIdentifier tableId = TableIdentifier.of("db", "table1");
  private Table table;

  private static final String ouputFileName = "output-00000-of-00001.txt";

  private void createIcebergTable(Catalog catalog, TableIdentifier tableId) throws IOException {
    var schema = new Schema(
        NestedField.required(1, "id", Types.LongType.get()),
        NestedField.optional(2, "name", Types.StringType.get()));

    table = catalog.createTable(tableId, schema);
  }

  private void writeRecords()
      throws IOException {
    GenericRecord record = GenericRecord.create(table.schema());
    ImmutableList<Record> records =
        ImmutableList.of(
            record.copy(ImmutableMap.of("id", 0L, "name", "Person-0")),
            record.copy(ImmutableMap.of("id", 1L, "name", "Person-1")),
            record.copy(ImmutableMap.of("id", 2L, "name", "Person-2")));

    Path path = new Path(location, "file1.parquet");

    FileAppender<Record> appender =
        Parquet.write(HadoopOutputFile.fromPath(path, hadoopConf))
            .createWriterFunc(GenericParquetWriter::buildWriter)
            .schema(table.schema())
            .overwrite()
            .build();
    appender.addAll(records);
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

    // Create an Apache Iceberg catalog with a table and write some test data.
    File warehouseFolder = temporaryFolder.newFolder();
    location = "file:" + warehouseFolder.toString();
    catalog =
        CatalogUtil.loadCatalog(
            CatalogUtil.ICEBERG_CATALOG_HADOOP,
            "local",
            ImmutableMap.of(CatalogProperties.WAREHOUSE_LOCATION, location),
            hadoopConf);

    createIcebergTable(catalog, tableId);
  }

  @After
  public void tearDown() throws IOException {
    Files.deleteIfExists(Paths.get(ouputFileName));
    System.setOut(null);
  }

  @Test
  public void testApacheIcebergWrite() {
    // Run the Dataflow pipeline.
    ApacheIcebergWrite.main(
        new String[] {
            "--runner=DirectRunner",
            "--warehouseLocation=" + location
        });

    // Verify that the pipeline wrote records to the table.
    Table table = catalog.loadTable(tableId);
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
    writeRecords();

    // Run the Dataflow pipeline.
    ApacheIcebergRead.main(
        new String[] {
            "--runner=DirectRunner",
            "--warehouseLocation=" + location,
            "--outputPath=output"
        });

    // Verify the pipeline wrote the table data to a local file.
    String output = Files.readString(Paths.get(ouputFileName));
    assertTrue(output.contains("0:Person-0"));
    assertTrue(output.contains("1:Person-1"));
    assertTrue(output.contains("2:Person-2"));
  }
}
