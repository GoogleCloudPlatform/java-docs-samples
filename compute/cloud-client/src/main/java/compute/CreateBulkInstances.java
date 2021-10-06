/*
 * Copyright 2021 Google LLC
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

// [START bulk_compute_instances_create]

import com.google.cloud.compute.v1.BulkInsertInstanceRequest;
import com.google.cloud.compute.v1.BulkInsertInstanceResource;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.ZoneOperationsClient;
import java.io.IOException;

public class CreateBulkInstances {

  public static void main(String[] args) throws IOException, InterruptedException {
    String project = "your-project-id";
    String zone = "zone-name";
    String instanceName = "instance-name";
    createBulkInstance(project, zone, instanceName);
  }

  // Create a new instance with the provided "instanceName" value in the specified
  // project and zone.
  public static void createBulkInstance(String project, String zone, String instanceName)
        throws IOException, InterruptedException {
    long numInstances = 50L;
    //          Initialize client that will be used to send requests. 
    //          This client only needs to be created
    //          once, and can be reused for multiple requests. After completing all 
    //          of your requests, call the `instancesClient.close()
    //          method on the client to safely clean up
    //          any remaining background resources.
    try (InstancesClient instancesClient = InstancesClient.create();
        ZoneOperationsClient zoneOperationsClient = ZoneOperationsClient.create()) {
      BulkInsertInstanceRequest bulkInsertInstancerequest = 
              BulkInsertInstanceRequest.newBuilder()
                  .setBulkInsertInstanceResourceResource(
                   BulkInsertInstanceResource.newBuilder()
                    .setSourceInstanceTemplate("instanceTemplate")
                    .setNamePattern("instance-name-###")
                    .setCount(numInstances).build())
                    .setProject(project)
                    .setRequestId("requestId")
                    .setZone(zone)
                    .build();
      System.out.println(String.format("Creating instance: %s at %s ", instanceName, zone));
      // Insert the instance in the specified project and zone.
      // Operation response = instancesClient.insert(project, zone, instanceResource);
      Operation response = instancesClient.bulkInsert(bulkInsertInstancerequest);
      
      if (response.hasError()) {
        System.out.println("Bulk Instances creation failed ! ! " + response);
        return;
      }
      System.out.println("Operation Status: " + response.getStatus());
    }
  }
}
//  [END bulk_compute_instances_create]
