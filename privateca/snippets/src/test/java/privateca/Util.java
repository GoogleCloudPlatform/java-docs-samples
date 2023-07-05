/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package privateca;

import com.google.cloud.security.privateca.v1.CaPool;
import com.google.cloud.security.privateca.v1.CertificateAuthority;
import com.google.cloud.security.privateca.v1.CertificateAuthority.State;
import com.google.cloud.security.privateca.v1.CertificateAuthorityServiceClient;
import com.google.cloud.security.privateca.v1.CertificateAuthorityServiceClient.ListCaPoolsPagedResponse;
import com.google.cloud.security.privateca.v1.DeleteCaPoolRequest;
import com.google.cloud.security.privateca.v1.DeleteCertificateAuthorityRequest;
import com.google.cloud.security.privateca.v1.DisableCertificateAuthorityRequest;
import com.google.cloud.security.privateca.v1.ListCaPoolsRequest;
import com.google.cloud.security.privateca.v1.LocationName;
import com.google.protobuf.Timestamp;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class Util {

  private static final int DELETION_THRESHOLD_TIME_HOURS = 24;

  // Delete Ca pools which starts with the given prefixToDelete.
  public static void cleanUpCaPool(String projectId,
      String location)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {

    try (CertificateAuthorityServiceClient client = CertificateAuthorityServiceClient.create()) {

      // List Ca pools
      for (CaPool caPool : listCaPools(projectId, location).iterateAll()) {
        deleteCertificateAuthority(caPool.getName());
        DeleteCaPoolRequest deleteCaPoolRequest =
            DeleteCaPoolRequest.newBuilder().setName(caPool.getName()).build();

        client.deleteCaPoolCallable().futureCall(deleteCaPoolRequest).get(5, TimeUnit.MINUTES);
      }
    }
  }

  public static ListCaPoolsPagedResponse listCaPools(String project,
      String location) throws IOException {
    try (CertificateAuthorityServiceClient certificateAuthorityServiceClient =
        CertificateAuthorityServiceClient.create()) {

      LocationName locationName =
          LocationName.newBuilder().setProject(project).setLocation(location).build();

      ListCaPoolsRequest request = ListCaPoolsRequest.newBuilder()
          .setParent(locationName.toString())
          .build();

      return
          certificateAuthorityServiceClient.listCaPools(request);
    }
  }

  public static void deleteCertificateAuthority(String caPoolName)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    try (CertificateAuthorityServiceClient certificateAuthorityServiceClient =
        CertificateAuthorityServiceClient.create()) {
      for (CertificateAuthority certificateAuthority :
          certificateAuthorityServiceClient.listCertificateAuthorities(caPoolName).iterateAll()) {
        // Check if the CA was created before the threshold time.
        if (!isCreatedBeforeThresholdTime(certificateAuthority.getCreateTime())) {
          continue;
        }

        // Check if the CA is enabled.
        State caState =
            certificateAuthorityServiceClient
                .getCertificateAuthority(certificateAuthority.getName())
                .getState();
        if (caState == State.ENABLED) {
          disableCertificateAuthority(certificateAuthority.getName());
        }

        DeleteCertificateAuthorityRequest deleteCertificateAuthorityRequest =
            DeleteCertificateAuthorityRequest.newBuilder()
                .setName(certificateAuthority.getName())
                .setIgnoreActiveCertificates(true)
                .setSkipGracePeriod(true)
                .build();

        certificateAuthorityServiceClient
            .deleteCertificateAuthorityCallable()
            .futureCall(deleteCertificateAuthorityRequest).get(5, TimeUnit.MINUTES);
      }
    }
  }

  public static void disableCertificateAuthority(String caName)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    try (CertificateAuthorityServiceClient client = CertificateAuthorityServiceClient.create()) {
      DisableCertificateAuthorityRequest disableCertificateAuthorityRequest =
          DisableCertificateAuthorityRequest.newBuilder()
              .setName(caName)
              .build();

      // Disable the Certificate Authority.
      client
          .disableCertificateAuthorityCallable()
          .futureCall(disableCertificateAuthorityRequest)
          .get(5, TimeUnit.MINUTES);
    }
  }

  public static boolean isCreatedBeforeThresholdTime(Timestamp timestamp) {
    Instant instant = Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    return instant
        .isBefore(Instant.now().minus(DELETION_THRESHOLD_TIME_HOURS, ChronoUnit.HOURS));
  }

  /**
   * @return a region (e.g. "us-west1") that is randomly selected from uswest-* regions.
   * This distributes the testing workload across regions to avoid exceeding quotas.
   */
  public static String getRegion() {
    String regionPrefix = "us-west";
    int numRegions = 4;  // 4 available us-west regions
    int selectedRegion = ThreadLocalRandom.current().nextInt(1, numRegions + 1);
    return regionPrefix + String.valueOf(selectedRegion);
  }
}
