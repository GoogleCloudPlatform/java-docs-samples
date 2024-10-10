package compute.tpu;

import com.google.cloud.tpu.v2.ListNodesRequest;
import com.google.cloud.tpu.v2.Node;
import com.google.cloud.tpu.v2.TpuClient;

import java.io.IOException;

public class ListTpuVms {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "tyaho-softserve-project";//"YOUR_PROJECT_ID";
    String zone = "europe-west4-a";

    listTpuVms(projectId, zone);
  }

  // Lists TPU VMs in the specified zone.
  public static TpuClient.ListNodesPagedResponse listTpuVms(String projectId, String zone) throws IOException {
    TpuClient.ListNodesPagedResponse response;
    try (TpuClient tpuClient = TpuClient.create()) {
      String parent = String.format("projects/%s/locations/%s", projectId, zone);

      ListNodesRequest request = ListNodesRequest.newBuilder().setParent(parent).build();

      response = tpuClient.listNodes(request);

      System.out.println("TPU VMs:");
      for (Node node : response.iterateAll()) {
        System.out.printf("  - Name: %s\n", node.getName());
        System.out.printf("    Accelerator Type: %s\n", node.getAcceleratorType());
      }
    }
    return response;
  }
}
