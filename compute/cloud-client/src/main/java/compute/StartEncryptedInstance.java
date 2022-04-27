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

// [START compute_start_enc_instance]

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.compute.v1.CustomerEncryptionKey;
import com.google.cloud.compute.v1.CustomerEncryptionKeyProtectedDisk;
import com.google.cloud.compute.v1.GetInstanceRequest;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.InstancesStartWithEncryptionKeyRequest;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.Operation.Status;
import com.google.cloud.compute.v1.StartWithEncryptionKeyInstanceRequest;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class StartEncryptedInstance {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    /* project: project ID or project number of the Cloud project your instance belongs to.
       zone: name of the zone your instance belongs to.
       instanceName: name of the instance your want to start.
       key: bytes object representing a raw base64 encoded key to your machines boot disk.
            For more information about disk encryption see:
            https://cloud.google.com/compute/docs/disks/customer-supplied-encryption#specifications
     */
    String project = "your-project-id";
    String zone = "zone-name";
    String instanceName = "instance-name";
    String key = "raw-key";

    startEncryptedInstance(project, zone, instanceName, key);
  }

  // Starts a stopped Google Compute Engine instance (with encrypted disks).
  public static void startEncryptedInstance(String project, String zone, String instanceName,
      String key)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    /* Initialize client that will be used to send requests. This client only needs to be created
       once, and can be reused for multiple requests. After completing all of your requests, call
       the `instancesClient.close()` method on the client to safely
       clean up any remaining background resources. */
    try (InstancesClient instancesClient = InstancesClient.create()) {

      GetInstanceRequest getInstanceRequest = GetInstanceRequest.newBuilder()
          .setProject(project)
          .setZone(zone)
          .setInstance(instanceName).build();

      Instance instance = instancesClient.get(getInstanceRequest);

      // Prepare the information about disk encryption.
      CustomerEncryptionKeyProtectedDisk protectedDisk = CustomerEncryptionKeyProtectedDisk
          .newBuilder()
          /* Use raw_key to send over the key to unlock the disk
             To use a key stored in KMS, you need to provide:
             `kms_key_name` and `kms_key_service_account`
           */
          .setDiskEncryptionKey(CustomerEncryptionKey.newBuilder()
              .setRawKey(key).build())
          .setSource(instance.getDisks(0).getSource())
          .build();

      InstancesStartWithEncryptionKeyRequest startWithEncryptionKeyRequest =
          InstancesStartWithEncryptionKeyRequest.newBuilder()
              .addDisks(protectedDisk).build();

      StartWithEncryptionKeyInstanceRequest encryptionKeyInstanceRequest =
          StartWithEncryptionKeyInstanceRequest.newBuilder()
              .setProject(project)
              .setZone(zone)
              .setInstance(instanceName)
              .setInstancesStartWithEncryptionKeyRequestResource(startWithEncryptionKeyRequest)
              .build();

      OperationFuture<Operation, Operation> operation = instancesClient.startWithEncryptionKeyAsync(
          encryptionKeyInstanceRequest);
      Operation response = operation.get(3, TimeUnit.MINUTES);
      ;

      if (response.getStatus() == Status.DONE) {
        System.out.println("Encrypted instance started successfully ! ");
      }
    }
  }

}
// [END compute_start_enc_instance]