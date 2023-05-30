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

package com.example.monitoring;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.cloud.monitoring.v3.NotificationChannelServiceClient;
import com.google.common.io.Files;
import com.google.monitoring.v3.AlertPolicy;
import com.google.monitoring.v3.NotificationChannel;
import com.google.monitoring.v3.ProjectName;
import io.grpc.StatusRuntimeException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.regex.Pattern;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for monitoring "AlertSample" sample. */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class AlertIT {
  private static String alertPolicyName;
  private static String alertPolicyId;
  private static String notificationChannelId;
  private static final String suffix = UUID.randomUUID().toString().substring(0, 8);
  private static final String testPolicyName = "test-policy" + suffix;
  private static final String policyFileName = "target/policyBackup.json";
  private static final String projectId = requireEnvVar();
  private ByteArrayOutputStream bout;
  private final PrintStream originalOut = System.out;

  private static String requireEnvVar() {
    String value = System.getenv("GOOGLE_CLOUD_PROJECT");
    assertNotNull(
        "Environment variable " + "GOOGLE_CLOUD_PROJECT" + " is required to perform these tests.",
        System.getenv("GOOGLE_CLOUD_PROJECT"));
    return value;
  }

  @BeforeClass
  public static void setupClass() throws IOException {
    // Create a test notification channel. Clean up not required because the channel
    // gets removed in `testReplaceChannels()`.
    try (NotificationChannelServiceClient client = NotificationChannelServiceClient.create()) {
      NotificationChannel notificationChannel =
          NotificationChannel.newBuilder()
              .setType("email")
              .putLabels("email_address", "java-docs-samples-testing@google.com")
              .build();
      NotificationChannel channel =
          client.createNotificationChannel(ProjectName.of(projectId), notificationChannel);
      String notificationChannelName = channel.getName();
      notificationChannelId =
          notificationChannelName.substring(notificationChannelName.lastIndexOf("/") + 1);
    }

    // Create a test alert policy.
    AlertPolicy alertPolicy = CreateAlertPolicy.createAlertPolicy(projectId, testPolicyName);
    alertPolicyName = alertPolicy.getName();
    alertPolicyId = alertPolicyName.substring(alertPolicyName.lastIndexOf('/') + 1);
  }

  @Before
  public void setUp() throws IOException {
    bout = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(bout);
    System.setOut(out);
  }

  @After
  public void tearDown() throws IOException {
    System.setOut(originalOut);
    bout.reset();
  }

  @AfterClass
  public static void tearDownClass() throws IOException {
    DeleteAlertPolicy.deleteAlertPolicy(alertPolicyName);
  }

  @Test
  public void testListPolicies() throws IOException {
    AlertSample.main("list");
    assertTrue(bout.toString().contains(testPolicyName));
  }

  @Test
  public void testBackupPolicies() throws IOException {
    AlertSample.main("backup", "-j", policyFileName);
    File backupFile = new File(policyFileName);
    assertTrue(backupFile.exists());
    String fileContents = String.join("\n", Files.readLines(backupFile, StandardCharsets.UTF_8));
    assertTrue(fileContents.contains(testPolicyName));
  }

  // TODO(b/78293034): Complete restore backup test when parse/unparse issue is figured out.
  @Test
  @Ignore
  public void testRestoreBackup() {}

  @Test
  public void testReplaceChannels() throws IOException {
    AlertSample.main("replace-channels", "-a", alertPolicyId, "-c", notificationChannelId);
    Pattern resultPattern = Pattern.compile("(?s).*Updated .*" + alertPolicyId);
    assertTrue(resultPattern.matcher(bout.toString()).find());
  }

  @Test
  public void testDisableEnablePolicies() throws IOException, InterruptedException {
    AlertSample.main("enable", "-d", "display_name=\"" + testPolicyName + "\"");

    // check the current state of policy to make sure
    // not to enable the policy that is already enabled.
    boolean isEnabled = bout.toString().contains("already");
    int maxAttempts = 10;
    int attempt = 0;
    int factor = 1;
    boolean retry = true;
    while (retry) {
      try {
        if (isEnabled) {
          AlertSample.main("disable", "-d", "display_name=\"" + testPolicyName + "\"");
          assertTrue(bout.toString().contains("disabled"));

          AlertSample.main("enable", "-d", "display_name=\"" + testPolicyName + "\"");
          assertTrue(bout.toString().contains("enabled"));
        } else {
          AlertSample.main("enable", "-d", "display_name=\"" + testPolicyName + "\"");
          assertTrue(bout.toString().contains("enabled"));

          AlertSample.main("disable", "-d", "display_name=\"" + testPolicyName + "\"");
          assertTrue(bout.toString().contains("disabled"));
        }
        retry = false;
      } catch (StatusRuntimeException e) {
        System.out.println("Error: " + e);
        System.out.println("Retrying...");
        Thread.sleep(2300L * factor);
        attempt += 1;
        factor += 1;
        if (attempt >= maxAttempts) {
          throw new RuntimeException("Retries failed.");
        }
      }
    }
  }
}
