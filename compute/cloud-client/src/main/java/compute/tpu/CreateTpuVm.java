package compute.tpu;

//[START tpu_vm_create]

import com.google.cloud.tpu.v1.CreateNodeRequest;
import com.google.cloud.tpu.v1.Node;
import com.google.cloud.tpu.v1.TpuClient;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class CreateTpuVm {

  public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "tyaho-softserve-project";//"YOUR_PROJECT_ID";
    String zone = "europe-west4-a";
    String tpuVmName = "my-tpu-vm111";
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

      Node tpuVm =
          Node.newBuilder()
              .setName(tpuVmName)
              .setAcceleratorType(acceleratorType)
              .setTensorflowVersion(version)
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
