/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloudrun.snippets.services;

import java.io.IOException;
// [START cloudrun_Services_GetIamPolicy_sync]
import com.google.cloud.run.v2.ServiceName;
import com.google.cloud.run.v2.ServicesClient;
import com.google.iam.v1.GetIamPolicyRequest;
import com.google.iam.v1.GetPolicyOptions;
import com.google.iam.v1.Policy;

public class GetIamPolicy {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "PROJECT";
    String location = "us-central1";
    syncGetIamPolicy(projectId, location);
  }

  public static void syncGetIamPolicy(String projectId, String location) throws IOException {

    try (ServicesClient servicesClient = ServicesClient.create()) {
      GetIamPolicyRequest request =
          GetIamPolicyRequest.newBuilder()
              .setResource(ServiceName.of(projectId, location, "[SERVICE]").toString())
              .setOptions(GetPolicyOptions.newBuilder().build())
              .build();
      Policy response = servicesClient.getIamPolicy(request);
    }
  }
}
// [END cloudrun_Services_GetIamPolicy_sync]
