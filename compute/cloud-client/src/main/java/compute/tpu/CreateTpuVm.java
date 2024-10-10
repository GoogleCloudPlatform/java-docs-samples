package compute.tpu;

//[START tpu_vm_create]

import com.google.cloud.tpu.v2.CreateNodeRequest;
import com.google.cloud.tpu.v2.Node;
import com.google.cloud.tpu.v2.TpuClient;
import java.io.IOException;

import java.util.concurrent.ExecutionException;

public class CreateTpuVm {

  public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "tyaho-softserve-project";//"YOUR_PROJECT_ID";
    String zone = "europe-west4-a";
    String tpuVmName = "test-tpu-name1";
    String acceleratorType = "v2-8";
    String version = "tpu-vm-tf-2.14.1";

    createTpuVm(projectId, zone, tpuVmName, acceleratorType, version);
  }

  // Creates a TPU VM with the specified name, zone, accelerator type, and version.
  public static void createTpuVm(
      String projectId, String zone, String tpuVmName, String acceleratorType, String version)
      throws IOException, ExecutionException, InterruptedException {
    try (TpuClient tpuClient = TpuClient.create()) {
      String parent = String.format("projects/%s/locations/%s", projectId, zone);

      // Create metadata map
//      Map<String, String> metadata = new HashMap<>();
//      metadata.put("startup-script", "pip3 install --upgrade numpy");
      String topology = "8x8x1";
      Node tpuVm =
          Node.newBuilder()
              .setName(tpuVmName)
              .setAcceleratorType(acceleratorType)
              .setAcceleratorConfig(Node.newBuilder()
                  .getAcceleratorConfigBuilder()
                  .setTopology(topology)
                  .build())
              .setRuntimeVersion(version)
//              .putAllLabels(metadata)
              .build();

      CreateNodeRequest request =
          CreateNodeRequest.newBuilder()
              .setParent(parent)
              .setNodeId(tpuVmName)
              .setNode(tpuVm)
              .build();

      Node response = tpuClient.createNodeAsync(request).get();
      System.out.printf("TPU VM created: %s\n", response.getName());
    }
  }
}
//[END tpu_vm_create]
