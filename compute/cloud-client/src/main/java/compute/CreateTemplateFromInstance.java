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

// [START compute_template_create_from_instance]

import com.google.cloud.compute.v1.DiskInstantiationConfig;
import com.google.cloud.compute.v1.DiskInstantiationConfig.InstantiateFrom;
import com.google.cloud.compute.v1.GlobalOperationsClient;
import com.google.cloud.compute.v1.InsertInstanceTemplateRequest;
import com.google.cloud.compute.v1.InstanceTemplate;
import com.google.cloud.compute.v1.InstanceTemplatesClient;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.SourceInstanceParams;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateTemplateFromInstance {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // projectId: project ID or project number of the Cloud project you use.
    // instance: the instance to base the new template on. This value uses the following format:
    // **NOTE**: "projects/{project}/zones/{zone}/instances/{instance_name}"
    // templateName: name of the new template to create.
    String projectId = "your-project-id";
    String templateName = "template-name";
    String instance = String.format("projects/%s/zones/%s/instances/%s", projectId, "zone",
        "instanceName");
    createTemplateFromInstance(projectId, templateName, instance);
  }

  // Create a new instance template based on an existing instance.
  // This new template specifies a different boot disk.
  public static void createTemplateFromInstance(String projectId, String templateName,
      String instance)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    try (InstanceTemplatesClient instanceTemplatesClient = InstanceTemplatesClient.create();
        GlobalOperationsClient globalOperationsClient = GlobalOperationsClient.create()) {

      SourceInstanceParams sourceInstanceParams = SourceInstanceParams.newBuilder()
          .addDiskConfigs(DiskInstantiationConfig.newBuilder()
              // Device name must match the name of a disk attached to the instance you are
              // basing your template on.
              .setDeviceName("disk-1")
              // Replace the original boot disk image used in your instance
              // with a Rocky Linux image.
              .setInstantiateFrom(InstantiateFrom.CUSTOM_IMAGE.toString())
              .setCustomImage(
                  String.format("projects/%s/global/images/family/%s", "rocky-linux-cloud",
                      "rocky-linux-8"))
              // Override the AutoDelete setting.
              .setAutoDelete(true).build())
          .build();

      InstanceTemplate instanceTemplate = InstanceTemplate.newBuilder()
          .setName(templateName)
          .setSourceInstance(instance)
          .setSourceInstanceParams(sourceInstanceParams)
          .build();

      InsertInstanceTemplateRequest insertInstanceTemplateRequest = InsertInstanceTemplateRequest
          .newBuilder()
          .setProject(projectId)
          .setInstanceTemplateResource(instanceTemplate)
          .build();

      Operation operation = instanceTemplatesClient.insertCallable()
          .futureCall(insertInstanceTemplateRequest).get(3, TimeUnit.MINUTES);
      ;

      Operation response = globalOperationsClient.wait(projectId, operation.getName());

      if (response.hasError()) {
        System.out.println("Instance Template creation failed ! ! " + response);
        return;
      }
      System.out.printf("Instance Template creation operation status %s: %s", templateName,
          response.getStatus());
    }
  }
}
// [END compute_template_create_from_instance] 