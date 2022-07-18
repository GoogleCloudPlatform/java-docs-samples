/*
 * Copyright 2021 Google Inc.
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

package com.google.cloud.storage.storagetransfer.samples.test;

import static com.google.common.truth.Truth.assertThat;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.api.services.storagetransfer.v1.Storagetransfer;
import com.google.api.services.storagetransfer.v1.model.Date;
import com.google.api.services.storagetransfer.v1.model.GcsData;
import com.google.api.services.storagetransfer.v1.model.ObjectConditions;
import com.google.api.services.storagetransfer.v1.model.Schedule;
import com.google.api.services.storagetransfer.v1.model.TimeOfDay;
import com.google.api.services.storagetransfer.v1.model.TransferJob;
import com.google.api.services.storagetransfer.v1.model.TransferOptions;
import com.google.api.services.storagetransfer.v1.model.TransferSpec;
import com.google.cloud.Binding;
import com.google.cloud.Policy;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.BucketInfo.LifecycleRule;
import com.google.cloud.storage.BucketInfo.LifecycleRule.LifecycleAction;
import com.google.cloud.storage.BucketInfo.LifecycleRule.LifecycleCondition;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageClass;
import com.google.cloud.storage.storagetransfer.samples.CheckLatestTransferOperation;
import com.google.cloud.storage.storagetransfer.samples.DownloadToPosix;
import com.google.cloud.storage.storagetransfer.samples.QuickstartSample;
import com.google.cloud.storage.storagetransfer.samples.TransferBetweenPosix;
import com.google.cloud.storage.storagetransfer.samples.TransferFromAws;
import com.google.cloud.storage.storagetransfer.samples.TransferFromPosix;
import com.google.cloud.storage.storagetransfer.samples.TransferToNearline;
import com.google.cloud.storage.storagetransfer.samples.TransferUsingManifest;
import com.google.cloud.storage.storagetransfer.samples.apiary.CheckLatestTransferOperationApiary;
import com.google.cloud.storage.storagetransfer.samples.apiary.CreateTransferClient;
import com.google.cloud.storage.storagetransfer.samples.apiary.TransferFromAwsApiary;
import com.google.cloud.storage.storagetransfer.samples.apiary.TransferToNearlineApiary;
import com.google.cloud.storage.storagetransfer.samples.test.util.TransferJobUtils;
import com.google.cloud.storage.testing.RemoteStorageHelper;
import com.google.cloud.testing.junit4.StdOutCaptureRule;
import com.google.common.collect.ImmutableList;
import com.google.storagetransfer.v1.proto.StorageTransferServiceClient;
import com.google.storagetransfer.v1.proto.TransferProto;
import com.google.storagetransfer.v1.proto.TransferProto.GetGoogleServiceAccountRequest;
import com.google.storagetransfer.v1.proto.TransferTypes;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

public class ITStoragetransferSamplesTest {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String SINK_GCS_BUCKET = "sts-test-bucket-sink" + UUID.randomUUID();
  private static final String SOURCE_GCS_BUCKET = "sts-test-bucket-source" + UUID.randomUUID();
  private static final String AMAZON_BUCKET = "sts-amazon-bucket" + UUID.randomUUID();
  private static Storage storage;
  private static AmazonS3 s3;
  private static StorageTransferServiceClient sts;

  @Rule public final StdOutCaptureRule stdOutCaptureRule = new StdOutCaptureRule();

  @BeforeClass
  public static void beforeClass() throws Exception {
    RemoteStorageHelper helper = RemoteStorageHelper.create();
    storage = helper.getOptions().getService();

    storage.create(
        BucketInfo.newBuilder(SOURCE_GCS_BUCKET)
            .setLocation("us")
            .setLifecycleRules(
                ImmutableList.of(
                    new LifecycleRule(
                        LifecycleAction.newDeleteAction(),
                        LifecycleCondition.newBuilder().setAge(1).build())))
            .build());
    storage.create(
        BucketInfo.newBuilder(SINK_GCS_BUCKET)
            .setLocation("us")
            .setLifecycleRules(
                ImmutableList.of(
                    new LifecycleRule(
                        LifecycleAction.newDeleteAction(),
                        LifecycleCondition.newBuilder().setAge(1).build())))
            .setStorageClass(StorageClass.NEARLINE)
            .build());

    sts = StorageTransferServiceClient.create();
    String serviceAccount =
        sts.getGoogleServiceAccount(
                GetGoogleServiceAccountRequest.newBuilder().setProjectId(PROJECT_ID).build())
            .getAccountEmail();

    grantBucketsStsPermissions(serviceAccount, SOURCE_GCS_BUCKET);
    grantBucketsStsPermissions(serviceAccount, SINK_GCS_BUCKET);

    s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.US_WEST_1).build();

    s3.createBucket(AMAZON_BUCKET);
  }

  private static void grantBucketsStsPermissions(String serviceAccount, String bucket)
      throws Exception {
    Policy policy =
        storage.getIamPolicy(bucket, Storage.BucketSourceOption.requestedPolicyVersion(3));

    String objectViewer = "roles/storage.objectViewer";
    String bucketReader = "roles/storage.legacyBucketReader";
    String bucketWriter = "roles/storage.legacyBucketWriter";
    String member = "serviceAccount:" + serviceAccount;

    List<Binding> bindings = new ArrayList<>(policy.getBindingsList());

    Binding objectViewerBinding =
        Binding.newBuilder().setRole(objectViewer).setMembers(Arrays.asList(member)).build();
    bindings.add(objectViewerBinding);

    Binding bucketReaderBinding =
        Binding.newBuilder().setRole(bucketReader).setMembers(Arrays.asList(member)).build();
    bindings.add(bucketReaderBinding);

    Binding bucketWriterBinding =
        Binding.newBuilder().setRole(bucketWriter).setMembers(Arrays.asList(member)).build();
    bindings.add(bucketWriterBinding);

    Policy.Builder newPolicy = policy.toBuilder().setBindings(bindings).setVersion(3);
    storage.setIamPolicy(bucket, newPolicy.build());
  }

  private static void cleanAmazonBucket() {
    try {
      ObjectListing objectListing = s3.listObjects(AMAZON_BUCKET);
      while (true) {
        for (Iterator<?> iterator = objectListing.getObjectSummaries().iterator();
            iterator.hasNext(); ) {
          S3ObjectSummary summary = (S3ObjectSummary) iterator.next();
          s3.deleteObject(AMAZON_BUCKET, summary.getKey());
        }

        if (objectListing.isTruncated()) {
          objectListing = s3.listNextBatchOfObjects(objectListing);
        } else {
          break;
        }
      }
      s3.deleteBucket(AMAZON_BUCKET);
    } catch (AmazonServiceException e) {
      System.err.println(e.getErrorMessage());
    }
  }

  // deletes a transfer job created by a sample to clean up
  private void deleteTransferJob(String sampleOutput) {
    Pattern pattern = Pattern.compile("(transferJobs/[a-zA-Z0-9]+)");
    Matcher matcher = pattern.matcher(sampleOutput);
    matcher.find();
    String jobName = matcher.group(1);

    TransferTypes.TransferJob job =
        TransferTypes.TransferJob.newBuilder()
            .setName(jobName)
            .setStatus(TransferTypes.TransferJob.Status.DELETED)
            .build();
    sts.updateTransferJob(
        TransferProto.UpdateTransferJobRequest.newBuilder()
            .setProjectId(PROJECT_ID)
            .setJobName(jobName)
            .setTransferJob(job)
            .build());
  }

  @AfterClass
  public static void afterClass() throws ExecutionException, InterruptedException {
    if (storage != null) {
      long cleanTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2);
      long cleanTimeout = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1);
      RemoteStorageHelper.cleanBuckets(storage, cleanTime, cleanTimeout);

      RemoteStorageHelper.forceDelete(storage, SINK_GCS_BUCKET, 1, TimeUnit.MINUTES);
      RemoteStorageHelper.forceDelete(storage, SOURCE_GCS_BUCKET, 1, TimeUnit.MINUTES);
    }

    cleanAmazonBucket();

    sts.shutdownNow();
  }

  @Test
  public void testCheckLatestTransferOperationApiary() throws Exception {
    Date date = TransferJobUtils.createDate("2000-01-01");
    TimeOfDay time = TransferJobUtils.createTimeOfDay("00:00:00");
    TransferJob transferJob =
        new TransferJob()
            .setDescription("Sample job")
            .setProjectId(PROJECT_ID)
            .setTransferSpec(
                new TransferSpec()
                    .setGcsDataSource(new GcsData().setBucketName(SOURCE_GCS_BUCKET))
                    .setGcsDataSink(new GcsData().setBucketName(SINK_GCS_BUCKET))
                    .setObjectConditions(
                        new ObjectConditions()
                            .setMinTimeElapsedSinceLastModification("2592000s" /* 30 days */))
                    .setTransferOptions(
                        new TransferOptions().setDeleteObjectsFromSourceAfterTransfer(false)))
            .setSchedule(new Schedule().setScheduleStartDate(date).setStartTimeOfDay(time))
            .setStatus("ENABLED");

    Storagetransfer client = CreateTransferClient.createStorageTransferClient();
    TransferJob response = client.transferJobs().create(transferJob).execute();

    CheckLatestTransferOperationApiary.checkLatestTransferOperationApiary(
        PROJECT_ID, response.getName());

    String sampleOutput = stdOutCaptureRule.getCapturedOutputAsUtf8String();
    assertThat(sampleOutput).contains(response.getName());

    TransferTypes.TransferJob job =
        TransferTypes.TransferJob.newBuilder()
            .setName(response.getName())
            .setStatus(TransferTypes.TransferJob.Status.DELETED)
            .build();
    sts.updateTransferJob(
        TransferProto.UpdateTransferJobRequest.newBuilder()
            .setProjectId(PROJECT_ID)
            .setJobName(response.getName())
            .setTransferJob(job)
            .build());
  }

  @Test
  public void testCheckLatestTransferOperation() throws Exception {
    Date date = TransferJobUtils.createDate("2000-01-01");
    TimeOfDay time = TransferJobUtils.createTimeOfDay("00:00:00");
    TransferJob transferJob =
        new TransferJob()
            .setDescription("Sample job")
            .setProjectId(PROJECT_ID)
            .setTransferSpec(
                new TransferSpec()
                    .setGcsDataSource(new GcsData().setBucketName(SOURCE_GCS_BUCKET))
                    .setGcsDataSink(new GcsData().setBucketName(SINK_GCS_BUCKET))
                    .setObjectConditions(
                        new ObjectConditions()
                            .setMinTimeElapsedSinceLastModification("2592000s" /* 30 days */))
                    .setTransferOptions(
                        new TransferOptions().setDeleteObjectsFromSourceAfterTransfer(false)))
            .setSchedule(new Schedule().setScheduleStartDate(date).setStartTimeOfDay(time))
            .setStatus("ENABLED");

    Storagetransfer client = CreateTransferClient.createStorageTransferClient();

    TransferJob response = client.transferJobs().create(transferJob).execute();

    CheckLatestTransferOperation.checkLatestTransferOperation(PROJECT_ID, response.getName());

    String sampleOutput = stdOutCaptureRule.getCapturedOutputAsUtf8String();
    System.out.println(sampleOutput);
    assertThat(sampleOutput).contains(response.getName());

    TransferTypes.TransferJob job =
        TransferTypes.TransferJob.newBuilder()
            .setName(response.getName())
            .setStatus(TransferTypes.TransferJob.Status.DELETED)
            .build();
    sts.updateTransferJob(
        TransferProto.UpdateTransferJobRequest.newBuilder()
            .setProjectId(PROJECT_ID)
            .setJobName(response.getName())
            .setTransferJob(job)
            .build());
  }

  @Test
  public void testTransferFromAws() throws Exception {
    TransferFromAws.transferFromAws(
        PROJECT_ID,
        "Sample transfer job from S3 to GCS.",
        AMAZON_BUCKET,
        SINK_GCS_BUCKET,
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2000-01-01 00:00:00").getTime(),
        System.getenv("AWS_ACCESS_KEY_ID"),
        System.getenv("AWS_SECRET_ACCESS_KEY"));

    String sampleOutput = stdOutCaptureRule.getCapturedOutputAsUtf8String();
    assertThat(sampleOutput).contains("transferJobs/");

    deleteTransferJob(sampleOutput);
  }

  @Test
  public void testTransferFromAwsApiary() throws Exception {
    TransferFromAwsApiary.transferFromAws(
        PROJECT_ID,
        "Sample transfer job from S3 to GCS.",
        AMAZON_BUCKET,
        SINK_GCS_BUCKET,
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2000-01-01 00:00:00").getTime(),
        System.getenv("AWS_ACCESS_KEY_ID"),
        System.getenv("AWS_SECRET_ACCESS_KEY"));

    String sampleOutput = stdOutCaptureRule.getCapturedOutputAsUtf8String();
    assertThat(sampleOutput).contains("transferJobs/");

    deleteTransferJob(sampleOutput);
  }

  @Test
  public void testTransferToNearlineApiary() throws Exception {
    TransferToNearlineApiary.transferToNearlineApiary(
        PROJECT_ID,
        "Sample transfer job from GCS to GCS Nearline.",
        SOURCE_GCS_BUCKET,
        SINK_GCS_BUCKET,
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2000-01-01 00:00:00").getTime());

    String sampleOutput = stdOutCaptureRule.getCapturedOutputAsUtf8String();
    assertThat(sampleOutput).contains("transferJobs/");

    deleteTransferJob(sampleOutput);
  }

  @Test
  public void testTransferToNearline() throws Exception {
    TransferToNearline.transferToNearline(
        PROJECT_ID,
        "Sample transfer job from GCS to GCS Nearline.",
        SOURCE_GCS_BUCKET,
        SINK_GCS_BUCKET,
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2000-01-01 00:00:00").getTime());

    String sampleOutput = stdOutCaptureRule.getCapturedOutputAsUtf8String();
    assertThat(sampleOutput).contains("transferJobs/");

    deleteTransferJob(sampleOutput);
  }

  @Test
  public void testQuickstart() throws Exception {
    QuickstartSample.quickStartSample(PROJECT_ID, SOURCE_GCS_BUCKET, SINK_GCS_BUCKET);

    String sampleOutput = stdOutCaptureRule.getCapturedOutputAsUtf8String();
    assertThat(sampleOutput).contains("transferJobs/");

    deleteTransferJob(sampleOutput);
  }

  @Test
  public void testDownloadToPosix() throws Exception {
    String sinkAgentPoolName = ""; // use default agent pool
    String rootDirectory = Files.createTempDirectory("sts-download-to-posix-test").toString();
    String gcsSourcePath = rootDirectory + "/";

    storage.create(BlobInfo.newBuilder(SOURCE_GCS_BUCKET, gcsSourcePath + "test.txt").build());
    try {
      DownloadToPosix.downloadToPosix(
          PROJECT_ID, sinkAgentPoolName, SOURCE_GCS_BUCKET, gcsSourcePath, rootDirectory);
    } finally {
      storage.delete(BlobId.of(SOURCE_GCS_BUCKET, gcsSourcePath + "test.txt"));
      String sampleOutput = stdOutCaptureRule.getCapturedOutputAsUtf8String();
      assertThat(sampleOutput).contains("transferJobs/");
      deleteTransferJob(sampleOutput);
    }
  }

  @Test
  public void testTransferFromPosix() throws Exception {
    String sourceAgentPoolName = ""; // use default agent pool
    String rootDirectory = Files.createTempDirectory("sts-transfer-from-posix-test").toString();

    TransferFromPosix.transferFromPosix(
        PROJECT_ID, sourceAgentPoolName, rootDirectory, SINK_GCS_BUCKET);

    String sampleOutput = stdOutCaptureRule.getCapturedOutputAsUtf8String();
    assertThat(sampleOutput).contains("transferJobs/");
    deleteTransferJob(sampleOutput);
  }

  @Test
  public void testTransferBetweenPosix() throws Exception {
    String sinkAgentPoolName = ""; // use default agent pool
    String sourceAgentPoolName = ""; // use default agent pool
    String rootDirectory = Files.createTempDirectory("sts-posix-test-source").toString();
    String destinationDirectory = Files.createTempDirectory("sts-posix-test-sink").toString();

    TransferBetweenPosix.transferBetweenPosix(
        PROJECT_ID,
        sourceAgentPoolName,
        sinkAgentPoolName,
        rootDirectory,
        destinationDirectory,
        SINK_GCS_BUCKET);

    String sampleOutput = stdOutCaptureRule.getCapturedOutputAsUtf8String();
    assertThat(sampleOutput).contains("transferJobs/");
    deleteTransferJob(sampleOutput);
  }

  @Test
  public void testTransferUsingManifest() throws Exception {
    String sourceAgentPoolName = ""; // use default agent pool
    String rootDirectory = Files.createTempDirectory("sts-manifest-test").toString();

    storage.create(BlobInfo.newBuilder(SOURCE_GCS_BUCKET, "manifest.csv").build());
    try {
      TransferUsingManifest.transferUsingManifest(
          PROJECT_ID,
          sourceAgentPoolName,
          rootDirectory,
          SINK_GCS_BUCKET,
          SOURCE_GCS_BUCKET,
          "manifest.csv");
    } finally {
      storage.delete(BlobId.of(SOURCE_GCS_BUCKET, "manifest.csv"));
      String sampleOutput = stdOutCaptureRule.getCapturedOutputAsUtf8String();
      assertThat(sampleOutput).contains("transferJobs/");
      deleteTransferJob(sampleOutput);
    }
  }
}
