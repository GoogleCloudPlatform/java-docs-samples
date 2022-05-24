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

package compute.custommachinetype;

// [START compute_custom_machine_type_create_shared_with_helper]

import com.google.cloud.compute.v1.AttachedDisk;
import com.google.cloud.compute.v1.AttachedDiskInitializeParams;
import com.google.cloud.compute.v1.InsertInstanceRequest;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.NetworkInterface;
import com.google.cloud.compute.v1.Operation;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;

public class CreateInstanceWithCustomSharedCore {

  public enum CpuSeries {
    N1("custom"),
    N2("n2-custom"),
    N2D("n2d-custom"),
    E2("e2-custom"),
    E2_MICRO("e2-custom-micro"),
    E2_SMALL("e2-custom-small"),
    E2_MEDIUM("e2-custom-medium");

    private static final Map<String, CpuSeries> ENUM_MAP;

    // Build an immutable map of String name to enum pairs.
    static {
      Map<String, CpuSeries> map = new ConcurrentHashMap<>();
      for (CpuSeries instance : CpuSeries.values()) {
        map.put(instance.getCpuSeries(), instance);
      }
      ENUM_MAP = Collections.unmodifiableMap(map);
    }

    private final String cpuSeries;

    CpuSeries(String cpuSeries) {
      this.cpuSeries = cpuSeries;
    }

    public static CpuSeries get(String name) {
      return ENUM_MAP.get(name);
    }

    public String getCpuSeries() {
      return this.cpuSeries;
    }
  }

  static final class TypeLimits {

    int[] allowedCores;
    int minMemPerCore;
    int maxMemPerCore;
    int extraMemoryLimit;
    boolean allowExtraMemory;

    TypeLimits(int[] allowedCores, int minMemPerCore, int maxMemPerCore, boolean allowExtraMemory,
        int extraMemoryLimit) {
      this.allowedCores = allowedCores;
      this.minMemPerCore = minMemPerCore;
      this.maxMemPerCore = maxMemPerCore;
      this.allowExtraMemory = allowExtraMemory;
      this.extraMemoryLimit = extraMemoryLimit;
    }
  }

  // The limits for various CPU types are described on:
  // https://cloud.google.com/compute/docs/general-purpose-machines
  enum Limits {
    CPUSeries_E2(new TypeLimits(range(2, 33, 2), 512, 8192, false, 0)),
    CPUSeries_E2MICRO(new TypeLimits(new int[]{}, 1024, 2048, false, 0)),
    CPUSeries_E2SMALL(new TypeLimits(new int[]{}, 2048, 4096, false, 0)),
    CPUSeries_E2MEDIUM(new TypeLimits(new int[]{}, 4096, 8192, false, 0)),
    CPUSeries_N2(
        new TypeLimits(concat(range(2, 33, 2), range(36, 129, 4)), 512, 8192, true, gbToMb(624))),
    CPUSeries_N2D(
        new TypeLimits(new int[]{2, 4, 8, 16, 32, 48, 64, 80, 96}, 512, 8192, true, gbToMb(768))),
    CPUSeries_N1(
        new TypeLimits(concat(new int[]{1}, range(2, 97, 2)), 922, 6656, true, gbToMb(624)));

    private final TypeLimits typeLimits;

    Limits(TypeLimits typeLimits) {
      this.typeLimits = typeLimits;
    }

    public TypeLimits getTypeLimits() {
      return typeLimits;
    }
  }

  static ImmutableMap<String, Limits> typeLimitsMap = ImmutableMap.<String, Limits>builder()
      .put("N1", Limits.CPUSeries_N1)
      .put("N2", Limits.CPUSeries_N2)
      .put("N2D", Limits.CPUSeries_N2D)
      .put("E2", Limits.CPUSeries_E2)
      .put("E2_MICRO", Limits.CPUSeries_E2MICRO)
      .put("E2_SMALL", Limits.CPUSeries_E2SMALL)
      .put("E2_MEDIUM", Limits.CPUSeries_E2SMALL)
      .build();

  static int[] range(int start, int stop, int step) {
    return IntStream.range(start, stop).filter(x -> (x - start) % step == 0).toArray();
  }

  static int gbToMb(int value) {
    return value << 10;
  }

  static int[] concat(int[] a, int[] b) {
    int[] result = new int[a.length + b.length];
    System.arraycopy(a, 0, result, 0, a.length);
    System.arraycopy(b, 0, result, a.length, b.length);
    return result;
  }

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "your-google-cloud-project-id";
    // Name of the zone to create the instance in. For example: "us-west3-b".
    String zone = "gcloud-zone";
    // Name of the new virtual machine (VM) instance.
    String instanceName = "instance-name";
    String cpuSeries = "N1";
    // The amount of memory for the VM instance, in megabytes.
    int memory = 256;

