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
import com.google.cloud.security.privateca.v1.CertificateAuthorityServiceClient;
import com.google.cloud.security.privateca.v1.CertificateAuthorityServiceClient.ListCaPoolsPagedResponse;
import com.google.cloud.security.privateca.v1.DeleteCaPoolRequest;
import com.google.cloud.security.privateca.v1.DeleteCertificateAuthorityRequest;
import com.google.cloud.security.privateca.v1.ListCaPoolsRequest;
import com.google.cloud.security.privateca.v1.LocationName;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Util {

  // Delete Ca pools which starts with the given prefixToDelete.
  public static void cleanUpCaPool(String prefixToDelete, String projectId,
      String location)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {

    try (CertificateAuthorityServiceClient client = CertificateAuthorityServiceClient.create()) {

      // Filter CA pools with the prefix
      for (CaPool caPool : filterCaPools(prefixToDelete, projectId, location).iterateAll()) {
        deleteCertificateAuthority(caPool.getName());
        DeleteCaPoolRequest deleteCaPoolRequest =
            DeleteCaPoolRequest.newBuilder().setName(caPool.getName()).build();

        client.deleteCaPoolCallable().futureCall(deleteCaPoolRequest).get(5, TimeUnit.MINUTES);
      }
    }
  }

  public static ListCaPoolsPagedResponse filterCaPools(String prefixToDelete, String project,
      String location) throws IOException {
    try (CertificateAuthorityServiceClient certificateAuthorityServiceClient =
        CertificateAuthorityServiceClient.create()) {

      LocationName locationName =
          LocationName.newBuilder().setProject(project).setLocation(location).build();

      ListCaPoolsRequest request = ListCaPoolsRequest.newBuilder()
          .setParent(locationName.toString())
          .setFilter(String.format("name:%s", prefixToDelete))
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

        DeleteCertificateAuthorityRequest deleteCertificateAuthorityRequest =
            DeleteCertificateAuthorityRequest.newBuilder()
                .setName(certificateAuthority.getName())
                .setIgnoreActiveCertificates(false)
                .build();

        certificateAuthorityServiceClient
            .deleteCertificateAuthorityCallable()
            .futureCall(deleteCertificateAuthorityRequest).get(5, TimeUnit.MINUTES);
      }
    }
  }
}
