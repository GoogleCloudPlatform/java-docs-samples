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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
  private static final String PRIVATE_KEY = System.getenv("PRIVATE_KEY_SELFSIGNED_CERT");
  private static final String CERTIFICATE_NAME =
          "cert-name-" + UUID.randomUUID().toString().substring(0, 8);
  private static final String REGION_CERTIFICATE_NAME =
          "cert-name-" + UUID.randomUUID().toString().substring(0, 8);
  private static final String CERTIFICATE_FILE = "resources/certificate.pem";
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
    requireEnvVar("PRIVATE_KEY_SELFSIGNED_CERT");
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
    String certificate = readFile(CERTIFICATE_FILE);

    SslCertificate sslCertificate = CreateCertificate
            .createCertificate(PROJECT_ID, certificate, PRIVATE_KEY, CERTIFICATE_NAME);

    Assert.assertNotNull(sslCertificate);
    Assert.assertEquals(CERTIFICATE_NAME, sslCertificate.getName());
    Assert.assertEquals(certificate, sslCertificate.getCertificate());
    Assert.assertNotNull(sslCertificate.getPrivateKey());
  }

  @Test
  public void createRegionCertificateTest()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    String certificate = readFile(CERTIFICATE_FILE);

    SslCertificate sslCertificate = CreateRegionalCertificate
            .createRegionCertificate(PROJECT_ID, certificate,
                    REGION, PRIVATE_KEY, REGION_CERTIFICATE_NAME);

    Assert.assertNotNull(sslCertificate);
    Assert.assertEquals(REGION_CERTIFICATE_NAME, sslCertificate.getName());
    Assert.assertEquals(certificate, sslCertificate.getCertificate());
    Assert.assertTrue(sslCertificate.getRegion().contains(REGION));
    Assert.assertNotNull(sslCertificate.getPrivateKey());
  }

  private String readFile(String path) throws IOException {
    File file = new File(path);
    return Files.readString(file.toPath());
  }
}
