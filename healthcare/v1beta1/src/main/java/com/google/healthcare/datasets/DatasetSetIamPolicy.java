/*
 * Copyright 2019 Google LLC
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

package com.google.healthcare.datasets;

// [START healthcare_dataset_set_iam_policy]

import com.google.HealthcareQuickstart;
import com.google.api.services.healthcare.v1beta1.model.Binding;
import com.google.api.services.healthcare.v1beta1.model.Policy;
import com.google.api.services.healthcare.v1beta1.model.SetIamPolicyRequest;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.List;

public class DatasetSetIamPolicy {
  private static final Gson GSON = new Gson();

  public static void setIamPolicy(String datasetName, String role, List<String> members)
      throws IOException {
    Binding binding = new Binding();

    binding.setRole(role);
    binding.setMembers(members);

    List<Binding> bindings = ImmutableList.of(binding);
    SetIamPolicyRequest request = new SetIamPolicyRequest();
    request.setPolicy(new Policy());
    request.getPolicy().setBindings(bindings);

    Policy updatedPolicy =
        HealthcareQuickstart.getCloudHealthcareClient()
            .projects()
            .locations()
            .datasets()
            .setIamPolicy(datasetName, request)
            .execute();
    System.out.println("Set Dataset store policy: " + GSON.toJson(updatedPolicy));
  }
}
// [END healthcare_dataset_set_iam_policy]
