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

import static org.junit.Assert.assertTrue;

import com.google.common.base.Strings;
import com.google.common.io.Files;
import io.grpc.StatusRuntimeException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for monitoring "AlertSample" sample. */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class AlertIT {
  private static String testPolicyName = "test-policy";
  private static String policyFileName = "target/policyBackup.json";
  private static Pattern policyNameRegex =
      Pattern.compile(
          "alertPolicies/(?<alertid>.*)(?s).*notificationChannels/(?<channel>[a-zA-Z0-9]*)");
  private ByteArrayOutputStream bout;
  private final PrintStream originalOut = System.out;

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
  }

  @After
  public void tearDown() {
    System.setOut(originalOut);
    bout.reset();
  }

  @Test
  public void testListPolicies() throws IOException {
    AlertSample.main(new String[] {"list"});
    assertTrue(bout.toString().contains(testPolicyName));
  }

  @Test
  public void testBackupPolicies() throws IOException {
    AlertSample.main(new String[] {"backup", "-j", policyFileName});
    File backupFile = new File(policyFileName);
    assertTrue(backupFile.exists());
    String fileContents = String.join("\n", Files.readLines(backupFile, StandardCharsets.UTF_8));
    assertTrue(fileContents.contains("test-policy"));
  }

  // TODO(b/78293034): Complete restore backup test when parse/unparse issue is figured out.
  @Test
  @Ignore
  public void testRestoreBackup() throws IOException {}

  @Test
  public void testReplaceChannels() throws IOException {
    // Get a test policy name for the project.
    AlertSample.main(new String[] {"list"});
    Matcher matcher = policyNameRegex.matcher(bout.toString());
    assertTrue(matcher.find());
    String alertId = matcher.group("alertid");
    String channel = matcher.group("channel");
    Assert.assertFalse(Strings.isNullOrEmpty(alertId));
    AlertSample.main(new String[] {"replace-channels", "-a", alertId, "-c", channel});
    Pattern resultPattern = Pattern.compile("(?s).*Updated .*/alertPolicies/" + alertId);
    assertTrue(resultPattern.matcher(bout.toString()).find());
  }

  @Test
  public void testDisableEnablePolicies() throws IOException, InterruptedException {
    AlertSample.main(new String[] {"enable", "-d", "display_name='test-policy'"});

    // check the current state of policy to make sure
    // not to enable the policy that is already enabled.
    boolean isEnabled = bout.toString().contains("already");
    int maxAttempts = 10;
    int attempt = 0;
    int factor = 1;
    Boolean retry = true;
    while (retry) {
      try {
        if (isEnabled) {
          AlertSample.main(new String[] {"disable", "-d", "display_name='test-policy'"});
          assertTrue(bout.toString().contains("disabled"));

          AlertSample.main(new String[] {"enable", "-d", "display_name='test-policy'"});
          assertTrue(bout.toString().contains("enabled"));
        } else {
          AlertSample.main(new String[] {"enable", "-d", "display_name='test-policy'"});
          assertTrue(bout.toString().contains("enabled"));

          AlertSample.main(new String[] {"disable", "-d", "display_name='test-policy'"});
          assertTrue(bout.toString().contains("disabled"));
        }
        retry = false;
      } catch (StatusRuntimeException e) {
        System.out.println("Error: " + e.toString());
        System.out.println("Retrying...");
        Thread.sleep(2300 * factor);
        attempt += 1;
        factor += 1;
        if (attempt >= maxAttempts) {
          throw new RuntimeException("Retries failed.");
        }
      }
    }
  }
}
