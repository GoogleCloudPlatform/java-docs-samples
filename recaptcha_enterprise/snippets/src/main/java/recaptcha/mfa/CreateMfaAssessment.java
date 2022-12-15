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

package recaptcha.mfa;

import com.google.cloud.recaptchaenterprise.v1.RecaptchaEnterpriseServiceClient;
import com.google.protobuf.ByteString;
import com.google.recaptchaenterprise.v1.AccountVerificationInfo;
import com.google.recaptchaenterprise.v1.AccountVerificationInfo.Result;
import com.google.recaptchaenterprise.v1.Assessment;
import com.google.recaptchaenterprise.v1.CreateAssessmentRequest;
import com.google.recaptchaenterprise.v1.EndpointVerificationInfo;
import com.google.recaptchaenterprise.v1.Event;
import com.google.recaptchaenterprise.v1.ProjectName;
import com.google.recaptchaenterprise.v1.TokenProperties;
import java.io.IOException;

public class CreateMfaAssessment {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectID = "PROJECT_ID";
    String recaptchaSiteKey = "SITE_KEY";
    String token = "TOKEN";
    String recaptchaAction = "ACTION";
    String hashedAccountId = "BP3ptt00D9W7UMzFmsPdEjNH3Chpi8bo40R6YW2b";
    String emailId = "foo@bar.com";
    String phoneNumber = "+11111111111";

    createMfaAssessment(projectID, recaptchaSiteKey, token, recaptchaAction, hashedAccountId,
        emailId, phoneNumber);
  }

  // MFA contains a series of workflow steps to be completed.
  // 1. Trigger the usual recaptcha challenge in the UI and get the token. In addition to the token,
  // supply the hashedAccountId, email and/or phone number of the user.
  // 2. Based on the recommended action, choose if you should trigger the MFA challenge.
  // 3. If you decide to trigger MFA, send the requestToken back to the UI.
  // 4. In the UI, call "grecaptcha.enterprise.challengeAccount" and pass the sitekey, request token,
  // and container id to render the challenge.
  // 5. The result from this promise is sent to another call "verificationHandle.verifyAccount(pin)"
  // This call verifies if the pin has been entered correct.
  // 6. The result from this call is sent to the backend to create a MFA assessment again.
  // The result of this assessment will tell if the MFA challenge has been successful.
  public static void createMfaAssessment(
      String projectID, String recaptchaSiteKey, String token, String recaptchaAction,
      String hashedAccountId,
      String emailId, String phoneNumber)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the `client.close()` method on the client to safely
    // clean up any remaining background resources.
    try (RecaptchaEnterpriseServiceClient client = RecaptchaEnterpriseServiceClient.create()) {

      // Set the properties of the event to be tracked.
      Event event = Event.newBuilder()
          .setSiteKey(recaptchaSiteKey)
          .setToken(token)
          .setHashedAccountId(ByteString.fromHex(hashedAccountId))
          .build();

      AccountVerificationInfo accountVerificationInfo = AccountVerificationInfo.newBuilder()
          .addEndpoints(EndpointVerificationInfo.newBuilder()
              .setEmailAddress(emailId)
              .setPhoneNumber(phoneNumber)
              .build())
          .build();

      // Build the assessment request.
      CreateAssessmentRequest createAssessmentRequest =
          CreateAssessmentRequest.newBuilder()
              .setParent(ProjectName.of(projectID).toString())
              .setAssessment(Assessment.newBuilder()
                  .setEvent(event)
                  .setAccountVerification(accountVerificationInfo)
                  .build())
              .build();

      Assessment response = client.createAssessment(createAssessmentRequest);

      System.out.println(response);

      // Check integrity of the response.
      if (!checkResponseIntegrity(response.getTokenProperties(), recaptchaAction)) {
        return;
      }

      // Check if the recommended action was set in account defender.
      // If set, send the request token.
      Result result = response.getAccountVerification().getLatestVerificationResult();
      if (result == Result.RESULT_UNSPECIFIED && response.getAccountDefenderAssessment()
          .isInitialized()) {
        // Send the request token for assessment.
        response.getAccountVerification().getEndpoints(0).getRequestToken();
        return;
      }

      System.out.println("MFA result:" + result);
    }
  }

  private static boolean checkResponseIntegrity(
      TokenProperties tokenProperties, String recaptchaAction) {
    // Check if the token is valid.
    if (!tokenProperties.getValid()) {
      System.out.println(
          "The Account Defender Assessment call failed because the token was: "
              + tokenProperties.getInvalidReason().name());
      return false;
    }

    // Check if the expected action was executed.
    if (!tokenProperties.getAction().equals(recaptchaAction)) {
      System.out.printf(
          "The action attribute in the reCAPTCHA tag '%s' does not match "
              + "the action '%s' you are expecting to score",
          tokenProperties.getAction(), recaptchaAction);
      return false;
    }
    return true;
  }
}
