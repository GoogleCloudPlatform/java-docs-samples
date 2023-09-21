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

package recaptcha.mfa;

// [START recaptcha_enterprise_mfa_assessment]

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
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class CreateMfaAssessment {

  public static void main(String[] args)
      throws IOException, NoSuchAlgorithmException, InvalidKeyException {
    // TODO(developer): Replace these variables before running the sample.
    // Google Cloud Project ID.
    String projectID = "PROJECT_ID";
    // Site key obtained by registering a domain/app to use recaptcha services.
    // See, https://cloud.google.com/recaptcha-enterprise/docs/instrument-web-pages
    String recaptchaSiteKey = "SITE_KEY";
    // The token obtained from the client on passing the recaptchaSiteKey.
    // To get the token, integrate the recaptchaSiteKey with frontend. See,
    // https://cloud.google.com/recaptcha-enterprise/docs/instrument-web-pages#frontend_integration_score
    String token = "TOKEN";
    // The action name corresponding to the token.
    String recaptchaAction = "ACTION";
    // Email id of the user to trigger the MFA challenge.
    String emailId = "foo@bar.com";
    // Phone number of the user to trigger the MFA challenge.
    String phoneNumber = "+11111111111";

    // Create hashedAccountId from user identifier.
    // It's a one-way hash of the user identifier: HMAC SHA256 + salt.
    String userIdentifier = "Alice Bob";
    // Change this to a secret not shared with Google.
    final String HMAC_KEY = "SOME_INTERNAL_UNSHARED_KEY";
    // Get instance of Mac object implementing HmacSHA256, and initialize it with the above
    // secret key.
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(HMAC_KEY.getBytes(StandardCharsets.UTF_8),
        "HmacSHA256"));
    byte[] hashBytes = mac.doFinal(userIdentifier.getBytes(StandardCharsets.UTF_8));
    ByteString hashedAccountId = ByteString.copyFrom(hashBytes);

    createMfaAssessment(projectID, recaptchaSiteKey, token, recaptchaAction, hashedAccountId,
        emailId, phoneNumber);
  }

  // Creates an assessment to obtain Multi-Factor Authentication result.
  // If the result is unspecified, sends the request token to the caller to initiate MFA challenge.
  // See, https://cloud.google.com/recaptcha-enterprise/docs/integrate-account-verification#understanding_the_configuration_process_of_mfa
  public static void createMfaAssessment(
      String projectID, String recaptchaSiteKey, String token, String recaptchaAction,
      ByteString hashedAccountId, String emailId, String phoneNumber)
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
          .setHashedAccountId(hashedAccountId)
          .build();

      // Set the email address and the phone number to trigger/ verify the MFA challenge.
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

      // Check integrity of the response.
      if (!checkResponseIntegrity(response.getTokenProperties(), recaptchaAction)) {
        throw new Error("Failed to verify token integrity.");
      }

      String assessmentName = response.getName();
      System.out.println("Assessment name: " + assessmentName.substring(assessmentName.lastIndexOf("/") + 1));
      // If the result is unspecified, send the request token to trigger MFA in the client.
      // You can choose to send both the email and phone number's request token.
      if (response.getAccountVerification().getLatestVerificationResult()
          == Result.RESULT_UNSPECIFIED) {
        System.out.println("MFA result: Result unspecified. Triggering MFA challenge.");
        // Send the request token to the client. The token is valid for 15 minutes.
        // System.out.println(response.getAccountVerification().getEndpoints(0).getRequestToken());
      }

      // If the result is not unspecified, return the result.
      System.out.printf("MFA result: %s%n",
          response.getAccountVerification().getLatestVerificationResult());
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
// [END recaptcha_enterprise_mfa_assessment]