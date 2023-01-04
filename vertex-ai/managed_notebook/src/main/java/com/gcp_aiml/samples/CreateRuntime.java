package com.gcp_aiml.samples;

/** Required imports */
import com.google.cloud.notebooks.v1.CreateRuntimeRequest;
import com.google.cloud.notebooks.v1.LocalDisk;
import com.google.cloud.notebooks.v1.LocalDiskInitializeParams;
import com.google.cloud.notebooks.v1.LocalDiskInitializeParams.DiskType;
import com.google.cloud.notebooks.v1.ManagedNotebookServiceClient;
import com.google.cloud.notebooks.v1.Runtime;
import com.google.cloud.notebooks.v1.RuntimeAccessConfig;
import com.google.cloud.notebooks.v1.RuntimeAccessConfig.RuntimeAccessType;
import com.google.cloud.notebooks.v1.VirtualMachine;
import com.google.cloud.notebooks.v1.VirtualMachineConfig;

/** Vertex AI Managed Notebook Sample */
public class CreateRuntime {
  // Tune these parameters for your sample
  private static String serviceAccount = "000000000007-compute@developer.gserviceaccount.com";
  private static String parentProject  = "projects/example-project-id/locations/us-central1";
  private static String runtimeId      = "example-vertexai-notebook";
  private static String machineType    = "n1-standard-4";

  public static void main(String[] args) {
    System.out.println("Creating Managed Notebook!");
    try {
      syncCreateRuntime();
    } catch (Exception e) {
      System.out.println(e);
    }
  }

  public static void syncCreateRuntime() throws Exception {
    try (ManagedNotebookServiceClient managedNotebookServiceClient =
        ManagedNotebookServiceClient.create()) {

      RuntimeAccessConfig accessConfig =
          RuntimeAccessConfig.newBuilder()
              .setRuntimeOwner(serviceAccount)
              .setAccessType(RuntimeAccessType.SERVICE_ACCOUNT)
              .build();

      LocalDisk datDisk =
          LocalDisk.newBuilder()
              .setInitializeParams(
                  LocalDiskInitializeParams.newBuilder()
                      .setDiskSizeGb(100)
                      .setDiskType(DiskType.PD_STANDARD)
                      .build())
              .build();

      VirtualMachineConfig machineConfig =
          VirtualMachineConfig.newBuilder()
              .setMachineType(machineType)
              .setDataDisk(datDisk)
              .build();

      VirtualMachine virtualMachine =
          VirtualMachine.newBuilder().setVirtualMachineConfig(machineConfig).build();

      Runtime runtime =
          Runtime.newBuilder()
              .setAccessConfig(accessConfig)
              .setVirtualMachine(virtualMachine)
              .build();

      CreateRuntimeRequest request =
          CreateRuntimeRequest.newBuilder()
              .setParent(parentProject)
              .setRuntimeId(runtimeId)
              .setRuntime(runtime)
              .build();

      Runtime response = managedNotebookServiceClient.createRuntimeAsync(request).get();

      System.out.println(response);
    }
  }
}
