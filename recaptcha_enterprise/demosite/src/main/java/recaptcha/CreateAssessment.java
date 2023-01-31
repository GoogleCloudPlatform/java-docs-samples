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

package recaptcha;

import com.google.cloud.recaptchaenterprise.v1.RecaptchaEnterpriseServiceClient;
import com.google.recaptchaenterprise.v1.Assessment;
import com.google.recaptchaenterprise.v1.CreateAssessmentRequest;
import com.google.recaptchaenterprise.v1.Event;
import com.google.recaptchaenterprise.v1.ProjectName;
import com.google.recaptchaenterprise.v1.RiskAnalysis.ClassificationReason;
import java.io.IOException;
import java.util.List;

public class CreateAssessment {

  static class AssessmentResponse {

    double recaptchaScore;
    List<ClassificationReason> reason;

    public AssessmentResponse(double recaptchaScore, List<ClassificationReason> reason) {
      this.recaptchaScore = recaptchaScore;
      this.reason = reason;
    }
  }

  /**
   * Create an assessment to analyze the risk of an UI action.
   *
   * @param projectID : GCloud Project ID
   * @param recaptchaSiteKey : Site key obtained by registering a domain/app to use recaptcha
   * services. (score/ checkbox type)
   * @param recaptchaAction : Action name corresponding to the token.
   * @param token : The token obtained from the client on passing the recaptchaSiteKey.
   */
  public static AssessmentResponse createAssessment(
      String projectID, String recaptchaSiteKey, String recaptchaAction, String token)
      throws IOException {
    try (RecaptchaEnterpriseServiceClient client = RecaptchaEnterpriseServiceClient.create()) {

      // Set the properties of the event to be tracked.
      Event event = Event.newBuilder().setSiteKey(recaptchaSiteKey).setToken(token).build();

      // Build the assessment request.
      CreateAssessmentRequest createAssessmentRequest =
          CreateAssessmentRequest.newBuilder()
              .setParent(ProjectName.of(projectID).toString())
              .setAssessment(Assessment.newBuilder().setEvent(event).build())
              .build();

      Assessment response = client.createAssessment(createAssessmentRequest);

      // Check if the token is valid.
      if (!response.getTokenProperties().getValid()) {
        throw new Error(
            "The Create Assessment call failed because the token was invalid for the following reasons: "
                + response.getTokenProperties().getInvalidReason().name());
      }

      // Check if the expected action was executed.
      if (!recaptchaAction.isEmpty() && !response.getTokenProperties().getAction()
          .equals(recaptchaAction)) {
        throw new Error(
            "The action attribute in your reCAPTCHA tag does not match the action you are expecting to score. Please check your action attribute !");
      }

      // Return the risk score and the reason(s).
      return new AssessmentResponse(response.getRiskAnalysis().getScore(),
          response.getRiskAnalysis().getReasonsList());
    }
  }
}
