package compute.tpu;

//[START tpu_vm_delete]

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.tpu.v2.DeleteNodeRequest;
import com.google.cloud.tpu.v2.NodeName;
import com.google.cloud.tpu.v2.OperationMetadata;
import com.google.cloud.tpu.v2.TpuClient;
import com.google.protobuf.Empty;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class DeleteTpuVm {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "tyaho-softserve-project";//"YOUR_PROJECT_ID";
    String zone = "europe-west4-a";
    String tpuVmName = "my-tpu-vm111";

    deleteTpuVm(projectId, zone, tpuVmName);
  }

  // Deletes a TPU VM with the specified name in the given project and zone.
  public static void deleteTpuVm(String projectId, String zone, String tpuVmName)
      throws IOException, ExecutionException, InterruptedException {
    try (TpuClient tpuClient = TpuClient.create()) {
      String nodeName =
          NodeName.of(projectId, zone, tpuVmName).toString();

      DeleteNodeRequest request =
          DeleteNodeRequest.newBuilder().setName(nodeName).build();

  OperationFuture<Empty, OperationMetadata> future = tpuClient.deleteNodeAsync(request);
      future.get();

//      Node response = tpuClient.deleteNodeAsync(request).get();

      System.out.printf("TPU VM deleted: %s\n", future);
    }
  }
}
//[END tpu_vm_delete]
