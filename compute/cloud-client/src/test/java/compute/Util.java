/*
 * Copyright 2022 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package compute;

import static compute.disks.consistencygroup.DeleteDiskConsistencyGroup.deleteDiskConsistencyGroup;

import com.google.cloud.compute.v1.DeleteStoragePoolRequest;
import com.google.cloud.compute.v1.Disk;
import com.google.cloud.compute.v1.DisksClient;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstanceTemplate;
import com.google.cloud.compute.v1.InstanceTemplatesClient;
import com.google.cloud.compute.v1.InstanceTemplatesClient.ListPagedResponse;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.ListRegionInstanceTemplatesRequest;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.RegionDisksClient;
import com.google.cloud.compute.v1.RegionInstanceTemplatesClient;
import com.google.cloud.compute.v1.Reservation;
import com.google.cloud.compute.v1.ReservationsClient;
import com.google.cloud.compute.v1.ResourcePoliciesClient;
import com.google.cloud.compute.v1.ResourcePolicy;
import com.google.cloud.compute.v1.Snapshot;
import com.google.cloud.compute.v1.SnapshotsClient;
import com.google.cloud.compute.v1.StoragePool;
import com.google.cloud.compute.v1.StoragePoolsClient;
import compute.deleteprotection.SetDeleteProtection;
import compute.disks.DeleteDisk;
import compute.disks.DeleteSnapshot;
import compute.reservation.DeleteReservation;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;

public abstract class Util {
  // Cleans existing test resources if any.
  // If the project contains too many instances, use "filter" when listing
  // resources
  // and delete the listed resources based on the timestamp.

  private static final int DELETION_THRESHOLD_TIME_MINUTES = 30;
  // comma separate list of zone names
  private static final String TEST_ZONES_NAME = "JAVA_DOCS_COMPUTE_TEST_ZONES";
  private static final String DEFAULT_ZONES = "us-central1-a,us-west1-a,asia-south1-a";

  // Delete templates which starts with the given prefixToDelete and
  // has creation timestamp >24 hours.
  public static void cleanUpExistingInstanceTemplates(String prefixToDelete, String projectId)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    try (InstanceTemplatesClient instanceTemplatesClient = InstanceTemplatesClient.create()) {
      ListPagedResponse templates = instanceTemplatesClient.list(projectId);
      for (InstanceTemplate instanceTemplate : templates.iterateAll()) {
        if (containPrefixToDelete(instanceTemplate, prefixToDelete)
            && isCreatedBeforeThresholdTime(instanceTemplate.getCreationTimestamp())
            && instanceTemplate.isInitialized()) {
          DeleteInstanceTemplate.deleteInstanceTemplate(projectId, instanceTemplate.getName());
        }
      }
    }
  }

  // Delete regional instance templates which starts with the given prefixToDelete and
  // has creation timestamp >24 hours.
  public static void cleanUpExistingRegionalInstanceTemplates(
      String prefixToDelete, String projectId, String zone)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    try (RegionInstanceTemplatesClient instanceTemplatesClient =
             RegionInstanceTemplatesClient.create()) {
      String region = zone.substring(0, zone.lastIndexOf('-'));
      ListRegionInstanceTemplatesRequest request =
          ListRegionInstanceTemplatesRequest.newBuilder()
              .setProject(projectId)
              .setRegion(region)
              .build();

      for (InstanceTemplate instanceTemplate :
          instanceTemplatesClient.list(request).iterateAll()) {
        if (containPrefixToDeleteAndZone(instanceTemplate, prefixToDelete, zone)
            && isCreatedBeforeThresholdTime(instanceTemplate.getCreationTimestamp())
            && instanceTemplate.isInitialized()) {
          DeleteRegionalInstanceTemplate.deleteRegionalInstanceTemplate(
              projectId, region, instanceTemplate.getName());
        }
      }
    }
  }

  // Delete instances which starts with the given prefixToDelete and
  // has creation timestamp >24 hours.
  public static void cleanUpExistingInstances(String prefixToDelete, String projectId,
                                              String instanceZone)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    try (InstancesClient instancesClient = InstancesClient.create()) {
      for (Instance instance : instancesClient.list(projectId, instanceZone).iterateAll()) {
        if (instance.getDeletionProtection()
            && isCreatedBeforeThresholdTime(instance.getCreationTimestamp())) {
          SetDeleteProtection.setDeleteProtection(
              projectId, instanceZone, instance.getName(), false);
        }
        if (containPrefixToDeleteAndZone(instance, prefixToDelete, instanceZone)
            && isCreatedBeforeThresholdTime(instance.getCreationTimestamp())) {
          DeleteInstance.deleteInstance(projectId, instanceZone, instance.getName());
        }
      }
    }
  }

  public static boolean isCreatedBeforeThresholdTime(String timestamp) {
    return OffsetDateTime.parse(timestamp).toInstant()
        .isBefore(Instant.now().minus(DELETION_THRESHOLD_TIME_MINUTES, ChronoUnit.MINUTES));
  }

  public static String getBase64EncodedKey() {
    String sampleSpace = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    StringBuilder stringBuilder = new StringBuilder();
    SecureRandom random = new SecureRandom();
    IntStream.range(0, 32)
        .forEach(
            x -> stringBuilder.append(sampleSpace.charAt(random.nextInt(sampleSpace.length()))));

    return Base64.getEncoder()
        .encodeToString(stringBuilder.toString().getBytes(StandardCharsets.US_ASCII));
  }

  public static String getInstanceStatus(String project, String zone, String instanceName)
      throws IOException {
    try (InstancesClient instancesClient = InstancesClient.create()) {
      Instance response = instancesClient.get(project, zone, instanceName);
      return response.getStatus();
    }
  }

  public static Instance getInstance(String projectId, String zone, String machineName)
      throws IOException {
    try (InstancesClient instancesClient = InstancesClient.create()) {
      return instancesClient.get(projectId, zone, machineName);
    }
  }

  public static Disk getDisk(String projectId, String zone, String diskName) throws IOException {
    try (DisksClient disksClient = DisksClient.create()) {
      return disksClient.get(projectId, zone, diskName);
    }
  }

  public static Disk getRegionalDisk(String projectId, String region, String diskName)
      throws IOException {
    try (RegionDisksClient regionDisksClient = RegionDisksClient.create()) {
      return regionDisksClient.get(projectId, region, diskName);
    }
  }

  // Returns a random zone.
  public static String getZone() {
    String zones = getEnvVar(TEST_ZONES_NAME, DEFAULT_ZONES);
    String[] parsedZones = zones.split(",");
    if (parsedZones.length == 0) {
      return "unknown";
    }
    return parsedZones[new Random().nextInt(parsedZones.length)].trim();
  }

  public static String getEnvVar(String envVarName, String defaultValue) {
    String val = System.getenv(envVarName);
    if (val == null || val.trim() == "") {
      return defaultValue;
    }
    return val;
  }

  // Delete reservations which starts with the given prefixToDelete and
  // has creation timestamp >24 hours.
  public static void cleanUpExistingReservations(
      String prefixToDelete, String projectId, String zone)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    try (ReservationsClient reservationsClient = ReservationsClient.create()) {
      for (Reservation reservation : reservationsClient.list(projectId, zone).iterateAll()) {
        if (containPrefixToDeleteAndZone(reservation, prefixToDelete, zone)
            && isCreatedBeforeThresholdTime(reservation.getCreationTimestamp())) {
          DeleteReservation.deleteReservation(projectId, zone, reservation.getName());
        }
      }
    }
  }

  // Delete disks which starts with the given prefixToDelete and
  // has creation timestamp >24 hours.
  public static void cleanUpExistingDisks(
      String prefixToDelete, String projectId, String zone)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    try (DisksClient disksClient = DisksClient.create()) {
      for (Disk disk : disksClient.list(projectId, zone).iterateAll()) {
        if (containPrefixToDeleteAndZone(disk, prefixToDelete, zone)
            && isCreatedBeforeThresholdTime(disk.getCreationTimestamp())) {
          DeleteDisk.deleteDisk(projectId, zone, disk.getName());
        }
      }
    }
  }

  // Delete snapshots which starts with the given prefixToDelete and
  // has creation timestamp >24 hours.
  public static void cleanUpExistingSnapshots(String prefixToDelete, String projectId)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    try (SnapshotsClient snapshotsClient = SnapshotsClient.create()) {
      for (Snapshot snapshot : snapshotsClient.list(projectId).iterateAll()) {
        if (containPrefixToDelete(snapshot, prefixToDelete)
            && isCreatedBeforeThresholdTime(snapshot.getCreationTimestamp())) {
          DeleteSnapshot.deleteSnapshot(projectId, snapshot.getName());
        }
      }
    }
  }

  // Delete storagePools which starts with the given prefixToDelete and
  // has creation timestamp >24 hours.
  public static void cleanUpExistingStoragePool(
      String prefixToDelete, String projectId, String zone)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    try (StoragePoolsClient storagePoolsClient = StoragePoolsClient.create()) {
      for (StoragePool storagePool : storagePoolsClient.list(projectId, zone).iterateAll()) {
        if (containPrefixToDeleteAndZone(storagePool, prefixToDelete, zone)
            && isCreatedBeforeThresholdTime(storagePool.getCreationTimestamp())) {
          deleteStoragePool(projectId, zone, storagePool.getName());
        }
      }
    }
  }

  public static void deleteStoragePool(String project, String zone, String storagePoolName)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    try (StoragePoolsClient storagePoolsClient = StoragePoolsClient.create()) {
      DeleteStoragePoolRequest request =
          DeleteStoragePoolRequest.newBuilder()
              .setProject(project)
              .setZone(zone)
              .setStoragePool(storagePoolName)
              .build();
      Operation operation = storagePoolsClient.deleteAsync(request).get(3, TimeUnit.MINUTES);
      if (operation.hasError()) {
        System.out.println("StoragePool deletion failed!");
        throw new Error(operation.getError().toString());
      }
      // Wait for server update
      TimeUnit.SECONDS.sleep(50);
      System.out.println("Deleted storage pool: " + storagePoolName);
    }
  }

  // Delete storagePools which starts with the given prefixToDelete and
  // has creation timestamp >24 hours.
  public static void cleanUpExistingConsistencyGroup(
          String prefixToDelete, String projectId, String region)
          throws IOException, ExecutionException, InterruptedException {
    try (ResourcePoliciesClient client = ResourcePoliciesClient.create()) {
      for (ResourcePolicy resourcePolicy : client.list(projectId, region).iterateAll()) {
        if (containPrefixToDeleteAndZone(resourcePolicy, prefixToDelete, region)
                && isCreatedBeforeThresholdTime(resourcePolicy.getCreationTimestamp())) {
          System.out.println(resourcePolicy.getName());
          deleteDiskConsistencyGroup(projectId, region, resourcePolicy.getName());
        }
      }
    }
  }

  public static boolean containPrefixToDeleteAndZone(
      Object resource, String prefixToDelete, String zone) {
    boolean containPrefixAndZone = false;
    try {
      if (resource instanceof Instance) {
        containPrefixAndZone = ((Instance) resource).getName().contains(prefixToDelete)
            && ((Instance) resource).getZone().contains(zone);
      }
      if (resource instanceof InstanceTemplate) {
        containPrefixAndZone = ((InstanceTemplate) resource).getName().contains(prefixToDelete)
            && ((InstanceTemplate) resource).getRegion()
            .contains(zone.substring(0, zone.lastIndexOf('-')));
      }
      if (resource instanceof Reservation) {
        containPrefixAndZone = ((Reservation) resource).getName().contains(prefixToDelete)
            && ((Reservation) resource).getZone().contains(zone);
      }
      if (resource instanceof Disk) {
        containPrefixAndZone = ((Disk) resource).getName().contains(prefixToDelete)
            && ((Disk) resource).getZone().contains(zone);
      }
      if (resource instanceof StoragePool) {
        containPrefixAndZone = ((StoragePool) resource).getName().contains(prefixToDelete)
            && ((StoragePool) resource).getZone().contains(zone);
      }
      if (resource instanceof ResourcePolicy) {
        containPrefixAndZone = ((ResourcePolicy) resource).getName().contains(prefixToDelete)
                && ((ResourcePolicy) resource).getRegion()
                .contains(zone.substring(0, zone.lastIndexOf('-')));
      }
    } catch (NullPointerException e) {
      System.out.println("Resource not found, skipping deletion:");
    }
    return containPrefixAndZone;
  }

  public static boolean containPrefixToDelete(
      Object resource, String prefixToDelete) {
    boolean containPrefixToDelete = false;
    try {
      if (resource instanceof InstanceTemplate) {
        containPrefixToDelete = ((InstanceTemplate) resource).getName().contains(prefixToDelete);
      }
      if (resource instanceof Snapshot) {
        containPrefixToDelete = ((Snapshot) resource).getName().contains(prefixToDelete);
      }
    } catch (NullPointerException e) {
      System.out.println("Resource not found, skipping deletion:");
    }
    return containPrefixToDelete;
  }
}
