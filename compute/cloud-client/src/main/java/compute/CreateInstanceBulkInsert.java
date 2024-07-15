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

// [START compute_instances_bulk_insert]

import com.google.cloud.compute.v1.BulkInsertInstanceRequest;
import com.google.cloud.compute.v1.BulkInsertInstanceResource;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstanceProperties;
import com.google.cloud.compute.v1.InstanceTemplatesClient;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.ListInstancesRequest;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateInstanceBulkInsert {
  public static void main(String[] args)
          throws IOException, InterruptedException, ExecutionException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    String project = "your-project-id";
    // Name of the zone to create the instance in. For example: "us-west3-b"
    String zone = "zone-name";
    // An Instance Template to be used for creation of the new VMs.
    String templateName = "instance-template";
    // The maximum number of instances to create.
    int count = 3;
    // The string pattern used for the names of the VMs. For more info see:
    // https://cloud.google.com/compute/docs/reference/rest/v1/instances/bulkInsert
    String namePattern = "instance-name-pattern";
    // (optional): The minimum number of instances to create. For more info see:
    // https://cloud.google.com/compute/docs/reference/rest/v1/instances/bulkInsert
    int minCount = 2;
    // (optional): A dictionary with labels to be added to the new VMs.
    Map<String, String> labels = new HashMap<>();

    bulkInsertInstance(project, zone, templateName, count, namePattern, minCount, labels);
  }

  // Create multiple VMs based on an Instance Template. The newly created instances will
  // be returned as a list and will share a label with key `bulk_batch` and a random value.
  public static List<Instance> bulkInsertInstance(String project, String zone, String templateName,
                                                  int count, String namePattern, int minCount,
                                                  Map<String, String> labels)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (InstanceTemplatesClient templatesClient = InstanceTemplatesClient.create();
         InstancesClient instancesClient = InstancesClient.create()) {
      String sourceInstanceTemplate = templatesClient.get(project, templateName).getSelfLink();

      String labelsValue = UUID.randomUUID().toString().replace("-", "").toLowerCase();
      labels.put("bulk_batch", labelsValue);

      InstanceProperties.Builder instanceProperties = InstanceProperties.newBuilder()
              .putAllLabels(labels);

      BulkInsertInstanceResource instanceResource = BulkInsertInstanceResource.newBuilder()
              .setSourceInstanceTemplate(sourceInstanceTemplate)
              .setCount(count)
              .setMinCount(minCount)
              .setNamePattern(namePattern)
              .setInstanceProperties(instanceProperties)
              .build();

      BulkInsertInstanceRequest request = BulkInsertInstanceRequest.newBuilder()
              .setBulkInsertInstanceResourceResource(instanceResource)
              .setProject(project)
              .setZone(zone)
              .build();
      instancesClient.bulkInsertCallable().futureCall(request).get(60, TimeUnit.SECONDS);

      // Create request to retrieve all created instances
      ListInstancesRequest build = ListInstancesRequest.newBuilder()
              .setProject(project)
              .setZone(zone)
              .setFilter(createFilter(labels))
              .build();

      // Wait for server update
      TimeUnit.SECONDS.sleep(60);;

      return Lists.newArrayList(instancesClient.list(build).iterateAll());
    }
  }

  // Filter instances by labels
  private static String createFilter(Map<String, String> labels) {
    StringJoiner joiner = new StringJoiner(" AND ");

    for (Map.Entry<String, String> entry : labels.entrySet()) {
      joiner.add("labels." + entry.getKey() + ":" + entry.getValue());
    }
    return joiner.toString();
  }
}
//  [END compute_instances_bulk_insert]