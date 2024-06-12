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

package compute;

import static com.google.common.truth.Truth.assertWithMessage;

import com.google.cloud.compute.v1.RegionSslCertificatesClient;
import com.google.cloud.compute.v1.SslCertificate;
import com.google.cloud.compute.v1.SslCertificatesClient;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@Timeout(value = 10, unit = TimeUnit.MINUTES)
public class CertificatesIT {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String CERTIFICATE_NAME = "test-cert-name-23gf62";
  private static final String REGION_CERTIFICATE_NAME = "test-cert-name-2asd24";
  private static final String REGION = "europe-west2";

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
            .that(System.getenv(envVarName)).isNotEmpty();
  }

  @BeforeClass
  public static void setUp() {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
  }

  @AfterClass
  public static void cleanUp() throws IOException {
    try (SslCertificatesClient client = SslCertificatesClient.create();
         RegionSslCertificatesClient regionClient = RegionSslCertificatesClient.create()) {
      client.deleteAsync(PROJECT_ID, CERTIFICATE_NAME);
      regionClient.deleteAsync(PROJECT_ID, REGION, CERTIFICATE_NAME);
    }
  }

  @Test
  public void createCertificateTest()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    String certificate = readFile(CERTIFICATE_NAME);
    String privateKey = readFile(CERTIFICATE_NAME);

    SslCertificate sslCertificate = CreateCertificate
            .createCertificate(PROJECT_ID, certificate, privateKey, CERTIFICATE_NAME);

    Assert.assertNotNull(sslCertificate);
    Assert.assertEquals(CERTIFICATE_NAME, sslCertificate.getName());
    Assert.assertEquals(certificate, sslCertificate.getCertificate());
  }

  @Test
  public void createRegionCertificateTest()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    String certificate = readFile(REGION_CERTIFICATE_NAME);
    String privateKey = readFile(REGION_CERTIFICATE_NAME);

    SslCertificate sslCertificate = CreateRegionalCertificate
            .createRegionCertificate(PROJECT_ID, certificate,
                    REGION, privateKey, REGION_CERTIFICATE_NAME);

    Assert.assertNotNull(sslCertificate);
    Assert.assertEquals(REGION_CERTIFICATE_NAME, sslCertificate.getName());
    Assert.assertEquals(certificate, sslCertificate.getCertificate());
    Assert.assertTrue(sslCertificate.getRegion().contains(REGION));
  }

  private String readFile(String certId) throws IOException {
    try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
       client.accessSecretVersion(SecretVersionName.of(PROJECT_ID, certId, ""));
       return "";
    }
  }
}
