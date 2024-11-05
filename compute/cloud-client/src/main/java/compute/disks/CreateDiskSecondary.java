package compute.disks;

import com.google.cloud.compute.v1.Disk;
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
    // Name of the region in which you want to create the secondary disk.
    // Learn more about zones and regions:
    // https://cloud.google.com/compute/docs/disks/async-pd/about#supported_region_pairs
    String primaryDiskRegion = "us-central1";
    // Name of the disk you want to create.
    String secondaryDiskName = "second-disk";//"SECONDARY_DISK_NAME";
    // Size of the new disk in gigabytes.
    // It should be less than or equal to 32 TiB.
    long diskSizeGb = 30;
    // Name of the primary disk you want to use as a source for the new disk.
    String primaryDiskName = "disk-1";//"PRIMARY_DISK_NAME";
    String secondaryDiskRegion = "us-east1";
    // The type of the disk you want to create. This value uses the following format:
    // "projects/{projectId}/zones/{zone}/diskTypes/
    // (pd-standard|pd-ssd|pd-balanced|pd-extreme)".
    String diskType = String.format(
        "projects/%s/regions/%s/diskTypes/pd-balanced", projectId, secondaryDiskRegion);
    createDiskSecondaryRegional(projectId, secondaryDiskName, primaryDiskRegion,
        diskSizeGb, primaryDiskName,  diskType, secondaryDiskRegion);
  }

  // Creates a secondary disk in a specified zone with the source disk information.
  public static Disk createDiskSecondaryRegional(String projectId, String secondaryDiskName,
     String primaryDiskRegion, long diskSizeGb, String primaryDiskName, String diskType, String secondaryDiskRegion)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // An iterable collection of zone names in which you want to keep
    // the new disks' replicas. One of the replica zones of the clone must match
    // the zone of the source disk.
    List<String> replicaZones = Arrays.asList(
        String.format("projects/%s/zones/%s-c", projectId, secondaryDiskRegion),
        String.format("projects/%s/zones/%s-b", projectId, secondaryDiskRegion));

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (RegionDisksClient disksClient = RegionDisksClient.create()) {
      String primaryDiskSource = String.format("projects/%s/regions/%s/disks/%s",
          projectId, primaryDiskRegion, primaryDiskName);
      // Create the disk object with the source disk information.
      Disk disk = Disk.newBuilder()
          .addAllReplicaZones(replicaZones)
          .setName(secondaryDiskName)
          .setSizeGb(diskSizeGb)
          .setType(diskType)
          .setRegion(secondaryDiskRegion)
          .setSourceDisk(primaryDiskSource).build();

      // Wait for the create disk operation to complete.
      Operation response = disksClient.insertAsync(projectId, secondaryDiskRegion, disk)
          .get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        return null;
      }
      return disksClient.get(projectId, primaryDiskRegion, secondaryDiskName);
    }
  }
}