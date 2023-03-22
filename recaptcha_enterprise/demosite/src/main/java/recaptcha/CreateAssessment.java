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

package recaptcha;

import com.google.cloud.recaptchaenterprise.v1.RecaptchaEnterpriseServiceClient;
import com.google.recaptchaenterprise.v1.Assessment;
import com.google.recaptchaenterprise.v1.CreateAssessmentRequest;
import com.google.recaptchaenterprise.v1.Event;
import com.google.recaptchaenterprise.v1.ProjectName;
import java.util.HashMap;

public class CreateAssessment {

  /**
   * Create an assessment to analyze the risk of a UI action.
   *
   * @param projectID : Google Cloud Project ID
   * @param recaptchaSiteKey : Site key obtained by registering a domain/app to use recaptcha
   *     services. (score/ checkbox type)
   * @param token : The token obtained from the client on passing the recaptchaSiteKey.
   * @param recaptchaAction : Action name corresponding to the token.
   * @return JSONObject that contains a risk score and verdict if the action was executed by a
   *     human.
   */
  public static Assessment createAssessment(
      String projectID, String recaptchaSiteKey, String token, String recaptchaAction)
      throws Exception {
    // Sample threshold score for classification of bad / not bad action. The threshold score
    // can be used to trigger secondary actions like MFA.
    double sampleThresholdScore = 0.50;

    // <!-- ATTENTION: reCAPTCHA Example (Server Part 2/2) Starts -->
    try (RecaptchaEnterpriseServiceClient client = RecaptchaEnterpriseServiceClient.create()) {
      // Set the properties of the event to be tracked.
      Event event = Event.newBuilder()
          .setSiteKey(recaptchaSiteKey)
          .setToken(token)
          .build();

      // Build the assessment request.
      CreateAssessmentRequest createAssessmentRequest =
          CreateAssessmentRequest.newBuilder()
              .setParent(ProjectName.of(projectID).toString())
              .setAssessment(Assessment.newBuilder().setEvent(event).build())
              .build();

      Assessment response = client.createAssessment(createAssessmentRequest);
      // <!-- ATTENTION: reCAPTCHA Example (Server Part 2/2) Ends -->

      return response;
    }
  }
}
