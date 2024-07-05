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

// [START compute_certificate_create_regional]

import com.google.cloud.compute.v1.InsertRegionSslCertificateRequest;
import com.google.cloud.compute.v1.RegionSslCertificatesClient;
import com.google.cloud.compute.v1.SslCertificate;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateRegionalCertificate {
  public static void main(String[] args)
          throws IOException, InterruptedException, ExecutionException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Google Cloud project you want to use.
    String project = "your-project-id";
    // The certificate you want to create in your project.
    String certificate = "your-certificate";
    // The private key you used to sign the certificate with.
    String privateKey = "your-private-key";
    // Name for the certificate once it's created in your project.
    String certificateName = "your-certificate-name";
    // Name of the region you want to use.
    String region = "your-region";

    createRegionCertificate(project, certificate, region, privateKey, certificateName);
  }

  // Create a regional SSL self-signed certificate within your Google Cloud project.
  public static SslCertificate createRegionCertificate(String project, String certificate,
                                                       String region, String privateKey,
                                                       String certificateName)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (RegionSslCertificatesClient client = RegionSslCertificatesClient.create()) {
      SslCertificate certificateResource = SslCertificate.newBuilder()
              .setCertificate(certificate)
              .setPrivateKey(privateKey)
              .setName(certificateName)
              .build();

      InsertRegionSslCertificateRequest request = InsertRegionSslCertificateRequest.newBuilder()
              .setProject(project)
              .setRegion(region)
              .setSslCertificateResource(certificateResource)
              .build();

      client.insertCallable().futureCall(request).get(60, TimeUnit.SECONDS);

      // Wait for server update
      TimeUnit.SECONDS.sleep(1);

      SslCertificate sslCert = client.get(project, region, certificateName);

      System.out.printf("Regional cert '%s' has been created successfully", sslCert.getName());

      return sslCert;
    }
  }
}
// [END compute_certificate_create_regional]