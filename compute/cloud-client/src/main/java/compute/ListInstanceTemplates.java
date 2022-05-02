/*
 * Copyright 2022 Google LLC
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

// [START compute_template_list]

import com.google.cloud.compute.v1.InstanceTemplate;
import com.google.cloud.compute.v1.InstanceTemplatesClient;
import com.google.cloud.compute.v1.InstanceTemplatesClient.ListPagedResponse;
import java.io.IOException;

public class ListInstanceTemplates {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    // projectId: project ID or project number of the Cloud project you use.
    String projectId = "your-project-id";
    listInstanceTemplates(projectId);
  }

  // Get a list of InstanceTemplate objects available in a project.
  public static ListPagedResponse listInstanceTemplates(String projectId) throws IOException {
    try (InstanceTemplatesClient instanceTemplatesClient = InstanceTemplatesClient.create()) {
      int count = 0;
      System.out.println("Listing instance templates...");
      ListPagedResponse templates = instanceTemplatesClient.list(projectId);
      for (InstanceTemplate instanceTemplate : templates.iterateAll()) {
        System.out.printf("%s. %s%n", ++count, instanceTemplate.getName());
      }
      return templates;
    }
  }
}
// [END compute_template_list]