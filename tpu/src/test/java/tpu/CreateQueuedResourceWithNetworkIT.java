package tpu;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.tpu.v2.Node;
import com.google.cloud.tpu.v2alpha1.CreateQueuedResourceRequest;
import com.google.cloud.tpu.v2alpha1.GetNodeRequest;
import com.google.cloud.tpu.v2alpha1.NetworkConfig;
import com.google.cloud.tpu.v2alpha1.QueuedResource;
import com.google.cloud.tpu.v2alpha1.TpuClient;
import com.google.longrunning.Operation;
import org.junit.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@Timeout(value = 25, unit = TimeUnit.MINUTES)
public class CreateQueuedResourceWithNetworkIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ZONE = "europe-west4-a";
  static String javaVersion = System.getProperty("java.version").substring(0, 2);
  private static final String NODE_NAME = "test-tpu-queued-resource-network-" + javaVersion + "-"
      + UUID.randomUUID().toString().substring(0, 8);
  private static final String TPU_TYPE = "v2-8";
  private static final String TPU_SOFTWARE_VERSION = "tpu-vm-tf-2.14.1";
  private static final String QUEUED_RESOURCE_NAME = "queued-resource-network-" + javaVersion + "-"
      + UUID.randomUUID().toString().substring(0, 8);


  @BeforeAll
  public static void setUp() throws IOException {

    // Cleanup existing stale resources.
    Util.cleanUpExistingQueuedResources("queued-resource-network-", PROJECT_ID, ZONE);
  }



  @Test
  public void shouldCreateQueuedResourceWithSpecifiedNetwork() throws Exception {
    final PrintStream out = System.out;
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
    String parent = String.format("projects/%s/locations/%s", PROJECT_ID, ZONE);
    TpuClient mockTpuClient = mock(TpuClient.class);

    NetworkConfig networkConfig = NetworkConfig.newBuilder()
        .setNetwork(String.format("projects/%s/global/networks/compute-tpu-network", PROJECT_ID))
        .setSubnetwork(String
            .format("projects/%s/regions/europe-west4/subnetworks/compute-tpu-network", PROJECT_ID))
        .setEnableExternalIps(true)
        .build();

com.google.cloud.tpu.v2alpha1.Node node =
    com.google.cloud.tpu.v2alpha1.Node.newBuilder()
        .setName(NODE_NAME)
        .setAcceleratorType(TPU_TYPE)
        .setRuntimeVersion(TPU_SOFTWARE_VERSION)
        .setNetworkConfig(networkConfig)
        .setQueuedResource(
            String.format(
                "projects/%s/locations/%s/queuedResources/%s",
                PROJECT_ID, ZONE, QUEUED_RESOURCE_NAME))
        .build();

    QueuedResource queuedResource =
        QueuedResource.newBuilder()
            .setName(QUEUED_RESOURCE_NAME)
            .setTpu(
                QueuedResource.Tpu.newBuilder()
                    .addNodeSpec(
                        QueuedResource.Tpu.NodeSpec.newBuilder()
                            .setParent(parent)
                            .setNode(node)
                            .setNodeId(NODE_NAME)
                            .build())
                    .build())
            .build();

    CreateQueuedResourceRequest request =
        CreateQueuedResourceRequest.newBuilder()
            .setParent(parent)
            .setQueuedResource(queuedResource)
            .setQueuedResourceId(QUEUED_RESOURCE_NAME)
            .build();
    OperationFuture mockFuture = mock(OperationFuture.class);
    when(mockTpuClient.createQueuedResourceAsync(request))
        .thenReturn(mockFuture);

    // We should mock this operation as we don't have another project to share with.
    // Without mocking these test will fail.
    Operation mockOperation = mock(Operation.class);
    when(mockFuture.get(3, TimeUnit.MINUTES)).thenReturn(mockOperation);
    when(mockOperation.hasError()).thenReturn(false);

    CreateQueuedResourceWithNetwork creator= new
    CreateQueuedResourceWithNetwork(mockTpuClient);
  creator.createQueuedResourceWithNetwork(PROJECT_ID, ZONE, QUEUED_RESOURCE_NAME,
      NODE_NAME, TPU_TYPE, TPU_SOFTWARE_VERSION );

    verify(mockTpuClient, times(1))
        .createQueuedResourceAsync(request);
    assertThat(stdOut.toString()).contains(" Queued Resource created: " + QUEUED_RESOURCE_NAME);

      stdOut.close();
      System.setOut(out);
  }
}