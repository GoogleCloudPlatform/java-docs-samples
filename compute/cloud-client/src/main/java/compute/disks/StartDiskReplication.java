package compute.disks;

import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.RegionDisksClient;
import com.google.cloud.compute.v1.RegionDisksStartAsyncReplicationRequest;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class StartDiskReplication {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException {
    // TODO(developer): Replace these variables before running the sample.
    // The project that contains the primary disk.
    String projectId = "tyaho-softserve-project";//"YOUR_PROJECT_ID";
    String primaryZone = "us-central1";
    String primaryDiskName = "primary-disk";//"PRIMARY_DISK_NAME";
    // Name of the zone in which you want to create the secondary disk.
    String secondaryZone = "us-central1";
    // Name of the disk you want to create.
    String secondaryDiskName = "second-disk";//"SECONDARY_DISK_NAME";

    startDiskAsyncReplication(
        projectId,
        primaryZone,
        primaryDiskName,
        secondaryZone,
        secondaryDiskName);
  }

  // Starts asynchronous replication for the specified disk.
  public static void startDiskAsyncReplication(
      String projectId,
      String primaryZone,
      String primaryDiskName,
      String secondaryZone,
      String secondaryDiskName)
      throws IOException, ExecutionException, InterruptedException {
    try (RegionDisksClient disksClient = RegionDisksClient.create()) {

      String asyncSecondaryDisk = String.format(
          "projects/%s/regions/%s/disks/%s",
          projectId,
          secondaryZone,
          secondaryDiskName);

      RegionDisksStartAsyncReplicationRequest startAsyncReplicationRequest =
          RegionDisksStartAsyncReplicationRequest.newBuilder()
              .setAsyncSecondaryDisk(asyncSecondaryDisk)
              .build();

      Operation response =
          disksClient.startAsyncReplicationAsync(
              projectId,
              primaryZone,
              primaryDiskName,
              startAsyncReplicationRequest).get();


      if (response.hasError()) {
        return;
      }
      System.out.println("Async replication started successfully.");
    }
  }
}
