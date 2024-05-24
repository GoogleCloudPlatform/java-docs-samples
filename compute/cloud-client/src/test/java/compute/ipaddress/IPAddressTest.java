/*
 * Copyright 2024 Google LLC
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

package compute.ipaddress;

import static com.google.common.truth.Truth.assertWithMessage;

import com.google.api.gax.rpc.NotFoundException;
import compute.windows.windowsinstances.CreateWindowsServerInstanceExternalIp;
import com.google.api.gax.longrunning.OperationFuture;
import com.google.api.gax.rpc.ApiException;
import com.google.cloud.compute.v1.AccessConfig;
import com.google.cloud.compute.v1.Address;
import com.google.cloud.compute.v1.AddressesClient;
import com.google.cloud.compute.v1.GlobalAddressesClient;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.NetworkInterface;
import compute.DeleteInstance;
import compute.Util;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@Timeout(value = 10, unit = TimeUnit.MINUTES)
public class IPAddressTest {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ZONE = "us-central1-b";
  private static final String REGION = "us-central1";
  private static String MACHINE_NAME;
  private static String EXTERNAL_NEW_VM_INSTANCE;
  private static final List<String> ADDRESSES = new ArrayList<>();
  private static final List<String> GLOBAL_ADDRESSES = new ArrayList<>();

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
            .that(System.getenv(envVarName)).isNotEmpty();
  }

  @BeforeClass
  public static void setUp()
          throws IOException, InterruptedException, ExecutionException, TimeoutException {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");

    MACHINE_NAME = "my-new-ip-test-instance" + UUID.randomUUID();
    EXTERNAL_NEW_VM_INSTANCE = "my-new-ip-test-instance" + UUID.randomUUID();

    // Cleanup existing stale resources.
    Util.cleanUpExistingInstances("my-new-ip-test-instance", PROJECT_ID, ZONE);

    CreateWindowsServerInstanceExternalIp
            .createWindowsServerInstanceExternalIp(PROJECT_ID, ZONE, MACHINE_NAME);

    TimeUnit.SECONDS.sleep(5);
  }

  @AfterClass
  public static void cleanup()
          throws IOException, InterruptedException, ExecutionException, TimeoutException {
    // Delete all instances created for testing.
    DeleteInstance.deleteInstance(PROJECT_ID, ZONE, MACHINE_NAME);
    DeleteInstance.deleteInstance(PROJECT_ID, ZONE, EXTERNAL_NEW_VM_INSTANCE);


    try (GlobalAddressesClient client = GlobalAddressesClient.create()) {
      for (String globalAddress : GLOBAL_ADDRESSES) {
        deleteResource(() -> client.deleteAsync(PROJECT_ID, globalAddress));
      }
    }
    try (AddressesClient client = AddressesClient.create()) {
      for (String address : ADDRESSES) {
        deleteResource(() -> client.deleteAsync(PROJECT_ID, REGION, address));
      }
    }
  }

  private static <T> void deleteResource(Supplier<OperationFuture<T, T>> supplier) {
    try {
      supplier.get().get(30, TimeUnit.SECONDS);
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }

  @Test
  public void getVMAddressInternalTest() throws IOException {
    List<String> vmAddress = GetVMAddress.getVMAddress(PROJECT_ID, MACHINE_NAME, IPType.INTERNAL);
    Assert.assertNotNull(vmAddress);
    Assert.assertFalse(vmAddress.isEmpty());
    Assert.assertTrue(vmAddress.get(0)
            .matches("^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$"));
  }

  @Test
  public void getVMAddressExternalTest()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    ReserveNewExternalAddress.
            reserveNewExternalIPAddress(PROJECT_ID, getNewAddressName(true), false, false, null);
    List<String> vmAddress = GetVMAddress.getVMAddress(PROJECT_ID, MACHINE_NAME, IPType.EXTERNAL);
    Assert.assertNotNull(vmAddress);
    Assert.assertFalse(vmAddress.isEmpty());
    Assert.assertTrue(vmAddress.get(0)
            .matches("^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$"));
  }

  @Test
  public void getVMAddressIPV6Test() throws IOException {
    List<String> vmAddress = GetVMAddress.getVMAddress(PROJECT_ID, MACHINE_NAME, IPType.IP_V6);
    Assert.assertNotNull(vmAddress);
    Assert.assertTrue(vmAddress.isEmpty());
  }

  @Test
  public void reserveNewExternalIPAddressTest()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    String addressName = getNewAddressName(true);
    List<Address> addresses = ReserveNewExternalAddress.
            reserveNewExternalIPAddress(PROJECT_ID, addressName, false, false, null);
    Assert.assertNotNull(addresses);
    Assert.assertFalse(addresses.isEmpty());
    Assert.assertTrue(addresses.stream().anyMatch(address -> address.getName().equals(addressName)));

    String regionAddressName = getNewAddressName(false);
    addresses = ReserveNewExternalAddress.
            reserveNewExternalIPAddress(PROJECT_ID, regionAddressName, false, true, REGION);
    Assert.assertNotNull(addresses);
    Assert.assertFalse(addresses.isEmpty());
    Assert.assertTrue(addresses.stream().anyMatch(address -> address.getName().equals(regionAddressName)));
  }

  @Test
  public void assignStaticExternalNewVMAddressTest()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    String ipAddress = getExternalIpAddress(getNewAddressName(false));
    String machineType = String.format("zones/%s/machineTypes/n1-standard-1", ZONE);
    Instance instance = AssignStaticExternalNewVMAddress.assignStaticExternalNewVMAddress(
            PROJECT_ID, EXTERNAL_NEW_VM_INSTANCE, ZONE, true, machineType, ipAddress);
    Assert.assertNotNull(instance);
    Assert.assertFalse(instance.getNetworkInterfacesList().isEmpty());
    Assert.assertFalse(instance.getNetworkInterfacesList().get(0).getAccessConfigsList().isEmpty());
    AccessConfig accessConfig = instance.getNetworkInterfacesList().get(0)
            .getAccessConfigsList().get(0);
    Assert.assertEquals(ipAddress, accessConfig.getNatIP());
  }

  @Test
  public void assignStaticExistingVMAddressTest()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    String ipAddress = getExternalIpAddress(getNewAddressName(false));
    Instance instance = AssignStaticExistingVm.assignStaticExistingVMAddress(
            PROJECT_ID, MACHINE_NAME, ZONE, ipAddress, "nic0");
    Assert.assertNotNull(instance);
    Assert.assertFalse(instance.getNetworkInterfacesList().isEmpty());
    Assert.assertFalse(instance.getNetworkInterfacesList().get(0).getAccessConfigsList().isEmpty());
    AccessConfig accessConfig = instance.getNetworkInterfacesList().get(0)
            .getAccessConfigsList().get(0);
    Assert.assertEquals(ipAddress, accessConfig.getNatIP());
  }

  @Test
  public void promoteEphemeralIdTest()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {

    String ipAddress = null;
    try (InstancesClient client = InstancesClient.create()) {
      Instance instance = client.get(PROJECT_ID, ZONE, EXTERNAL_NEW_VM_INSTANCE);
      for (NetworkInterface networkInterface : instance.getNetworkInterfacesList()) {
        for (AccessConfig accessConfig : networkInterface.getAccessConfigsList()) {
          if (accessConfig.getType().equals(AccessConfig.Type.ONE_TO_ONE_NAT.name())) {
            ipAddress = accessConfig.getNatIP();
            break;
          }
        }
      }
    }

    List<Address> addresses = PromoteEphemeralIp.
            promoteEphemeralId(PROJECT_ID, REGION, ipAddress, getNewAddressName(false));

    Assert.assertNotNull(addresses);
    Assert.assertFalse(addresses.isEmpty());

    String fIpAddress = ipAddress;
    Assert.assertTrue(addresses.stream().anyMatch(address -> address.getAddress().equals(fIpAddress)
            && address.getStatus().equals(Address.Status.IN_USE.name())));
  }

  @Test
  public void listStaticExternalIpTest() throws IOException {
    List<Address> addresses = ListStaticExternalIp.listStaticExternalIp(PROJECT_ID, REGION);
    Assert.assertNotNull(addresses);
    Assert.assertFalse(addresses.isEmpty());
    Assert.assertTrue(addresses.stream().allMatch(address -> address.getRegion().contains(REGION)));

    addresses = ListStaticExternalIp.listStaticExternalIp(PROJECT_ID, null);
    Assert.assertNotNull(addresses);
    Assert.assertFalse(addresses.isEmpty());
    Assert.assertTrue(addresses.stream().noneMatch(Address::hasRegion));
  }

  @Test
  public void getStaticIPAddressTest() throws IOException {
    String addressName = getNewAddressName(false);
    Address address = GetStaticIPAddress.getStaticIPAddress(PROJECT_ID, REGION, addressName);
    Assert.assertNotNull(address);
    Assert.assertEquals(addressName, address.getAddress());
    Assert.assertEquals(REGION, address.getRegion());

    address = GetStaticIPAddress.getStaticIPAddress(PROJECT_ID, null, addressName);
    Assert.assertNotNull(address);
    Assert.assertEquals(addressName, address.getAddress());
    Assert.assertFalse(address.hasRegion());
  }

  @Test
  public void unassignStaticIPAddressTest()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    String netInterfaceName = "nic0";
    Instance instance = UnassignStaticIPAddress.unassignStaticIPAddress(
            PROJECT_ID, MACHINE_NAME, ZONE, netInterfaceName);
    Assert.assertNotNull(instance);
    Assert.assertFalse(instance.getNetworkInterfacesList().isEmpty());

    String type = AccessConfig.Type.ONE_TO_ONE_NAT.name();
    Assert.assertFalse(instance.getNetworkInterfacesList().stream()
            .filter(networkInterface -> networkInterface.getName().equals(netInterfaceName))
            .anyMatch(networkInterface ->
                    networkInterface.getAccessConfigsList().stream()
                            .anyMatch(accessConfig -> accessConfig.getType().equals(type))));

  }

  @Test
  public void releaseStaticIPAddress()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    String addressName = getNewAddressName(false);
    getExternalIpAddress(addressName);
    ReleaseStaticAddress.releaseStaticAddress(PROJECT_ID, addressName, REGION);
    Assert.assertThrows(".getStaticIPAddress() should throw NotFoundException",
            NotFoundException.class,
            () -> GetStaticIPAddress
                    .getStaticIPAddress(PROJECT_ID, REGION, addressName));
  }

  private String getExternalIpAddress(String addressName)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    try (AddressesClient client = AddressesClient.create()) {
      return client.get(PROJECT_ID, REGION, addressName).getAddress();
    } catch (ApiException e) {
      return ReserveNewExternalAddress.
              reserveNewExternalIPAddress(PROJECT_ID, addressName, false, true, REGION)
              .get(0).getAddress();
    }
  }

  private String getNewAddressName(boolean isGlobal) {
    String newAddress = "my-new-address-test" + UUID.randomUUID();
    if (isGlobal) {
      GLOBAL_ADDRESSES.add(newAddress);
    } else {
      ADDRESSES.add(newAddress);
    }
    return newAddress;
  }
}