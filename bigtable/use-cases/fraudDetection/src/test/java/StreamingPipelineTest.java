/*
 * Copyright 2022 Google LLC
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class StreamingPipelineTest {

  // The following variables are populated automatically by running Terraform.
  static String cbtInstanceID;
  static String cbtTableID;
  static String gcsBucket;
  static String pubsubInputTopic;
  static String pubsubOutputTopic;
  static String pubsubOutputSubscription;
  private static String projectID;

  @BeforeClass
  public static void beforeClass() throws InterruptedException, IOException {
    projectID = FraudDetectionTestUtil.requireEnv("GOOGLE_CLOUD_PROJECT");
    System.out.println("Project id = " + projectID);
    // Run terraform and populate all variables necessary for testing and assert
    // that the exit code is 0 (no errors).
    assertEquals(
        FraudDetectionTestUtil.runCommand(
            "terraform -chdir=terraform/ init"), 0);
    assertEquals(
        FraudDetectionTestUtil.runCommand(
            "terraform -chdir=terraform/ apply -auto-approve -var=project_id=" + projectID), 0);
  }

  @AfterClass
  public static void afterClass() throws IOException, InterruptedException {

    // Destroy all the resources we built before testing.
    assertEquals(
        FraudDetectionTestUtil.runCommand(
            "terraform -chdir=terraform/ destroy -auto-approve -var=project_id=" + projectID),
        0);
  }

  // Assert that the variables exported by Terraform are not null.
  @Test
  public void testTerraformSetup() {
    FraudDetectionTestUtil.requireVar(pubsubInputTopic);
    FraudDetectionTestUtil.requireVar(pubsubOutputTopic);
    FraudDetectionTestUtil.requireVar(pubsubOutputSubscription);
    FraudDetectionTestUtil.requireVar(gcsBucket);
    FraudDetectionTestUtil.requireVar(cbtInstanceID);
    FraudDetectionTestUtil.requireVar(cbtTableID);

    System.out.println("pubsubInputTopic= " + pubsubInputTopic);
    System.out.println("pubsubOutputTopic= " + pubsubOutputTopic);
    System.out.println("pubsubOutputSubscription= " + pubsubOutputSubscription);
    System.out.println("gcsBucket= " + gcsBucket);
    System.out.println("cbtInstanceID= " + cbtInstanceID);
    System.out.println("cbtTableID= " + cbtTableID);
  }

}
