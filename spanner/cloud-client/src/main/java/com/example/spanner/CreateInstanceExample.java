package com.example.spanner;

//[START spanner_create_instance]
import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.spanner.Instance;
import com.google.cloud.spanner.InstanceAdminClient;
import com.google.cloud.spanner.InstanceConfigId;
import com.google.cloud.spanner.InstanceId;
import com.google.cloud.spanner.InstanceInfo;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerOptions;
import com.google.spanner.admin.instance.v1.CreateInstanceMetadata;
import java.util.concurrent.ExecutionException;

class CreateInstanceExample {

  static void createInstance() {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project";
    String instanceId = "my-instance";
    createInstance(projectId, instanceId);
  }

  static void createInstance(String projectId, String instanceId) {
    Spanner spanner = SpannerOptions.newBuilder().setProjectId(projectId).build().getService();
    InstanceAdminClient instanceAdminClient = spanner.getInstanceAdminClient();

    // Set Instance configuration.
    String configId = "regional-us-central1";
    int nodeCount = 2;
    String displayName = "Descriptive name for the instance";

    // Create an InstanceInfo object that will be used to create the instance.
    InstanceInfo instanceInfo =
        InstanceInfo.newBuilder(InstanceId.of(projectId, instanceId))
            .setInstanceConfigId(InstanceConfigId.of(projectId, configId))
            .setNodeCount(nodeCount)
            .setDisplayName(displayName)
            .build();
    OperationFuture<Instance, CreateInstanceMetadata> operation =
        instanceAdminClient.createInstance(instanceInfo);
    try {
      // Wait for the createInstance operation to finish.
      Instance instance = operation.get();
      System.out.printf("Instance %s was successfully created%n", instance.getId());
    } catch (ExecutionException e) {
      System.err.printf(
          "Creating instance %s failed with error message %s%n",
          instanceInfo.getId(), e.getMessage());
    } catch (InterruptedException e) {
      System.err.println("Waiting for createInstance operation to finish was interrupted");
    }
  }
}
//[END spanner_create_instance]
