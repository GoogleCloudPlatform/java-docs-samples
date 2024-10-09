package compute.tpu;

//[START tpu_vm_get]

import com.google.cloud.tpu.v2.GetNodeRequest;
import com.google.cloud.tpu.v2.Node;
import com.google.cloud.tpu.v2.NodeName;
import com.google.cloud.tpu.v2.TpuClient;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class GetTpuVm {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "tyaho-softserve-project";//"YOUR_PROJECT_ID";
    String zone = "europe-west4-a";
    String tpuVmName = "my-tpu-vm111";

    getTpuVm(projectId, zone, tpuVmName);
  }

  // Describes a TPU VM with the specified name in the given project and zone.
  public static Node getTpuVm(String projectId, String zone, String tpuVmName)
      throws IOException{
    Node node;
    try (TpuClient tpuClient = TpuClient.create()) {
      String nodeName =
          NodeName.of(projectId, zone, tpuVmName).toString();

      GetNodeRequest request =
          GetNodeRequest.newBuilder().setName(nodeName).build();

      node = tpuClient.getNode(request);


      System.out.printf("TPU VM details: %s\n", node);
    }
    return node;
  }
}
//[END tpu_vm_get]
