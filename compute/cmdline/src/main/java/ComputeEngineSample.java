/*
 * Copyright 2015 Google Inc.
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

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.compute.Compute;
import com.google.api.services.compute.ComputeScopes;
import com.google.api.services.compute.model.AccessConfig;
import com.google.api.services.compute.model.AttachedDisk;
import com.google.api.services.compute.model.AttachedDiskInitializeParams;
import com.google.api.services.compute.model.Instance;
import com.google.api.services.compute.model.InstanceList;
import com.google.api.services.compute.model.Metadata;
import com.google.api.services.compute.model.NetworkInterface;
import com.google.api.services.compute.model.Operation;
import com.google.api.services.compute.model.ServiceAccount;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Command-line sample to demo listing Google Compute Engine instances using Java and the Google
 * Compute Engine API.
 *
 * @author Jonathan Simon
 */
public class ComputeEngineSample {

  /**
   * Be sure to specify the name of your application. If the application name is {@code null} or
   * blank, the application will log a warning. Suggested format is "MyCompany-ProductName/1.0".
   */
  private static final String APPLICATION_NAME = "";

  /** Set PROJECT_ID to your Project ID from the Overview pane in the Developers console. */
  private static final String PROJECT_ID = "YOUR_PROJECT_ID";

  /** Set Compute Engine zone. */
  private static final String ZONE_NAME = "us-central1-f";

  /** Set the name of the sample VM instance to be created. */
  private static final String SAMPLE_INSTANCE_NAME = "my-sample-instance";

  /** Set the path of the OS image for the sample VM instance to be created. */
  private static final String SOURCE_IMAGE_PREFIX =
      "https://www.googleapis.com/compute/v1/projects/";

  private static final String SOURCE_IMAGE_PATH =
      "ubuntu-os-cloud/global/images/ubuntu-2004-focal-v20200529";

  /** Set the Network configuration values of the sample VM instance to be created. */
  private static final String NETWORK_INTERFACE_CONFIG = "ONE_TO_ONE_NAT";

  private static final String NETWORK_ACCESS_CONFIG = "External NAT";

  /** Set the time out limit for operation calls to the Compute Engine API. */
  private static final long OPERATION_TIMEOUT_MILLIS = 60 * 1000;

  /** Global instance of the HTTP transport. */
  private static HttpTransport httpTransport;

  /** Global instance of the JSON factory. */
  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

