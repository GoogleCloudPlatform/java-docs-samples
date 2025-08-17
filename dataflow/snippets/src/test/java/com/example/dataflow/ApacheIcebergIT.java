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

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.storage.testing.RemoteStorageHelper;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
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

  private Configuration hadoopConf = new Configuration();
  private Storage storage = StorageOptions.getDefaultInstance().getService();
  private java.nio.file.Path warehouseDirectory;
  private String warehouseLocation;
  private String bucketName;
  private Catalog catalog;
  private static final String CATALOG_NAME = "local";

  String outputFileNamePrefix = UUID.randomUUID().toString();
  String outputFileName = outputFileNamePrefix + "-00000-of-00001.txt";
  String table = "user_clicks.streaming_write";
  String destinationTable = "user_clicks.cdc_destination";

  private Table createIcebergTable(String name) {

    TableIdentifier tableId = TableIdentifier.of(name);

    // This schema represents an Iceberg table schema. It needs to match the
    // org.apache.beam.sdk.schemas.Schema that is defined in ApacheIcebergWrite. However, these
    // are unrelated types so there isn't a straightforward conversion from one to the other.
    var schema = new Schema(
        NestedField.required(1, "id", Types.LongType.get()),
        NestedField.optional(2, "name", Types.StringType.get()));

    return catalog.createTable(tableId, schema);
  }

  private void writeTableRecord(Table table)
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

  private boolean tableContainsRecord(Table table, String data) {
    CloseableIterable<Record> records = IcebergGenerics.read(table).build();
    for (Record r : records) {
      if (r.toString().contains(data)) {
        return true;
      }
    }
    return false;
  }

  @Before
  public void setUp() throws IOException {
    // Create an Apache Iceberg catalog with a table.
    warehouseDirectory = Files.createTempDirectory("test-warehouse");
    warehouseLocation = "file:" + warehouseDirectory.toString();
    catalog =
        CatalogUtil.loadCatalog(
            CatalogUtil.ICEBERG_CATALOG_HADOOP,
            CATALOG_NAME,
            ImmutableMap.of(CatalogProperties.WAREHOUSE_LOCATION, warehouseLocation),
            hadoopConf);
    bucketName = "test-bucket-" + UUID.randomUUID();
    storage.create(BucketInfo.newBuilder(bucketName).setLocation("us-central1").build());
  }

  @After
  public void tearDown() throws IOException, ExecutionException, InterruptedException {
    Files.deleteIfExists(Paths.get(outputFileName));
    if (bucketName != null) {
      RemoteStorageHelper.forceDelete(storage, bucketName, 1, TimeUnit.MINUTES);
    }
  }
 
  @Test
  public void testApacheIcebergRestCatalog() throws IOException, InterruptedException {
    String warehouse = "gs://" + bucketName;
    Thread thread =
        new Thread(
            () -> {
              try {
                ApacheIcebergRestCatalogStreamingWrite.main(
                    new String[] {
                      "--runner=DirectRunner",
                      "--warehouse=" + warehouse,
                      "--icebergTable=" + table,
                      "--catalogName=biglake",
                    });
              } catch (Exception e) {
                // We expect an InterruptedException when the test interrupts the thread.
                // We can ignore it.
                if (!(e.getCause() instanceof InterruptedException)) {
                  throw new RuntimeException(e);
                }
              }
            });

    thread.start();
    Thread.sleep(60000);
    thread.interrupt();
    thread.join();

    Thread cdcThread =
        new Thread(
            () -> {
              try {
                ApacheIcebergCdcRead.main(
                    new String[] {
                      "--runner=DirectRunner",
                      "--sourceTable=" + table,
                      "--destinationTable=" + destinationTable,
                      "--warehouse=" + warehouse,
                      "--catalogName=" + "biglake",
                    });
              } catch (Exception e) {
                if (!(e.getCause() instanceof InterruptedException)) {
                  throw new RuntimeException(e);
                }
              }
            });
    cdcThread.start();
    Thread.sleep(120000);
    cdcThread.interrupt();
    cdcThread.join();

    boolean dataFolderHasFiles = false;
    boolean metadataFolderHasFiles = false;

    Page<Blob> blobs = storage.list(bucketName);
    for (Blob blob : blobs.iterateAll()) {
      if (blob.getName().startsWith("user_clicks/cdc_destination/data/") && blob.getSize() > 0) {
        dataFolderHasFiles = true;
      }
      if (blob.getName().startsWith("user_clicks/cdc_destination/metadata/")
          && blob.getSize() > 0) {
        metadataFolderHasFiles = true;
      }
    }

    assertTrue(dataFolderHasFiles);
    assertTrue(metadataFolderHasFiles);
  }

  @Test
  public void testApacheIcebergWrite() {
    String tableName = "write_table";
    final Table table = createIcebergTable("write_table");

    // Run the Dataflow pipeline.
    ApacheIcebergWrite.main(
        new String[] {
            "--runner=DirectRunner",
            "--warehouseLocation=" + warehouseLocation,
            "--catalogName=" + CATALOG_NAME,
            "--tableName=" + tableName
        });

    // Verify that the pipeline wrote records to the table.
    assertTrue(tableContainsRecord(table, "0, Alice"));
    assertTrue(tableContainsRecord(table, "1, Bob"));
    assertTrue(tableContainsRecord(table, "2, Charles"));
  }

  @Test
  public void testApacheIcebergDynamicDestinations() {
    final Table tableORD = createIcebergTable("flights-ORD");
    final Table tableSYD = createIcebergTable("flights-SYD");

    // Run the Dataflow pipeline.
    ApacheIcebergDynamicDestinations.main(
        new String[] {
            "--runner=DirectRunner",
            "--warehouseLocation=" + warehouseLocation,
            "--catalogName=" + CATALOG_NAME
        });

    // Verify that the pipeline wrote records to the correct tables.
    assertTrue(tableContainsRecord(tableORD, "0, Alice"));
    assertTrue(tableContainsRecord(tableORD, "2, Charles"));
    assertTrue(tableContainsRecord(tableSYD, "1, Bob"));
  }

  @Test
  public void testApacheIcebergRead() throws IOException {
    String tableName = "read_table";
    final Table table = createIcebergTable(tableName);

    // Seed the Apache Iceberg table with data.
    writeTableRecord(table);

    // Run the Dataflow pipeline.
    ApacheIcebergRead.main(
        new String[] {
            "--runner=DirectRunner",
            "--warehouseLocation=" + warehouseLocation,
            "--catalogName=" + CATALOG_NAME,
            "--tableName=" + tableName,
            "--outputPath=" + outputFileNamePrefix
        });

    // Verify the pipeline wrote the table data to a text file.
    String output = Files.readString(Paths.get(outputFileName));
    assertTrue(output.contains("0:Person-0"));
  }
}
