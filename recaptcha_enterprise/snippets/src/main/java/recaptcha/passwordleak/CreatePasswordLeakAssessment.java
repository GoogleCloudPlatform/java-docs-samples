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

package passwordleak;

// [START recaptcha_enterprise_password_leak_verification]

import com.google.cloud.recaptcha.passwordcheck.PasswordCheckResult;
import com.google.cloud.recaptcha.passwordcheck.PasswordCheckVerification;
import com.google.cloud.recaptcha.passwordcheck.PasswordCheckVerifier;
import com.google.cloud.recaptchaenterprise.v1.RecaptchaEnterpriseServiceClient;
import com.google.protobuf.ByteString;
import com.google.recaptchaenterprise.v1.Assessment;
import com.google.recaptchaenterprise.v1.CreateAssessmentRequest;
import com.google.recaptchaenterprise.v1.PrivatePasswordLeakVerification;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.bouncycastle.util.encoders.Base64;

public class CreatePasswordLeakAssessment {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException {
    // TODO(developer): Replace these variables before running the sample.
    // Google Cloud Project ID.
    String projectID = "project-id";

    // Username and password to be checked for credential breach.
    String username = "username";
    String password = "password";

    checkPasswordLeak(projectID, username, password);
  }

  /*
   * Detect password leaks and breached credentials to prevent account takeovers
   * (ATOs) and credential stuffing attacks.
   * For more information, see:
   * https://cloud.google.com/recaptcha-enterprise/docs/check-passwords and
   * https://security.googleblog.com/2019/02/protect-your-accounts-from-data.html

   * Steps:
   * 1. Use the 'create' method to hash and Encrypt the hashed username and
   * password.
   * 2. Send the hash prefix (26-bit) and the encrypted credentials to create
   * the assessment.(Hash prefix is used to partition the database.)
   * 3. Password leak assessment returns a list of encrypted credential hashes to
   * be compared with the decryption of the returned re-encrypted credentials.
   * Create Assessment also sends back re-encrypted credentials.
   * 4. The re-encrypted credential is then locally verified to see if there is
   * a match in the database.
   *
   * To perform hashing, encryption and verification (steps 1, 2 and 4),
   * reCAPTCHA Enterprise provides a helper library in Java.
   * See, https://github.com/GoogleCloudPlatform/java-recaptcha-password-check-helpers

   * If you want to extend this behavior to your own implementation/ languages,
   * make sure to perform the following steps:
   * 1. Hash the credentials (First 26 bits of the result is the
   * 'lookupHashPrefix')
   * 2. Encrypt the hash (result = 'encryptedUserCredentialsHash')
   * 3. Get back the PasswordLeak information from
   * reCAPTCHA Enterprise Create Assessment.
   * 4. Decrypt the obtained 'credentials.getReencryptedUserCredentialsHash()'
   * with the same key you used for encryption.
   * 5. Check if the decrypted credentials are present in
   * 'credentials.getEncryptedLeakMatchPrefixesList()'.
   * 6. If there is a match, that indicates a credential breach.
   */
  public static void checkPasswordLeak(
      String projectID, String username, String password)
      throws ExecutionException, InterruptedException, IOException {

    // Instantiate the java-password-leak-helper library to perform the cryptographic functions.
    PasswordCheckVerifier passwordLeak = new PasswordCheckVerifier();

    // Create the request to obtain the hash prefix and encrypted credentials.
    PasswordCheckVerification verification =
        passwordLeak.createVerification(username, password).get();

    byte[] lookupHashPrefix = Base64.encode(verification.getLookupHashPrefix());
    byte[] encryptedUserCredentialsHash = Base64.encode(
        verification.getEncryptedUserCredentialsHash());

    // Pass the credentials to the createPasswordLeakAssessment() to get back
    // the matching database entry for the hash prefix.
    PrivatePasswordLeakVerification credentials =
        createPasswordLeakAssessment(
            projectID,
            lookupHashPrefix,
            encryptedUserCredentialsHash);

    // Convert to appropriate input format.
    List<byte[]> leakMatchPrefixes =
        credentials.getEncryptedLeakMatchPrefixesList().stream()
            .map(x -> Base64.decode(x.toByteArray()))
            .collect(Collectors.toList());

    // Verify if the encrypted credentials are present in the obtained match list.
    PasswordCheckResult result =
        passwordLeak
            .verify(
                verification,
                Base64.decode(credentials.getReencryptedUserCredentialsHash().toByteArray()),
                leakMatchPrefixes)
            .get();

    // Check if the credential is leaked.
    boolean isLeaked = result.areCredentialsLeaked();
    System.out.printf("Is Credential leaked: %s", isLeaked);
  }

  // Create a reCAPTCHA Enterprise assessment.
  // Returns:  PrivatePasswordLeakVerification which contains
  // reencryptedUserCredentialsHash and credential breach database
  // whose prefix matches the lookupHashPrefix.
  private static PrivatePasswordLeakVerification createPasswordLeakAssessment(
      String projectID,
      byte[] lookupHashPrefix,
      byte[] encryptedUserCredentialsHash)
      throws IOException {
    try (RecaptchaEnterpriseServiceClient client = RecaptchaEnterpriseServiceClient.create()) {

      // Set the hashprefix and credentials hash.
      // Setting this will trigger the Password leak protection.
      PrivatePasswordLeakVerification passwordLeakVerification =
          PrivatePasswordLeakVerification.newBuilder()
              .setLookupHashPrefix(ByteString.copyFrom(lookupHashPrefix))
              .setEncryptedUserCredentialsHash(ByteString.copyFrom(encryptedUserCredentialsHash))
              .build();

      // Build the assessment request.
      CreateAssessmentRequest createAssessmentRequest =
          CreateAssessmentRequest.newBuilder()
              .setParent(String.format("projects/%s", projectID))
              .setAssessment(
                  Assessment.newBuilder()
                      // Set request for Password leak verification.
                      .setPrivatePasswordLeakVerification(passwordLeakVerification)
                      .build())
              .build();

      // Send the create assessment request.
      Assessment response = client.createAssessment(createAssessmentRequest);

      // Get the reCAPTCHA Enterprise score.
      float recaptchaScore = response.getRiskAnalysis().getScore();
      System.out.println("The reCAPTCHA score is: " + recaptchaScore);

      // Get the assessment name (id). Use this to annotate the assessment.
      String assessmentName = response.getName();
      System.out.println(
          "Assessment name: " + assessmentName.substring(assessmentName.lastIndexOf("/") + 1));

      return response.getPrivatePasswordLeakVerification();
    }
  }
}
// [END recaptcha_enterprise_password_leak_verification]
