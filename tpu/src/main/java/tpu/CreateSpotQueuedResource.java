package tpu;

import com.google.api.gax.retrying.RetrySettings;
import com.google.cloud.tpu.v2alpha1.CreateQueuedResourceRequest;
import com.google.cloud.tpu.v2alpha1.Node;
import com.google.cloud.tpu.v2alpha1.QueuedResource;
import com.google.cloud.tpu.v2alpha1.SchedulingConfig;
import com.google.cloud.tpu.v2alpha1.TpuClient;
import com.google.cloud.tpu.v2alpha1.TpuSettings;
import org.threeten.bp.Duration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class CreateSpotQueuedResource {
  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Google Cloud project you want to create a node.
    String projectId = "tyaho-softserve-project";//"your-project";
    // The zone in which to create the TPU.
    // For more information about supported TPU types for specific zones,
    // see https://cloud.google.com/tpu/docs/regions-zones
    String zone = "europe-west4-a";
    // The name for your TPU.
    String nodeName = "your-node-id1";
    // The accelerator type that specifies the version and size of the Cloud TPU you want to create.
    // For more information about supported accelerator types for each TPU version,
    // see https://cloud.google.com/tpu/docs/system-architecture-tpu-vm#versions.
    String tpuType = "v2-8";
    // Software version that specifies the version of the TPU runtime to install.
    // For more information see https://cloud.google.com/tpu/docs/runtimes
    String tpuSoftwareVersion = "tpu-vm-tf-2.14.1";
    // The name for your Queued Resource.
    String queuedResourceId = "your-queued-resource-id1";

    createQueuedResource(
        projectId, zone, queuedResourceId, nodeName, tpuType, tpuSoftwareVersion);
  }

  // Creates a Queued Resource
  public static void createQueuedResource(
      String projectId,
      String zone,
      String queuedResourceId,
      String nodeName,
      String tpuType,
      String tpuSoftwareVersion)
      throws IOException, ExecutionException, InterruptedException {
    // With these settings the client library handles the Operation's polling mechanism
    // and prevent CancellationException error
    TpuSettings.Builder clientSettings =
        TpuSettings.newBuilder();
    clientSettings
        .createQueuedResourceSettings()
        .setRetrySettings(
            RetrySettings.newBuilder()
                .setInitialRetryDelay(Duration.ofMillis(5000L))
                .setRetryDelayMultiplier(2.0)
                .setInitialRpcTimeout(Duration.ZERO)
                .setRpcTimeoutMultiplier(1.0)
                .setMaxRetryDelay(Duration.ofMillis(45000L))
                .setTotalTimeout(Duration.ofHours(24L))
                .build());
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (TpuClient tpuClient = TpuClient.create(clientSettings.build())) {
      String parent = String.format("projects/%s/locations/%s", projectId, zone);
      SchedulingConfig schedulingConfig = SchedulingConfig.newBuilder()
          .setPreemptible(true)
          .build();

      Node node =
          Node.newBuilder()
              .setName(nodeName)
              .setAcceleratorType(tpuType)
              .setRuntimeVersion(tpuSoftwareVersion)
              .setSchedulingConfig(schedulingConfig)
              .setQueuedResource(
                  String.format(
                      "projects/%s/locations/%s/queuedResources/%s",
                      projectId, zone, queuedResourceId))
              .build();

      QueuedResource queuedResource =
          QueuedResource.newBuilder()
              .setName(queuedResourceId)
              .setTpu(
                  QueuedResource.Tpu.newBuilder()
                      .addNodeSpec(
                          QueuedResource.Tpu.NodeSpec.newBuilder()
                              .setParent(parent)
                              .setNode(node)
                              .setNodeId(nodeName)
                              .build())
                      .build())
              .build();

      CreateQueuedResourceRequest request =
          CreateQueuedResourceRequest.newBuilder()
              .setParent(parent)
              .setQueuedResourceId(queuedResourceId)
              .setQueuedResource(queuedResource)
              .build();

      QueuedResource response = tpuClient.createQueuedResourceAsync(request).get();
      // You can wait until TPU Node is READY,
      // and check its status using getTpuVm() from "tpu_vm_get" sample.
      System.out.printf("Queued Resource created: %s\n", queuedResourceId);
    }
  }
}
