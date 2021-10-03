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

import com.google.cloud.compute.v1.AttachedDisk;
import com.google.cloud.compute.v1.AttachedDisk.Type;
import com.google.cloud.compute.v1.AttachedDiskInitializeParams;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.NetworkInterface;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.Operation.Status;
import com.google.cloud.compute.v1.ZoneOperationsClient;
import com.google.cloud.compute.v1.BulkInsertInstanceRequest;
import com.google.cloud.compute.v1.BulkInsertInstanceResource;

import java.io.IOException;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

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
    // Below are sample values that can be replaced.
    // machineType: machine type of the VM being created. This value uses the format zones/{zone}/machineTypes/{type_name}. For a list of machine types, see https://cloud.google.com/compute/docs/machine-types
    // sourceImage: path to the operating system image to mount. For details about images you can mount, see https://cloud.google.com/compute/docs/images
    // diskSizeGb: storage size of the boot disk to attach to the instance.
    // networkName: network interface to associate with the instance.
        String machineType = String.format("zones/%s/machineTypes/n1-standard-1", zone);
        long diskSizeGb = 10L;
        long numInstances = 50L;
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the `instancesClient.close()` method on the client to safely
    // clean up any remaining background resources.
        try (InstancesClient instancesClient = InstancesClient.create();
                ZoneOperationsClient zoneOperationsClient = ZoneOperationsClient.create()) {
            // Instance creation requires at least one persistent disk and one network
            // interface.
            // Bind `instanceName`, `machineType`, `disk`, and `networkInterface` to an
            // instance.
            BulkInsertInstanceRequest bulkInsertInstancerequest = BulkInsertInstanceRequest.newBuilder()
                    .setBulkInsertInstanceResourceResource(BulkInsertInstanceResource.newBuilder().
                    setSourceInstanceTemplate(instanceTemplate)
                    .setNamePattern("instance-name-###")
                    .setCount(numInstances).build())
                    .setProject(project)
                    .setRequestId("requestId")
                    .setZone(zone)
                    .build();
            // SOP
            System.out.println(String.format("Creating instance: %s at %s ", instanceName, request));
            // Insert the instance in the specified project and zone.
            // Operation response = instancesClient.insert(project, zone, instanceResource);
            Operation operation = instancesClient.insertCallable().futureCall(bulkInsertInstancerequest)
                .get();
      
            // Wait for the operation to complete.
            Operation response = zoneOperationsClient.wait(project, zone, operation.getName());
      
            if (response.hasError()) {
              System.out.println("Instance creation failed ! ! " + response);
              return;
            }
            System.out.println("Operation Status: " + response.getStatus());
          }
        }
      }
      //  [END bulk_compute_instances_create]