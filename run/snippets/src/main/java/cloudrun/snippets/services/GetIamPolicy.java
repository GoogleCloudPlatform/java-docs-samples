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

// [START cloudrun_get_iam_policy]
import com.google.cloud.run.v2.ServiceName;
import com.google.cloud.run.v2.ServicesClient;
import com.google.iam.v1.Binding;
import com.google.iam.v1.GetIamPolicyRequest;
import com.google.iam.v1.Policy;
import java.io.IOException;

public class GetIamPolicy {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String location = "us-central1";
    String serviceId = "my-service";
    getIamPolicy(projectId, location, serviceId);
  }

  public static void getIamPolicy(String projectId, String location, String serviceId) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (ServicesClient servicesClient = ServicesClient.create()) {
      GetIamPolicyRequest request =
          GetIamPolicyRequest.newBuilder()
              .setResource(ServiceName.of(projectId, location, serviceId).toString())
              .build();
      // Send request
      Policy response = servicesClient.getIamPolicy(request);
      System.out.println("IAM Policy bindings for service, " + serviceId + ":");
      // Example usage of the Policy object
      for (Binding binding : response.getBindingsList()) {
        System.out.println("Role: " + binding.getRole());
        System.out.println("Members: " + binding.getMembersList());
      }
    }
  }
}
// [END cloudrun_get_iam_policy]
