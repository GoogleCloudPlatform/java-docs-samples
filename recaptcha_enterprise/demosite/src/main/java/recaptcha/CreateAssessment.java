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
import com.google.recaptchaenterprise.v1.TokenProperties;
import java.io.IOException;
import java.util.List;

public class CreateAssessment {

  class AssessmentResponse {
    List<ClassificationReason> reason;
    double recaptchaScore;
    String token;

    public AssessmentResponse(List<ClassificationReason> reason, double recaptchaScore, String token) {
      this.reason = reason;
      this.recaptchaScore = recaptchaScore;
      this.token = token;
    }
  }


  /**
   * Create an assessment to analyze the risk of an UI action. Assessment approach is the same for
   * both 'score' and 'checkbox' type recaptcha site keys.
   *  @param projectID : GCloud Project ID
   * @param recaptchaSiteKey : Site key obtained by registering a domain/app to use recaptcha
   *     services. (score/ checkbox type)
   * @param token : The token obtained from the client on passing the recaptchaSiteKey.
   * @param recaptchaAction : Action name corresponding to the token.
   * @return
   */
  public AssessmentResponse createAssessment(
      String projectID, String recaptchaSiteKey, String token, String recaptchaAction)
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

      // Check integrity of the response token.
      if (!checkTokenIntegrity(response.getTokenProperties(), recaptchaAction)) {
        return null;
      }

      return new AssessmentResponse(response.getRiskAnalysis().getReasonsList(), response.getRiskAnalysis().getScore(), token);
    }
  }

  private static boolean checkTokenIntegrity(
      TokenProperties tokenProperties, String recaptchaAction) {
    // Check if the token is valid.
    if (!tokenProperties.getValid()) {
      System.out.println(
          "The Create Assessment call failed because the token was: "
              + tokenProperties.getInvalidReason().name());
      return false;
    }

    // Check if the expected action was executed.
    if (!recaptchaAction.isEmpty() && !tokenProperties.getAction().equals(recaptchaAction)) {
      System.out.printf(
          "The action attribute in the reCAPTCHA tag '%s' does not match "
              + "the action '%s' you are expecting to score",
          tokenProperties.getAction(), recaptchaAction);
      return false;
    }
    return true;
  }

}
