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

// [START compute_custom_machine_type_create_with_helper]

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

public class CreateWithHelper {

  // This class defines the configurable parameters for a custom VM.
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

  public enum CpuSeries {
    N1("custom"),
    N2("n2-custom"),
    N2D("n2d-custom"),
    E2("e2-custom"),
    E2_MICRO("e2-custom-micro"),
    E2_SMALL("e2-custom-small"),
    E2_MEDIUM("e2-custom-medium");

    private static final Map<String, CpuSeries> ENUM_MAP;

    static {
      ENUM_MAP = init();
    }

    // Build an immutable map of String name to enum pairs.
    public static Map<String, CpuSeries> init() {
      Map<String, CpuSeries> map = new ConcurrentHashMap<>();
      for (CpuSeries instance : CpuSeries.values()) {
        map.put(instance.getCpuSeries(), instance);
      }
      return Collections.unmodifiableMap(map);
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

  // This enum correlates a machine type with its limits.
  // The limits for various CPU types are described in:
  // https://cloud.google.com/compute/docs/general-purpose-machines
  enum Limits {
    CPUSeries_E2(new TypeLimits(getNumsInRangeWithStep(2, 33, 2), 512, 8192, false, 0)),
    CPUSeries_E2MICRO(new TypeLimits(new int[]{}, 1024, 2048, false, 0)),
    CPUSeries_E2SMALL(new TypeLimits(new int[]{}, 2048, 4096, false, 0)),
    CPUSeries_E2MEDIUM(new TypeLimits(new int[]{}, 4096, 8192, false, 0)),
    CPUSeries_N2(
        new TypeLimits(concat(getNumsInRangeWithStep(2, 33, 2), getNumsInRangeWithStep(36, 129, 4)),
            512, 8192, true, gbToMb(624))),
    CPUSeries_N2D(
        new TypeLimits(new int[]{2, 4, 8, 16, 32, 48, 64, 80, 96}, 512, 8192, true, gbToMb(768))),
    CPUSeries_N1(
        new TypeLimits(concat(new int[]{1}, getNumsInRangeWithStep(2, 97, 2)), 922, 6656, true,
            gbToMb(624)));

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

  // Returns the array of integers within the given range, incremented by the specified step.
  // start (inclusive): starting number of the range
  // stop (inclusive): ending number of the range
  // step : increment value
  static int[] getNumsInRangeWithStep(int start, int stop, int step) {
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
    String zone = "google-cloud-zone";
    // Name of the new virtual machine (VM) instance.
    String instanceName = "instance-name";
    String cpuSeries = "N1";
    // Number of CPU cores you want to use.
    int coreCount = 2;
    // The amount of memory for the VM instance, in megabytes.
    int memory = 256;

    createInstanceWithCustomMachineTypeWithHelper(
        projectId, zone, instanceName, cpuSeries, coreCount, memory);
  }

  // Create a VM instance with a custom machine type.
  public static void createInstanceWithCustomMachineTypeWithHelper(
      String project, String zone, String instanceName, String cpuSeries, int coreCount, int memory)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Construct the URI string identifying the machine type.
    String machineTypeUri = customMachineTypeUri(zone, cpuSeries, coreCount, memory);

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the `instancesClient.close()` method on the client to safely
    // clean up any remaining background resources.
    try (InstancesClient instancesClient = InstancesClient.create()) {

      AttachedDisk attachedDisk = AttachedDisk.newBuilder()
          .setInitializeParams(
              // Describe the size and source image of the boot disk to attach to the instance.
              // The list of public images available in Compute Engine can be found here:
              // https://cloud.google.com/compute/docs/images#list_of_public_images_available_on
              AttachedDiskInitializeParams.newBuilder()
                  .setSourceImage(
                      String.format("projects/%s/global/images/family/%s", "debian-cloud",
                          "debian-11"))
                  .setDiskSizeGb(10)
                  .build()
          )
          // Remember to set auto_delete to True if you want the disk to be deleted when you delete
          // your VM instance.
          .setAutoDelete(true)
          .setBoot(true)
          .build();

      // Create the Instance object with the relevant information.
      Instance instance = Instance.newBuilder()
          .setName(instanceName)
          .addDisks(attachedDisk)
          .setMachineType(machineTypeUri)
          .addNetworkInterfaces(
              NetworkInterface.newBuilder().setName("global/networks/default").build())
          .build();

      // Create the insert instance request object.
      InsertInstanceRequest insertInstanceRequest = InsertInstanceRequest.newBuilder()
          .setProject(project)
          .setZone(zone)
          .setInstanceResource(instance)
          .build();

      // Invoke the API with the request object and wait for the operation to complete.
      Operation response = instancesClient.insertAsync(insertInstanceRequest)
          .get(3, TimeUnit.MINUTES);

      // Check for errors.
      if (response.hasError()) {
        throw new Error("Instance creation failed!!" + response);
      }
      System.out.printf("Instance created : %s", instanceName);
      System.out.println("Operation Status: " + response.getStatus());
    }
  }

  public static String customMachineTypeUri(String zone, String cpuSeries, int coreCount,
      int memory) {

    if (!Arrays.asList(CpuSeries.E2.cpuSeries, CpuSeries.N1.cpuSeries, CpuSeries.N2.cpuSeries,
        CpuSeries.N2D.cpuSeries).contains(cpuSeries)) {
      throw new Error(String.format("Incorrect cpu type: %s", cpuSeries));
    }

    TypeLimits typeLimit = Objects.requireNonNull(
        typeLimitsMap.get(CpuSeries.get(cpuSeries).name())).typeLimits;

    // Perform the following checks to verify if the requested parameters are allowed.
    // Find more information about limitations of custom machine types at:
    // https://cloud.google.com/compute/docs/general-purpose-machines#custom_machine_types

    // 1. Check the number of cores and if the coreCount is present in allowedCores.
    if (typeLimit.allowedCores.length > 0 && Arrays.stream(typeLimit.allowedCores)
        .noneMatch(x -> x == coreCount)) {
      throw new Error(String.format(
          "Invalid number of cores requested. "
              + "Number of cores requested for CPU %s should be one of: %s",
          cpuSeries,
          Arrays.toString(typeLimit.allowedCores)));
    }

    // 2. Memory must be a multiple of 256 MB
    if (memory % 256 != 0) {
      throw new Error("Requested memory must be a multiple of 256 MB");
    }

    // 3. Check if the requested memory isn't too little
    if (memory < coreCount * typeLimit.minMemPerCore) {
      throw new Error(
          String.format("Requested memory is too low. Minimum memory for %s is %s MB per core",
              cpuSeries, typeLimit.minMemPerCore));
    }

    // 4. Check if the requested memory isn't too much
    if (memory > coreCount * typeLimit.maxMemPerCore && !typeLimit.allowExtraMemory) {
      throw new Error(String.format(
          "Requested memory is too large.. Maximum memory allowed for %s is %s MB per core",
          cpuSeries, typeLimit.extraMemoryLimit));
    }

    // 5. Check if the requested memory isn't too large
    if (memory > typeLimit.extraMemoryLimit && typeLimit.allowExtraMemory) {
      throw new Error(
          String.format("Requested memory is too large.. Maximum memory allowed for %s is %s MB",
              cpuSeries, typeLimit.extraMemoryLimit));
    }

    // Check if the CPU Series is E2 and return the custom machine type in the form of a string
    // acceptable by Compute Engine API.
    if (Arrays.asList(CpuSeries.E2_SMALL.cpuSeries, CpuSeries.E2_MICRO.cpuSeries,
        CpuSeries.E2_MEDIUM.cpuSeries).contains(cpuSeries)) {
      return String.format("zones/%s/machineTypes/%s-%s", zone, cpuSeries, memory);
    }

    // Check if extended memory was requested and return the extended custom machine type
    // in the form of a string acceptable by Compute Engine API.
    if (memory > coreCount * typeLimit.maxMemPerCore) {
      return String.format("zones/%s/machineTypes/%s-%s-%s-ext", zone, cpuSeries, coreCount,
          memory);
    }

    // Return the custom machine type in the form of a standard string
    // acceptable by Compute Engine API.
    return String.format("zones/%s/machineTypes/%s-%s-%s", zone, cpuSeries, coreCount, memory);
  }
}
// [END compute_custom_machine_type_create_with_helper]