    createInstanceWithCustomSharedCore(projectId, zone, instanceName, cpuSeries, memory);
  }

  // Construct custom machine type.
  public static String customMachineTypeSharedCoreUri(String zone, String cpuSeries, int memory) {

    if (!Arrays.asList(CpuSeries.E2_SMALL.cpuSeries, CpuSeries.E2_MICRO.cpuSeries,
        CpuSeries.E2_MEDIUM.cpuSeries).contains(cpuSeries)) {
      return String.format("Incorrect cpu type: %s", cpuSeries);
    }

    int coreCount = 2;

    TypeLimits typeLimit = Objects.requireNonNull(
        typeLimitsMap.get(CpuSeries.get(cpuSeries).name())).typeLimits;

    // Check whether the requested parameters are allowed.
    // Find more information about limitations of custom machine types at:
    // https://cloud.google.com/compute/docs/general-purpose-machines#custom_machine_types

    // Check the number of cores and if the coreCount is present in allowedCores.
    if (typeLimit.allowedCores.length > 0 && Arrays.stream(typeLimit.allowedCores)
        .noneMatch(x -> x == coreCount)) {
      return String.format(
          "Invalid number of cores requested. Allowed number of cores for %s is: %s",
          cpuSeries,
          Arrays.toString(typeLimit.allowedCores));
    }

    // Memory must be a multiple of 256 MB
    if (memory % 256 != 0) {
      return "Requested memory must be a multiple of 256 MB";
    }

    // Check if the requested memory isn't too little
    if (memory < coreCount * typeLimit.minMemPerCore) {
      return String.format("Requested memory is too low. Minimal memory for %s is %s MB per core",
          cpuSeries, typeLimit.minMemPerCore);
    }

    // Check if the requested memory isn't too much
    if (memory > coreCount * typeLimit.maxMemPerCore && !typeLimit.allowExtraMemory) {
      return String.format(
          "Requested memory is too large.. Maximum memory allowed for %s is %s MB per core",
          cpuSeries, typeLimit.extraMemoryLimit);
    }

    if (memory > typeLimit.extraMemoryLimit && typeLimit.allowExtraMemory) {
      return String.format("Requested memory is too large.. Maximum memory allowed for %s is %s MB",
          cpuSeries, typeLimit.extraMemoryLimit);
    }

    // Return the custom machine type in form of a string acceptable by Compute Engine API.
    if (Arrays.asList(CpuSeries.E2_SMALL.cpuSeries, CpuSeries.E2_MICRO.cpuSeries,
        CpuSeries.E2_MEDIUM.cpuSeries).contains(cpuSeries)) {
      return String.format("zones/%s/machineTypes/%s-%s", zone, cpuSeries, memory);
    }

    if (memory > coreCount * typeLimit.maxMemPerCore) {
      return String.format("zones/%s/machineTypes/%s-%s-%s-ext", zone, cpuSeries, coreCount,
          memory);
    }

    return String.format("zones/%s/machineTypes/%s-%s-%s", zone, cpuSeries, coreCount, memory);
  }

  // Create a new VM instance with a custom type using shared CPUs.
  public static Instance createInstanceWithCustomSharedCore(String project, String zone,
      String instanceName, String cpuSeries, int memory)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Construct the machine type.
    String machineTypeResponse = customMachineTypeSharedCoreUri(zone, cpuSeries, memory);
    // Check for error string.
    if (!machineTypeResponse.startsWith("zones/")) {
      System.out.printf("Unable to create custom machine type string: %s%n", machineTypeResponse);
      return Instance.getDefaultInstance();
    }

    try (InstancesClient instancesClient = InstancesClient.create()) {

      AttachedDisk attachedDisk = AttachedDisk.newBuilder()
          .setInitializeParams(
              // Describe the size and source image of the boot disk to attach to the instance.
              AttachedDiskInitializeParams.newBuilder()
                  .setSourceImage(
                      String.format("projects/debian-cloud/global/images/family/%s", "debian-11"))
                  .setDiskSizeGb(10)
                  .build()
          )
          .setAutoDelete(true)
          .setBoot(true)
          .setType(AttachedDisk.Type.PERSISTENT.name())
          .build();

      // Collect information into the Instance object.
      Instance instance = Instance.newBuilder()
          .setName(instanceName)
          .addDisks(attachedDisk)
          .setMachineType(machineTypeResponse)
          .addNetworkInterfaces(
              NetworkInterface.newBuilder().setName("global/networks/default").build())
          .build();

      // Prepare the request to insert an instance.
      InsertInstanceRequest insertInstanceRequest = InsertInstanceRequest.newBuilder()
          .setProject(project)
          .setZone(zone)
          .setInstanceResource(instance)
          .build();

      // Wait for the operation to complete.
      Operation response = instancesClient.insertAsync(insertInstanceRequest)
          .get(3, TimeUnit.MINUTES);
      // Check for errors.
      if (response.hasError()) {
        System.out.println("Instance creation failed ! ! " + response);
        return Instance.getDefaultInstance();
      }
      System.out.printf("Instance created : %s", instanceName);
      System.out.println("Operation Status: " + response.getStatus());

      return instancesClient.get(project, zone, instanceName);
    }
  }
}
// [END compute_custom_machine_type_create_shared_with_helper]