package compute.disks;

import com.google.cloud.compute.v1.Disk;
import com.google.cloud.compute.v1.DisksClient;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.RegionDisksClient;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateDiskSecondary {
  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // The project that contains the primary disk.
    String projectId = "tyaho-softserve-project";//"YOUR_PROJECT_ID";
    // Name of the zone in which you want to create the secondary disk.
    String disksRegion = "us-central1";
    // Name of the disk you want to create.
    String secondaryDiskName = "second-disk";//"SECONDARY_DISK_NAME";
    // Size of the new disk in gigabytes.
    long diskSizeGb = 100;
    // Name of the primary disk you want to use as a source for the new disk.
    String primaryDiskName = "primary-disk";//"PRIMARY_DISK_NAME";
    // The type of the disk you want to create. This value uses the following format:
    // "projects/{projectId}/zones/{zone}/diskTypes/
    // (pd-standard|pd-ssd|pd-balanced|pd-extreme)".
    String diskType = String.format(
        "projects/%s/regions/%s/diskTypes/pd-balanced", projectId, disksRegion);
    String disksRegion2 = "us-east1";
    createDiskSecondaryRegional(projectId, secondaryDiskName, disksRegion,
        diskSizeGb, primaryDiskName,  diskType, disksRegion2);
  }

  // Creates a secondary disk in a specified zone with the source disk information.
  public static Disk createDiskSecondaryRegional(String projectId, String secondaryDiskName,
                                                 String disksRegion, long diskSizeGb, String primaryDiskName, String diskType, String disksRegion2)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // An iterable collection of zone names in which you want to keep
    // the new disks' replicas. One of the replica zones of the clone must match
    // the zone of the source disk.
    List<String> replicaZones = Arrays.asList(
        String.format("projects/%s/zones/%s-a", projectId, disksRegion2),
        String.format("projects/%s/zones/%s-b", projectId, disksRegion2));

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (RegionDisksClient disksClient = RegionDisksClient.create()) {
      String primaryDiskSource = String.format("projects/%s/regions/%s/disks/%s",
          projectId, disksRegion, primaryDiskName);
      // Create the disk object with the source disk information.
      Disk disk = Disk.newBuilder()
          .addAllReplicaZones(replicaZones)
          .setName(secondaryDiskName)
          .setSizeGb(diskSizeGb)
          .setType(diskType)
          .setRegion(disksRegion2)
          .setSourceDisk(primaryDiskSource).build();

      // Wait for the create disk operation to complete.
      Operation response = disksClient.insertAsync(projectId, disksRegion2, disk)
          .get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        return null;
      }
      return disksClient.get(projectId, disksRegion, secondaryDiskName);
    }
  }
}