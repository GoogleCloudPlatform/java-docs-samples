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

package vtwo;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.cloud.securitycenter.v2.NotificationConfig;
import com.google.cloud.testing.junit4.MultipleAttemptsRule;
import com.google.pubsub.v1.ProjectTopicName;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import vtwo.notifications.CreateNotification;
import vtwo.notifications.DeleteNotification;
import vtwo.notifications.GetNotification;
import vtwo.notifications.ListNotification;
import vtwo.notifications.UpdateNotification;

// Test v2 Notification samples.
@RunWith(JUnit4.class)
public class NotificationIT {

  // TODO: Replace the below variables.
  private static final String PROJECT_ID = System.getenv("SCC_PROJECT_ID");
  private static final String LOCATION = "global";
  private static final String NOTIFICATION_RULE_CREATE =
      "random-notification-id-" + UUID.randomUUID();
  private static final String NOTIFICATION_TOPIC = "test-topic-" + UUID.randomUUID();
  private static final int MAX_ATTEMPT_COUNT = 3;
  private static final int INITIAL_BACKOFF_MILLIS = 120000; // 2 minutes
  private static ByteArrayOutputStream stdOut;

  @Rule
  public final MultipleAttemptsRule multipleAttemptsRule = new MultipleAttemptsRule(
      MAX_ATTEMPT_COUNT,
      INITIAL_BACKOFF_MILLIS);

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName))
        .isNotEmpty();
  }

  @BeforeClass
  public static void setUp() throws IOException, InterruptedException {
    final PrintStream out = System.out;
    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));

    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("SCC_PROJECT_ID");

    // Create pubsub topic.
    createPubSubTopic(PROJECT_ID, NOTIFICATION_TOPIC);

    // Create notification rules.
    NotificationConfig result = CreateNotification.createNotificationConfig(PROJECT_ID, LOCATION,
        NOTIFICATION_TOPIC, NOTIFICATION_RULE_CREATE);
    System.out.printf("NotificationConfig: " + result.getName() + " " + result.getDescription());

    stdOut = null;
    System.setOut(out);
  }

  @AfterClass
  public static void cleanUp() throws IOException {
    final PrintStream out = System.out;
    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));

    DeleteNotification.deleteNotificationConfig(PROJECT_ID, LOCATION, NOTIFICATION_RULE_CREATE);
    assertThat(stdOut.toString()).contains(
        "Deleted Notification config: " + NOTIFICATION_RULE_CREATE);

    deletePubSubTopic(PROJECT_ID, NOTIFICATION_TOPIC);

    stdOut = null;
    System.setOut(out);
  }

  @Before
  public void beforeEach() {
    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
  }

  @After
  public void afterEach() {
    stdOut = null;
    System.setOut(null);
  }

  @Test
  public void testGetNotificationRule() throws IOException {
    NotificationConfig notificationConfig = GetNotification.getNotificationConfig(PROJECT_ID,
        LOCATION, NOTIFICATION_RULE_CREATE);

    assertThat(notificationConfig.getName()).contains(NOTIFICATION_RULE_CREATE);
  }

  @Test
  public void testListNotificationRules() throws IOException {
    ListNotification.listNotificationConfigs(PROJECT_ID, LOCATION);

    assertThat(stdOut.toString()).contains(NOTIFICATION_TOPIC);
  }

  @Test
  public void testUpdateNotificationRule() throws IOException {
    UpdateNotification.updateNotificationConfig(PROJECT_ID, LOCATION, NOTIFICATION_TOPIC,
        NOTIFICATION_RULE_CREATE);
    NotificationConfig notificationConfig = GetNotification.getNotificationConfig(PROJECT_ID,
        LOCATION, NOTIFICATION_RULE_CREATE);

    assertThat(notificationConfig.getDescription()).contains("updated description");
  }

  public static void createPubSubTopic(String projectId, String topicId) throws IOException {
    ProjectTopicName topicName = ProjectTopicName.of(projectId, topicId);
    TopicAdminClient client = TopicAdminClient.create();
    client.createTopic(topicName);
  }

  public static void deletePubSubTopic(String projectId, String topicId) throws IOException {
    ProjectTopicName topicName = ProjectTopicName.of(projectId, topicId);
    TopicAdminClient client = TopicAdminClient.create();
    client.deleteTopic(topicName);
  }

}