  public static void main(String[] args) {
    try {
      httpTransport = GoogleNetHttpTransport.newTrustedTransport();

      // Authenticate using Google Application Default Credentials.
      GoogleCredentials credential = GoogleCredentials.getApplicationDefault();
      if (credential.createScopedRequired()) {
        List<String> scopes = new ArrayList<>();
        // Set Google Cloud Storage scope to Full Control.
        scopes.add(ComputeScopes.DEVSTORAGE_FULL_CONTROL);
        // Set Google Compute Engine scope to Read-write.
        scopes.add(ComputeScopes.COMPUTE);
        credential = credential.createScoped(scopes);
      }
      HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credential);
      // Create Compute Engine object for listing instances.
      Compute compute =
          new Compute.Builder(httpTransport, JSON_FACTORY, requestInitializer)
              .setApplicationName(APPLICATION_NAME)
              .build();

      // List out instances, looking for the one created by this sample app.
      boolean foundOurInstance = printInstances(compute);

      Operation op;
      if (foundOurInstance) {
        op = deleteInstance(compute, SAMPLE_INSTANCE_NAME);
      } else {
        op = startInstance(compute, SAMPLE_INSTANCE_NAME);
      }

      // Call Compute Engine API operation and poll for operation completion status
      System.out.println("Waiting for operation completion...");
      Operation.Error error = blockUntilComplete(compute, op, OPERATION_TIMEOUT_MILLIS);
      if (error == null) {
        System.out.println("Success!");
      } else {
        System.out.println(error.toPrettyString());
      }
    } catch (IOException e) {
      System.err.println(e.getMessage());
    } catch (Throwable t) {
      t.printStackTrace();
    }
    System.exit(1);
  }

  // [START list_instances]
  /**
   * Print available machine instances.
   *
   * @param compute The main API access point
   * @return {@code true} if the instance created by this sample app is in the list
   */
  public static boolean printInstances(Compute compute) throws IOException {
    System.out.println("================== Listing Compute Engine Instances ==================");
    Compute.Instances.List instances = compute.instances().list(PROJECT_ID, ZONE_NAME);
    InstanceList list = instances.execute();
    boolean found = false;
    if (list.getItems() == null) {
      System.out.println(
          "No instances found. Sign in to the Google Developers Console and create "
              + "an instance at: https://console.developers.google.com/");
    } else {
      for (Instance instance : list.getItems()) {
        System.out.println(instance.toPrettyString());
        if (instance.getName().equals(SAMPLE_INSTANCE_NAME)) {
          found = true;
        }
      }
    }
    return found;
  }
  // [END list_instances]

  // [START compute_create_instance]
  // [START create_instances]
  public static Operation startInstance(Compute compute, String instanceName) throws IOException {
    System.out.println("================== Starting New Instance ==================");

    // Create VM Instance object with the required properties.
    Instance instance = new Instance();
    instance.setName(instanceName);
    instance.setMachineType(
        String.format(
            "https://www.googleapis.com/compute/v1/projects/%s/zones/%s/machineTypes/e2-standard-1",
            PROJECT_ID, ZONE_NAME));
    // Add Network Interface to be used by VM Instance.
    NetworkInterface ifc = new NetworkInterface();
    ifc.setNetwork(
        String.format(
            "https://www.googleapis.com/compute/v1/projects/%s/global/networks/default",
            PROJECT_ID));
    List<AccessConfig> configs = new ArrayList<>();
    AccessConfig config = new AccessConfig();
    config.setType(NETWORK_INTERFACE_CONFIG);
    config.setName(NETWORK_ACCESS_CONFIG);
    configs.add(config);
    ifc.setAccessConfigs(configs);
    instance.setNetworkInterfaces(Collections.singletonList(ifc));

    // Add attached Persistent Disk to be used by VM Instance.
    AttachedDisk disk = new AttachedDisk();
    disk.setBoot(true);
    disk.setAutoDelete(true);
    disk.setType("PERSISTENT");
    AttachedDiskInitializeParams params = new AttachedDiskInitializeParams();
    // Assign the Persistent Disk the same name as the VM Instance.
    params.setDiskName(instanceName);
    // Specify the source operating system machine image to be used by the VM Instance.
    params.setSourceImage(SOURCE_IMAGE_PREFIX + SOURCE_IMAGE_PATH);
    // Specify the disk type as Standard Persistent Disk
    params.setDiskType(
        String.format(
            "https://www.googleapis.com/compute/v1/projects/%s/zones/%s/diskTypes/pd-standard",
            PROJECT_ID, ZONE_NAME));
    disk.setInitializeParams(params);
    instance.setDisks(Collections.singletonList(disk));

    // Initialize the service account to be used by the VM Instance and set the API access scopes.
    ServiceAccount account = new ServiceAccount();
    account.setEmail("default");
    List<String> scopes = new ArrayList<>();
    scopes.add("https://www.googleapis.com/auth/devstorage.full_control");
    scopes.add("https://www.googleapis.com/auth/compute");
    account.setScopes(scopes);
    instance.setServiceAccounts(Collections.singletonList(account));

    // Optional - Add a startup script to be used by the VM Instance.
    Metadata meta = new Metadata();
    Metadata.Items item = new Metadata.Items();
    item.setKey("startup-script-url");
    // If you put a script called "vm-startup.sh" in this Google Cloud Storage
    // bucket, it will execute on VM startup.  This assumes you've created a
    // bucket named the same as your PROJECT_ID.
    // For info on creating buckets see:
    // https://cloud.google.com/storage/docs/cloud-console#_creatingbuckets
    item.setValue(String.format("gs://%s/vm-startup.sh", PROJECT_ID));
    meta.setItems(Collections.singletonList(item));
    instance.setMetadata(meta);

    System.out.println(instance.toPrettyString());
    Compute.Instances.Insert insert = compute.instances().insert(PROJECT_ID, ZONE_NAME, instance);
    return insert.execute();
  }
  // [END create_instances]
  // [END compute_create_instance]

  private static Operation deleteInstance(Compute compute, String instanceName) throws Exception {
    System.out.println(
        "================== Deleting Instance " + instanceName + " ==================");
    Compute.Instances.Delete delete =
        compute.instances().delete(PROJECT_ID, ZONE_NAME, instanceName);
    return delete.execute();
  }

  public static String getLastWordFromUrl(String url) {
    if (url != null) {
      String[] bits = url.split("/");
      url = bits[bits.length - 1];
    }
    return url;
  }

  // [START compute_wait_for_operation]
  // [START wait_until_complete]
  /**
   * Wait until {@code operation} is completed.
   *
   * @param compute the {@code Compute} object
   * @param operation the operation returned by the original request
   * @param timeout the timeout, in millis
   * @return the error, if any, else {@code null} if there was no error
   * @throws InterruptedException if we timed out waiting for the operation to complete
   * @throws IOException if we had trouble connecting
   */
  public static Operation.Error blockUntilComplete(
      Compute compute, Operation operation, long timeout) throws Exception {
    long start = System.currentTimeMillis();
    final long pollInterval = 5 * 1000;
    String zone = getLastWordFromUrl(operation.getZone()); // null for global/regional operations
    String region = getLastWordFromUrl(operation.getRegion());
    String status = operation.getStatus();
    String opId = operation.getName();
    while (operation != null && !status.equals("DONE")) {
      Thread.sleep(pollInterval);
      long elapsed = System.currentTimeMillis() - start;
      if (elapsed >= timeout) {
        throw new InterruptedException("Timed out waiting for operation to complete");
      }
      System.out.println("waiting...");
      if (zone != null) {
        Compute.ZoneOperations.Get get = compute.zoneOperations().get(PROJECT_ID, zone, opId);
        operation = get.execute();
      } else if (region != null) {
        Compute.RegionOperations.Get get = compute.regionOperations().get(PROJECT_ID, region, opId);
        operation = get.execute();
      } else {
        Compute.GlobalOperations.Get get = compute.globalOperations().get(PROJECT_ID, opId);
        operation = get.execute();
      }
      if (operation != null) {
        status = operation.getStatus();
      }
    }
    return operation == null ? null : operation.getError();
  }
  // [END wait_until_complete]
  // [END compute_wait_for_operation]
}
