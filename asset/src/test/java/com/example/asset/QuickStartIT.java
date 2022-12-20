/*
 * Copyright 2018 Google LLC
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

package com.example.asset;

import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.asset.v1.ContentType;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQuery.DatasetDeleteOption;
import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.DatasetId;
import com.google.cloud.bigquery.DatasetInfo;
import com.google.cloud.bigquery.testing.RemoteBigQueryHelper;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.BlobListOption;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.testing.junit4.MultipleAttemptsRule;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for quickstart sample. */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class QuickStartIT {
  @Rule public final Timeout testTimeout = new Timeout(10, TimeUnit.MINUTES);
  @Rule public final MultipleAttemptsRule multipleAttemptsRule = new MultipleAttemptsRule(3);
  
  private static final String bucketName = "java-docs-samples-testing";
  private static final String[] assetTypes = { "compute.googleapis.com/Disk" };

  private ByteArrayOutputStream bout;
  private PrintStream out;
  private PrintStream originalPrintStream;
  private BigQuery bigquery;

  private static final void deleteObjects(String path) {
    Storage storage = StorageOptions.getDefaultInstance().getService();
    for (BlobInfo info :
        storage
            .list(
                bucketName,
                BlobListOption.versions(true),
                BlobListOption.currentDirectory(),
                BlobListOption.prefix(path + "/"))
            .getValues()) {
      storage.delete(info.getBlobId());
    }
  }

  @Before
  public void setUp() {
    bigquery = BigQueryOptions.getDefaultInstance().getService();
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    originalPrintStream = System.out;
    System.setOut(out);
  }

  @After
  public void tearDown() {
    // restores print statements in the original method
    System.out.flush();
    System.setOut(originalPrintStream);
  }

  @Test
  public void testExportAssetExample() throws Exception {
    String path = UUID.randomUUID().toString();
    try {
      String assetDumpPath = String.format("gs://%s/%s/my-assets-dump.txt", bucketName, path);
      ExportAssetsExample.exportAssets(assetDumpPath, ContentType.RESOURCE, assetTypes);
      String got = bout.toString();
      assertThat(got).contains(String.format("uri: \"%s\"", assetDumpPath));
    } finally {
      deleteObjects(path); 
    }
  }

  @Test
  public void testExportAssetBigqueryPerTypeExample() throws Exception {
    String datasetName = RemoteBigQueryHelper.generateDatasetName();    
    try {
      String dataset = getDataset(datasetName);
      String table = "java_test_per_type";
      ExportAssetsBigqueryExample.exportBigQuery(dataset, table, ContentType.RESOURCE, assetTypes,
          /*perType*/ true);
      String got = bout.toString();
      assertThat(got).contains(String.format("dataset: \"%s\"", dataset));
    } finally {
      deleteDataset(datasetName);
    }
  }

  @Test
  public void testExportAssetBigqueryExample() throws Exception {
    String datasetName = RemoteBigQueryHelper.generateDatasetName();    
    try {
      String dataset = getDataset(datasetName);
      String table = "java_test";
      String[] assetTypes = { "compute.googleapis.com/Disk" };
      ExportAssetsBigqueryExample.exportBigQuery(
          dataset, table, ContentType.RESOURCE, assetTypes, /*perType*/ false);
      String got = bout.toString();
      assertThat(got).contains(String.format("dataset: \"%s\"", dataset));
    } finally {
      deleteDataset(datasetName);
    }
  }

  @Test
  public void testBatchGetAssetsHistory() throws Exception {
    String bucketAssetName = String.format("//storage.googleapis.com/%s", bucketName);
    BatchGetAssetsHistoryExample.main(bucketAssetName);
    String got = bout.toString();
    if (!got.isEmpty()) {
      assertThat(got).contains(bucketAssetName);
    }
  }

  protected String getDataset(String datasetName) throws BigQueryException {
    bigquery.create(DatasetInfo.newBuilder(datasetName).build());
    return String.format(
      "projects/%s/datasets/%s", bigquery.getOptions().getProjectId(), datasetName);
  }

  protected void deleteDataset(String datasetName) {
    DatasetId datasetId = DatasetId.of(bigquery.getOptions().getProjectId(), datasetName);
    bigquery.delete(datasetId, DatasetDeleteOption.deleteContents());
  }


}
