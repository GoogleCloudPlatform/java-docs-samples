// Copyright 2021 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import static junit.framework.TestCase.assertNotNull;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.gax.paging.Page;
import com.google.api.services.dataflow.Dataflow;
import com.google.api.services.dataflow.model.Job;
import com.google.api.services.dataflow.model.ListJobsResponse;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.pubsublite.AdminClient;
import com.google.cloud.pubsublite.AdminClientSettings;
import com.google.cloud.pubsublite.CloudRegion;
import com.google.cloud.pubsublite.CloudZone;
import com.google.cloud.pubsublite.ProjectId;
import com.google.cloud.pubsublite.SubscriptionName;
import com.google.cloud.pubsublite.SubscriptionPath;
import com.google.cloud.pubsublite.TopicName;
import com.google.cloud.pubsublite.TopicPath;
import com.google.cloud.pubsublite.cloudpubsub.Publisher;
import com.google.cloud.pubsublite.cloudpubsub.PublisherSettings;
import com.google.cloud.pubsublite.proto.Subscription;
import com.google.cloud.pubsublite.proto.Subscription.DeliveryConfig;
import com.google.cloud.pubsublite.proto.Subscription.DeliveryConfig.DeliveryRequirement;
import com.google.cloud.pubsublite.proto.Topic;
import com.google.cloud.pubsublite.proto.Topic.PartitionConfig;
import com.google.cloud.pubsublite.proto.Topic.PartitionConfig.Capacity;
import com.google.cloud.pubsublite.proto.Topic.RetentionConfig;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.protobuf.ByteString;
import com.google.protobuf.util.Durations;
import com.google.pubsub.v1.PubsubMessage;
import examples.PubsubliteToGcs;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.apache.beam.sdk.testing.TestPipeline;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

public class PubsubliteToGcsIT {
  @Rule public final TestPipeline testPipeline = TestPipeline.create();

  private static final String projectId = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String cloudRegion = "us-east1";
  private static final char zoneId = 'b';
  private static final String suffix = UUID.randomUUID().toString().substring(0, 6);
  private static final String topicId = "pubsublite-streaming-analytics-topic-" + suffix;
  private static final String subscriptionId = "pubsublite-streaming-analytics-sub-" + suffix;
  private static final String bucketName = "pubsublite-it";
  private static final String directoryPrefix = "samples/" + suffix;
  private static final String jobName = "pubsublite-dataflow-job-" + suffix;

  private static final Storage storage =
      StorageOptions.newBuilder().setProjectId(projectId).build().getService();

  private static final TopicPath topicPath =
      TopicPath.newBuilder()
          .setProject(ProjectId.of(projectId))
          .setLocation(CloudZone.of(CloudRegion.of(cloudRegion), zoneId))
          .setName(TopicName.of(topicId))
          .build();

  private static final Topic topic =
      Topic.newBuilder()
          .setName(topicPath.toString())
          .setPartitionConfig(
              PartitionConfig.newBuilder()
                  .setCount(1)
                  .setCapacity(
                      Capacity.newBuilder().setPublishMibPerSec(4).setSubscribeMibPerSec(4).build())
                  .build())
          .setRetentionConfig(
              RetentionConfig.newBuilder()
                  .setPeriod(Durations.fromDays(1))
                  .setPerPartitionBytes(30 * 1024 * 1024 * 1024L)
                  .build())
          .build();

  private static final SubscriptionPath subscriptionPath =
      SubscriptionPath.newBuilder()
          .setLocation(CloudZone.of(CloudRegion.of(cloudRegion), zoneId))
          .setProject(ProjectId.of(projectId))
          .setName(SubscriptionName.of(subscriptionId))
          .build();

  private static final Subscription subscription =
      Subscription.newBuilder()
          .setDeliveryConfig(
              DeliveryConfig.newBuilder()
                  .setDeliveryRequirement(DeliveryRequirement.DELIVER_IMMEDIATELY))
          .setName(subscriptionPath.toString())
          .setTopic(topicPath.toString())
          .build();

  private static final AdminClientSettings adminClientSettings =
      AdminClientSettings.newBuilder().setRegion(CloudRegion.of(cloudRegion)).build();

