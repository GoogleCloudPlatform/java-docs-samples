/*
 * Copyright 2023 Google LLC
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

package com.google.cloud.storage.storageinsights.samples.test;

import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.Binding;
import com.google.cloud.Policy;
import com.google.cloud.resourcemanager.v3.Project;
import com.google.cloud.resourcemanager.v3.ProjectName;
import com.google.cloud.resourcemanager.v3.ProjectsClient;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageClass;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.storage.storageinsights.samples.CreateInventoryReportConfig;
import com.google.cloud.storage.storageinsights.samples.DeleteInventoryReportConfig;
import com.google.cloud.storage.storageinsights.samples.EditInventoryReportConfig;
import com.google.cloud.storage.storageinsights.samples.GetInventoryReportNames;
import com.google.cloud.storage.storageinsights.samples.ListInventoryReportConfigs;
import com.google.cloud.storage.testing.RemoteStorageHelper;
import com.google.cloud.storageinsights.v1.LocationName;
import com.google.cloud.storageinsights.v1.ReportConfig;
import com.google.cloud.storageinsights.v1.StorageInsightsClient;
import com.google.cloud.testing.junit4.MultipleAttemptsRule;
import com.google.cloud.testing.junit4.StdOutCaptureRule;
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

public class ITStorageinsightsSamplesTest {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String SINK_BUCKET = "insights-test-bucket-sink" + UUID.randomUUID();
  private static final String SOURCE_BUCKET = "insights-test-bucket-source" + UUID.randomUUID();
  public static final String BUCKET_LOCATION = "us-west1";
  private static Storage storage;
  private static StorageInsightsClient insights;

  @Rule(order = 1)
  public final StdOutCaptureRule stdOutCaptureRule = new StdOutCaptureRule();

  // This is in case the tests fail due to the permissions for the service account needing extra
  // time to propagate.
  @Rule(order = 2)
  public final MultipleAttemptsRule multipleAttemptsRule = new MultipleAttemptsRule(5);

  @BeforeClass
  public static void beforeClass() throws Exception {
    insights = StorageInsightsClient.create();

    storage = StorageOptions.newBuilder().build().getService();
    storage.create(
        BucketInfo.newBuilder(SOURCE_BUCKET)
            .setLocation(BUCKET_LOCATION)
            .setLifecycleRules(
                ImmutableList.of(
                    new BucketInfo.LifecycleRule(
                        BucketInfo.LifecycleRule.LifecycleAction.newDeleteAction(),
                        BucketInfo.LifecycleRule.LifecycleCondition.newBuilder()
                            .setAge(1)
                            .build())))
            .build());
    storage.create(
        BucketInfo.newBuilder(SINK_BUCKET)
            .setLocation(BUCKET_LOCATION)
            .setLifecycleRules(
                ImmutableList.of(
                    new BucketInfo.LifecycleRule(
                        BucketInfo.LifecycleRule.LifecycleAction.newDeleteAction(),
                        BucketInfo.LifecycleRule.LifecycleCondition.newBuilder()
                            .setAge(1)
                            .build())))
            .setStorageClass(StorageClass.NEARLINE)
            .build());

    ProjectsClient pc = ProjectsClient.create();
    Project project = pc.getProject(ProjectName.of(PROJECT_ID));
    String projectNumber = project.getName().split("/")[1];
    String insightsServiceAccount =
        "service-" + projectNumber + "@gcp-sa-storageinsights.iam.gserviceaccount.com";

    grantBucketsInsightsPermissions(insightsServiceAccount, SOURCE_BUCKET);
    grantBucketsInsightsPermissions(insightsServiceAccount, SINK_BUCKET);
  }

  @AfterClass
  public static void afterClass() throws Exception {
    if (storage != null) {
      long cleanTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2);
      long cleanTimeout = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1);
      RemoteStorageHelper.cleanBuckets(storage, cleanTime, cleanTimeout);

      RemoteStorageHelper.forceDelete(storage, SINK_BUCKET, 1, TimeUnit.MINUTES);
      RemoteStorageHelper.forceDelete(storage, SOURCE_BUCKET, 1, TimeUnit.MINUTES);
    }
  }

  private static void grantBucketsInsightsPermissions(String serviceAccount, String bucket)
      throws IOException {

    Policy policy =
        storage.getIamPolicy(bucket, Storage.BucketSourceOption.requestedPolicyVersion(3));

    String insightsCollectorService = "roles/storage.insightsCollectorService";
    String objectCreator = "roles/storage.objectCreator";
    String member = "serviceAccount:" + serviceAccount;

    List<Binding> bindings = new ArrayList<>(policy.getBindingsList());

    Binding objectViewerBinding =
        Binding.newBuilder()
            .setRole(insightsCollectorService)
            .setMembers(Arrays.asList(member))
            .build();
    bindings.add(objectViewerBinding);

    Binding bucketReaderBinding =
        Binding.newBuilder().setRole(objectCreator).setMembers(Arrays.asList(member)).build();
    bindings.add(bucketReaderBinding);

    Policy.Builder newPolicy = policy.toBuilder().setBindings(bindings).setVersion(3);
    storage.setIamPolicy(bucket, newPolicy.build());
  }

  @Test
  public void testCreateInventoryReportConfig() throws Exception {
    CreateInventoryReportConfig.createInventoryReportConfig(
        PROJECT_ID, BUCKET_LOCATION, SOURCE_BUCKET, SINK_BUCKET);
    String sampleOutput = stdOutCaptureRule.getCapturedOutputAsUtf8String();
    assertThat(sampleOutput.contains("reportConfigs/"));
    deleteInventoryReportConfig(sampleOutput);
  }

  @Test
  public void testDeleteInventoryReportConfig() throws Exception {
    CreateInventoryReportConfig.createInventoryReportConfig(
        PROJECT_ID, BUCKET_LOCATION, SOURCE_BUCKET, SINK_BUCKET);
    String sampleOutput = stdOutCaptureRule.getCapturedOutputAsUtf8String();
    String reportConfigName = getReportConfigNameFromSampleOutput(sampleOutput);

    DeleteInventoryReportConfig.deleteInventoryReportConfig(
        PROJECT_ID, BUCKET_LOCATION, reportConfigName.split("/")[5]);
    for (ReportConfig config :
        insights.listReportConfigs(LocationName.of(PROJECT_ID, BUCKET_LOCATION)).iterateAll()) {
      assertThat(!config.getName().equals(reportConfigName));
    }
  }

  @Test
  public void testEditInventoryReportConfig() throws Exception {
    CreateInventoryReportConfig.createInventoryReportConfig(
        PROJECT_ID, BUCKET_LOCATION, SOURCE_BUCKET, SINK_BUCKET);
    String sampleOutput = stdOutCaptureRule.getCapturedOutputAsUtf8String();
    String reportConfigName = getReportConfigNameFromSampleOutput(sampleOutput);
    try {
      EditInventoryReportConfig.editInventoryReportConfig(
          PROJECT_ID, BUCKET_LOCATION, reportConfigName.split("/")[5]);
      ReportConfig reportConfig = insights.getReportConfig(reportConfigName);
      assertThat(reportConfig.getDisplayName().contains("Updated"));
    } finally {
      insights.deleteReportConfig(reportConfigName);
    }
  }

  @Test
  public void testListInventoryReportConfigs() throws Exception {
    CreateInventoryReportConfig.createInventoryReportConfig(
        PROJECT_ID, BUCKET_LOCATION, SOURCE_BUCKET, SINK_BUCKET);
    String sampleOutput = stdOutCaptureRule.getCapturedOutputAsUtf8String();
    int originalSampleOutputLength = sampleOutput.length();
    String reportConfigName = getReportConfigNameFromSampleOutput(sampleOutput);
    try {
      ListInventoryReportConfigs.listInventoryReportConfigs(PROJECT_ID, BUCKET_LOCATION);
      sampleOutput = stdOutCaptureRule.getCapturedOutputAsUtf8String();
      // Using originalSampleOutputLength as fromIndex prevents the output from the creation from
      // being taken into account
      assertThat(sampleOutput.indexOf(reportConfigName, originalSampleOutputLength) > -1);
    } finally {
      insights.deleteReportConfig(reportConfigName);
    }
  }

  @Test
  public void testGetInventoryReportConfigNames() throws Exception {
    CreateInventoryReportConfig.createInventoryReportConfig(
        PROJECT_ID, BUCKET_LOCATION, SOURCE_BUCKET, SINK_BUCKET);
    String sampleOutput = stdOutCaptureRule.getCapturedOutputAsUtf8String();
    String reportConfigName = getReportConfigNameFromSampleOutput(sampleOutput);
    try {
      GetInventoryReportNames.getInventoryReportNames(
          PROJECT_ID, BUCKET_LOCATION, reportConfigName.split("/")[5]);
      /* We can't actually test for a report config name showing up here, because we create
       * the bucket and inventory configs for this test, and it takes 24 hours for an
       * inventory report to actually get written to the bucket.
       * We could set up a hard-coded bucket, but that would probably introduce flakes.
       * The best we can do is make sure the test runs without throwing an error
       */
    } finally {
      insights.deleteReportConfig(reportConfigName);
    }
  }

  private static void deleteInventoryReportConfig(String sampleOutput) throws IOException {
    String reportConfigName = getReportConfigNameFromSampleOutput(sampleOutput);
    insights.deleteReportConfig(reportConfigName);
  }

  // Gets the last instance of a Report Config Name from an output string
  private static String getReportConfigNameFromSampleOutput(String sampleOutput)
      throws IOException {
    Pattern pattern = Pattern.compile(".*(projects/.*)");
    return ImmutableList.copyOf(CharStreams.readLines(new StringReader(sampleOutput)))
        .reverse()
        .stream()
        .map(pattern::matcher)
        .filter(Matcher::matches)
        .map(m -> m.group(1))
        .findFirst()
        .orElse("");
  }
}
