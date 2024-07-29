/*
 * Copyright 2024 Google LLC
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

package secretmanager.regionalsamples;

import static org.junit.Assert.assertEquals;

import com.google.cloud.secretmanager.v1.DeleteSecretRequest;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretManagerServiceSettings;
import com.google.cloud.secretmanager.v1.SecretName;
import com.google.cloud.secretmanager.v1.SecretPayload;
import com.google.common.base.Strings;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.UUID;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Integration (system) tests for {@link Quickstart}. */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class QuickstartIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String LOCATION_ID = "us-central1";
  private static final String SECRET_ID = "java-quickstart-" + UUID.randomUUID().toString();

  @BeforeClass
  public static void beforeAll() throws Exception {
    Assert.assertFalse("missing GOOGLE_CLOUD_PROJECT", Strings.isNullOrEmpty(PROJECT_ID));
    Assert.assertFalse("missing GOOGLE_CLOUD_PROJECT_LOCATION", Strings.isNullOrEmpty(LOCATION_ID));
  }

  @AfterClass
  public static void afterAll() throws Exception {
    String apiEndpoint = String.format("secretmanager.%s.rep.googleapis.com:443", LOCATION_ID);
    SecretManagerServiceSettings secretManagerServiceSettings =
        SecretManagerServiceSettings.newBuilder().setEndpoint(apiEndpoint).build();

    try (SecretManagerServiceClient regionalClient =
        SecretManagerServiceClient.create(secretManagerServiceSettings)) {

      // Delete the secret created by regional quickstart
      SecretName name = SecretName.ofProjectLocationSecretName(PROJECT_ID, LOCATION_ID, SECRET_ID);
      DeleteSecretRequest deleteRequest =
          DeleteSecretRequest.newBuilder().setName(name.toString()).build();

      regionalClient.deleteSecret(deleteRequest);
    }
  }

  @Test
  public void regional_quickstart_test() throws Exception {
    PrintStream originalOut = System.out;
    ByteArrayOutputStream redirected = new ByteArrayOutputStream();

    System.setOut(new PrintStream(redirected));

    SecretName.ofProjectLocationSecretName(PROJECT_ID, LOCATION_ID, SECRET_ID);
    
    try {
      SecretPayload payload = 
          new RegionalQuickstart().regionalQuickstart(PROJECT_ID, LOCATION_ID, SECRET_ID);

      assertEquals("Secret data", payload.getData().toStringUtf8());
    } finally {
      System.setOut(originalOut);
    }
  }
}