  private static void requireEnvVar(String varName) {
    assertNotNull(
        "Environment variable " + varName + " is required to perform these tests.",
        System.getenv(varName));
  }

  @BeforeClass
  public static void checkRequirements() {
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
  }

  @Before
  public void setUp() throws Exception {
    // Create a test topic and subscription.
    try (AdminClient adminClient = AdminClient.create(adminClientSettings)) {
      Topic responseTopic = adminClient.createTopic(topic).get();
      System.out.println(responseTopic.getAllFields() + " created successfully.");
      Subscription response = adminClient.createSubscription(subscription).get();
      System.out.println(response.getAllFields() + " created successfully.");
    }
  }

  @After
  public void tearDown() throws Exception {
    // Delete the test topic and subscription.
    try (AdminClient adminClient = AdminClient.create(adminClientSettings)) {
      adminClient.deleteTopic(topicPath).get();
      System.out.println("Deleted topic: " + topicPath);
      adminClient.deleteSubscription(subscriptionPath).get();
      System.out.println("Deleted subscription: " + subscriptionPath);
    }

    // Delete the output files.
    Page<Blob> blobs = storage.list(bucketName, Storage.BlobListOption.prefix(directoryPrefix));
    for (Blob blob : blobs.iterateAll()) {
      storage.delete(bucketName, blob.getName());
      System.out.println("Deleted a file: " + blob.getName());
    }

    // Stop the Dataflow job.
    NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

    GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
    HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

    Dataflow dataflow =
        new Dataflow.Builder(httpTransport, GsonFactory.getDefaultInstance(), requestInitializer)
            .build();

    // Match Dataflow job of the same job name and cancel it.
    ListJobsResponse jobs =
        dataflow.projects().locations().jobs().list(projectId, cloudRegion).execute();

    try {
      jobs.getJobs()
          .forEach(
              job -> {
                if (job.getName().equals(jobName)) {
                  String jobId = job.getId();
                  try {
                    dataflow
                        .projects()
                        .locations()
                        .jobs()
                        .update(
                            projectId,
                            cloudRegion,
                            jobId,
                            new Job().setRequestedState("JOB_STATE_CANCELLED"))
                        .execute();
                    System.out.println("Cancelling Dataflow job: " + jobId);
                  } catch (IOException e) {
                    e.printStackTrace();
                  }
                }
              });
    } catch (NullPointerException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testPubsubliteToGcs() throws InterruptedException, ExecutionException {
    // Run the pipeline on Dataflow as instructed in the README.
    PubsubliteToGcs.main(
        new String[] {
          "--subscription=" + subscriptionPath.toString(),
          "--output=gs://" + bucketName + "/" + directoryPrefix + "/output",
          "--windowSize=1",
          "--runner=DataflowRunner",
          "--project=" + projectId,
          "--region=" + cloudRegion,
          "--tempLocation=gs://" + bucketName + "/temp",
          "--jobName=" + jobName
        });

    // Create a publisher client.
    PublisherSettings publisherSettings =
        PublisherSettings.newBuilder().setTopicPath(topicPath).build();
    Publisher publisher = Publisher.create(publisherSettings);

    // Start the publisher client.
    publisher.startAsync().awaitRunning();

    // Publish a few messages at one-minute interval.
    for (int i = 0; i < 6; i++) {
      String message = "message-" + i;
      ByteString data = ByteString.copyFromUtf8(message);
      PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();
      publisher.publish(pubsubMessage).get();
      TimeUnit.MINUTES.sleep(1);
    }

    // Stop the publisher client.
    publisher.stopAsync().awaitTerminated();

    // Check for output files.
    Page<Blob> blobs = storage.list(bucketName, Storage.BlobListOption.prefix(directoryPrefix));

    int numFiles = 0;
    // Check the content of the output files.
    for (Blob blob : blobs.iterateAll()) {
      String content = new String(blob.getContent(), StandardCharsets.UTF_8);
      System.out.println("Has content: " + content);
      Assert.assertTrue(content.contains("message-"));
      // Increment the count if the file has the desired content.
      numFiles += 1;
    }

    // Expect at least one file of desired output.
    Assert.assertTrue(numFiles > 0);
  }
}
