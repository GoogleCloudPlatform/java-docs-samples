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

package functions;

import static com.google.common.truth.Truth.assertThat;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.compute.v1.AttachedDisk;
import com.google.cloud.compute.v1.AttachedDisk.Type;
import com.google.cloud.compute.v1.AttachedDiskInitializeParams;
import com.google.cloud.compute.v1.DeleteInstanceRequest;
import com.google.cloud.compute.v1.InsertInstanceRequest;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.NetworkInterface;
import com.google.cloud.compute.v1.Operation;
import com.google.common.testing.TestLogHandler;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class AutoLabelInstanceTest {
  private static final Logger logger = Logger.getLogger(AutoLabelInstance.class.getName());
  private static final TestLogHandler logHandler = new TestLogHandler();

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ZONE = "us-central1-a";
  private static final String INSTANCE = "gcf-test" + UUID.randomUUID().toString();
  private static final int TIMEOUT = 3;

  @BeforeClass
  public static void beforeClass()
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    assertThat(PROJECT_ID).isNotNull();
    try {
      createInstance(PROJECT_ID, ZONE, INSTANCE);
    } catch (Exception e) {
      System.out.println("VM already exists: " + e);
    }
    logger.addHandler(logHandler);
  }

  @AfterClass
  public static void cleanup() throws Exception {
    deleteInstance(PROJECT_ID, ZONE, INSTANCE);
  }

  @Test
  public void functionsAutoLabelInstance() throws Exception {
    // Build a CloudEvent Log Entry
    JsonObject protoPayload = new JsonObject();
    JsonObject authInfo = new JsonObject();
    String email = "test@gmail.com";
    authInfo.addProperty("principalEmail", email);

    protoPayload.add("authenticationInfo", authInfo);

    String resource =
        String.format(
            "compute.googleapis.com/projects/%s/zones/%s/instances/%s", PROJECT_ID, ZONE, INSTANCE);
    protoPayload.addProperty("resourceName", resource);
    protoPayload.addProperty("methodName", "beta.compute.instances.insert");

    JsonObject encodedData = new JsonObject();
    encodedData.add("protoPayload", protoPayload);
    encodedData.addProperty("name", "test name");

    CloudEvent event =
        CloudEventBuilder.v1()
            .withId("0")
            .withSubject(resource)
            .withType("google.cloud.audit.log.v1.written")
            .withSource(URI.create("https://example.com"))
            .withData(new Gson().toJson(encodedData).getBytes())
            .build();

    new AutoLabelInstance().accept(event);

    assertThat(
            String.format(
                "Adding label, \"{'creator': '%s'}\", to instance, \"%s\".",
                "test-gmail-com", INSTANCE))
        .isEqualTo(logHandler.getStoredLogRecords().get(0).getMessage());
  }

  public static void createInstance(String project, String zone, String instanceName)
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    String machineType = String.format("zones/%s/machineTypes/n1-standard-1", zone);
    String sourceImage = "projects/debian-cloud/global/images/family/debian-11";
    long diskSizeGb = 10L;
    String networkName = "default";

    try (InstancesClient instancesClient = InstancesClient.create()) {
      // Instance creation requires at least one persistent disk and one network interface.
      AttachedDisk disk =
          AttachedDisk.newBuilder()
              .setBoot(true)
              .setAutoDelete(true)
              .setType(Type.PERSISTENT.toString())
              .setDeviceName("disk-1")
              .setInitializeParams(
                  AttachedDiskInitializeParams.newBuilder()
                      .setSourceImage(sourceImage)
                      .setDiskSizeGb(diskSizeGb)
                      .build())
              .build();

      // Use the network interface provided in the networkName argument.
      NetworkInterface networkInterface =
          NetworkInterface.newBuilder().setName(networkName).build();

      // Bind `instanceName`, `machineType`, `disk`, and `networkInterface` to an instance.
      Instance instanceResource =
          Instance.newBuilder()
              .setName(instanceName)
              .setMachineType(machineType)
              .addDisks(disk)
              .addNetworkInterfaces(networkInterface)
              .build();

      System.out.printf("Creating instance: %s at %s %n", instanceName, zone);

      // Insert the instance in the specified project and zone.
      InsertInstanceRequest insertInstanceRequest =
          InsertInstanceRequest.newBuilder()
              .setProject(project)
              .setZone(zone)
              .setInstanceResource(instanceResource)
              .build();

      OperationFuture<Operation, Operation> operation =
          instancesClient.insertAsync(insertInstanceRequest);

      // Wait for the operation to complete.
      Operation response = operation.get(TIMEOUT, TimeUnit.MINUTES);

      if (response.hasError()) {
        System.out.println("Instance creation failed ! ! " + response);
        return;
      }
      System.out.println("Operation Status: " + response.getStatus());
    }
  }

  public static void deleteInstance(String project, String zone, String instanceName)
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    try (InstancesClient instancesClient = InstancesClient.create()) {

      System.out.printf("Deleting instance: %s ", instanceName);

      // Describe which instance is to be deleted.
      DeleteInstanceRequest deleteInstanceRequest =
          DeleteInstanceRequest.newBuilder()
              .setProject(project)
              .setZone(zone)
              .setInstance(instanceName)
              .build();

      OperationFuture<Operation, Operation> operation =
          instancesClient.deleteAsync(deleteInstanceRequest);
      // Wait for the operation to complete.
      Operation response = operation.get(TIMEOUT, TimeUnit.MINUTES);

      if (response.hasError()) {
        System.out.println("Instance deletion failed ! ! " + response);
        return;
      }
      System.out.println("Operation Status: " + response.getStatus());
    }
  }
}
