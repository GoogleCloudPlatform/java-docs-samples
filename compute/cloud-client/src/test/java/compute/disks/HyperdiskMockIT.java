package compute.disks;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.compute.v1.InsertStoragePoolRequest;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.StoragePool;
import com.google.cloud.compute.v1.StoragePoolsClient;
import compute.disks.storagepool.CreateHyperdiskStoragePool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.junit.Assert.assertEquals;


@RunWith(JUnit4.class)
@Timeout(value = 10)
public class HyperdiskMockIT {
  private static final String PROJECT_ID = "project-id";
  private static final String ZONE = "us-west1-a";
  private static final String HYPERDISK_NAME = "test-hyperdisk";
  private static final String  HYPERDISK_IN_POOL_NAME = "test-hyperdisk";
  private static final String STORAGE_POOL_NAME = "test-storage-pool";
  private static final String PERFORMANCE_PROVISIONING_TYPE = "advanced";
  private static final String CAPACITY_PROVISIONING_TYPE = "advanced";

  @Test
  public void testCreateHyperdiskStoragePool() throws Exception {
    String poolType = String.format("projects/%s/zones/%s/storagePoolTypes/hyperdisk-balanced",
            PROJECT_ID, ZONE);

    try (MockedStatic<StoragePoolsClient> mockedStoragePoolsClient = mockStatic(StoragePoolsClient.class)) {
//      StoragePool mockStoragePool = mock(StoragePool.class);
      StoragePoolsClient mockClient = mock(StoragePoolsClient.class);
      OperationFuture mockFuture = mock(OperationFuture.class);
      Operation.Status mockOperationStatus = Operation.Status.DONE;
//      OperationFuture<StoragePool, Operation> mockFuture = mock(OperationFuture.class, Mockito.RETURNS_DEEP_STUBS);
      StoragePool mockStoragePool = StoragePool.newBuilder().setName(STORAGE_POOL_NAME).build();
      mockedStoragePoolsClient.when(StoragePoolsClient::create).thenReturn(mockClient);
      when(mockClient.insertAsync(any(InsertStoragePoolRequest.class))).thenReturn(mockFuture);
      when(mockFuture.get()).thenReturn(mockOperationStatus);
      when(mockClient.get(PROJECT_ID, ZONE, STORAGE_POOL_NAME)).thenReturn(mockStoragePool);


      Operation.Status returnedStatus = CreateHyperdiskStoragePool
              .createHyperdiskStoragePool(PROJECT_ID, ZONE, STORAGE_POOL_NAME, poolType,
                      CAPACITY_PROVISIONING_TYPE, 10240, 10000, 1024,
                      PERFORMANCE_PROVISIONING_TYPE);

      verify(mockClient, times(1))
              .insertAsync(any(InsertStoragePoolRequest.class));
      verify(mockFuture, times(1)).get();
      assertEquals(returnedStatus, mockOperationStatus);
    }
  }
}
