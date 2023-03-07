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

package app;

import com.google.cloud.recaptchaenterprise.v1.RecaptchaEnterpriseServiceClient;
import com.google.recaptchaenterprise.v1.CreateKeyRequest;
import com.google.recaptchaenterprise.v1.DeleteKeyRequest;
import com.google.recaptchaenterprise.v1.Key;
import com.google.recaptchaenterprise.v1.KeyName;
import com.google.recaptchaenterprise.v1.ProjectName;
import com.google.recaptchaenterprise.v1.WebKeySettings;
import com.google.recaptchaenterprise.v1.WebKeySettings.IntegrationType;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Util {

  /**
   * Create reCAPTCHA Site key which binds a domain name to a unique key.
   *
   * @param projectID : Google Cloud Project ID.
   * @param domainName : Specify the domain name in which the reCAPTCHA should be activated.
   */
  public static String createSiteKey(String projectID, String domainName, IntegrationType keyType)
      throws IOException {
    try (RecaptchaEnterpriseServiceClient client = RecaptchaEnterpriseServiceClient.create()) {

      // Set the type of reCAPTCHA to be displayed.
      // For different types, see: https://cloud.google.com/recaptcha-enterprise/docs/keys
      Key scoreKey =
          Key.newBuilder()
              .setDisplayName("test-key-recaptcha-demosite-" +
                  UUID.randomUUID().toString().split("-")[0])
              .setWebSettings(
                  WebKeySettings.newBuilder()
                      .addAllowedDomains(domainName)
                      .setAllowAmpTraffic(false)
                      .setIntegrationType(keyType)
                      .build())
              .build();

      CreateKeyRequest createKeyRequest =
          CreateKeyRequest.newBuilder()
              .setParent(ProjectName.of(projectID).toString())
              .setKey(scoreKey)
              .build();

      // Get the name of the created reCAPTCHA site key.
      Key response = client.createKey(createKeyRequest);
      String keyName = response.getName();
      String recaptchaSiteKey = keyName.substring(keyName.lastIndexOf("/") + 1);
      System.out.println("reCAPTCHA Site key created successfully. Site Key: " + recaptchaSiteKey);
      return recaptchaSiteKey;
    }
  }

  /**
   * Delete the given reCAPTCHA site key present under the project ID.
   *
   * @param projectID: GCloud Project ID.
   * @param recaptchaSiteKey: Specify the site key to be deleted.
   */
  public static void deleteSiteKey(String projectID, String recaptchaSiteKey)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    try (RecaptchaEnterpriseServiceClient client = RecaptchaEnterpriseServiceClient.create()) {

      // Set the project ID and reCAPTCHA site key.
      DeleteKeyRequest deleteKeyRequest =
          DeleteKeyRequest.newBuilder()
              .setName(KeyName.of(projectID, recaptchaSiteKey).toString())
              .build();

      client.deleteKeyCallable().futureCall(deleteKeyRequest).get(5, TimeUnit.SECONDS);
      System.out.println("reCAPTCHA Site key successfully deleted !");
    }
  }

}
