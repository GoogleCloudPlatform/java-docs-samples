/*
 * Copyright 2020 Google LLC
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

package com.example.cloud.bigtable.scheduledbackups;

import static com.google.common.truth.Truth.assertThat;

import com.google.api.gax.grpc.GrpcStatusCode;
import com.google.api.gax.longrunning.OperationFuture;
import com.google.api.gax.longrunning.OperationFutures;
import com.google.api.gax.longrunning.OperationSnapshot;
import com.google.api.gax.rpc.OperationCallable;
import com.google.api.gax.rpc.UnaryCallable;
import com.google.api.gax.rpc.testing.FakeOperationSnapshot;
import com.google.bigtable.admin.v2.CreateBackupMetadata;
import com.google.cloud.bigtable.admin.v2.BigtableTableAdminClient;
import com.google.cloud.bigtable.admin.v2.internal.NameUtil;
import com.google.cloud.bigtable.admin.v2.models.Backup;
import com.google.cloud.bigtable.admin.v2.models.CreateBackupRequest;
import com.google.cloud.bigtable.admin.v2.stub.EnhancedBigtableTableAdminStub;
import com.google.longrunning.Operation;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import io.grpc.Status.Code;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.threeten.bp.Instant;


@RunWith(JUnit4.class)
public final class CreateBackupTest {
  @Rule public final MockitoRule mockitoRule = MockitoJUnit.rule();

  private static final String PROJECT_ID = "my-project";
  private static final String INSTANCE_ID = "my-instance";
  private static final String TABLE_ID = "my-table";
  private static final String CLUSTER_ID = "my-cluster";
  private static final String BACKUP_ID = "my-backup";

  private static final String TABLE_NAME =
      NameUtil.formatTableName(PROJECT_ID, INSTANCE_ID, TABLE_ID);

  private BigtableTableAdminClient adminClient;
  @Mock private EnhancedBigtableTableAdminStub mockStub;

  @Mock
  private UnaryCallable<com.google.bigtable.admin.v2.CreateBackupRequest, Operation>
      mockCreateBackupCallable;

  @Mock
  private OperationCallable<
          com.google.bigtable.admin.v2.CreateBackupRequest,
          com.google.bigtable.admin.v2.Backup,
          CreateBackupMetadata>
      mockCreateBackupOperationCallable;

  @Before
  public void setUp() {
    adminClient = BigtableTableAdminClient.create(PROJECT_ID, INSTANCE_ID, mockStub);

    Mockito.when(mockStub.createBackupOperationCallable())
        .thenReturn(mockCreateBackupOperationCallable);
    Mockito.when(mockStub.createBackupCallable()).thenReturn(mockCreateBackupCallable);
  }

  @Test
  public void close() {
    adminClient.close();
    Mockito.verify(mockStub).close();
  }

  @Test
  public void testCreateBackup() {
    // Setup
    String backupName = NameUtil.formatBackupName(PROJECT_ID, INSTANCE_ID, CLUSTER_ID, BACKUP_ID);
    Timestamp startTime = Timestamp.newBuilder().setSeconds(123).build();
    Timestamp endTime = Timestamp.newBuilder().setSeconds(456).build();
    Timestamp expireTime = Timestamp.newBuilder().setSeconds(789).build();
    long sizeBytes = 123456789;
    CreateBackupRequest req =
        CreateBackupRequest.of(CLUSTER_ID, BACKUP_ID).setSourceTableId(TABLE_ID);
    mockOperationResult(
        mockCreateBackupOperationCallable,
        req.toProto(PROJECT_ID, INSTANCE_ID),
        com.google.bigtable.admin.v2.Backup.newBuilder()
            .setName(backupName)
            .setSourceTable(TABLE_NAME)
            .setStartTime(startTime)
            .setEndTime(endTime)
            .setExpireTime(expireTime)
            .setSizeBytes(sizeBytes)
            .build(),
        CreateBackupMetadata.newBuilder()
            .setName(backupName)
            .setStartTime(startTime)
            .setEndTime(endTime)
            .setSourceTable(TABLE_NAME)
            .build());
    // Execute
    Backup actualResult = adminClient.createBackup(req);

    // Verify
    assertThat(actualResult.getId()).isEqualTo(BACKUP_ID);
    assertThat(actualResult.getSourceTableId()).isEqualTo(TABLE_ID);
    assertThat(actualResult.getStartTime())
        .isEqualTo(Instant.ofEpochMilli(Timestamps.toMillis(startTime)));
    assertThat(actualResult.getEndTime())
        .isEqualTo(Instant.ofEpochMilli(Timestamps.toMillis(endTime)));
    assertThat(actualResult.getExpireTime())
        .isEqualTo(Instant.ofEpochMilli(Timestamps.toMillis(expireTime)));
    assertThat(actualResult.getSizeBytes()).isEqualTo(sizeBytes);
  }

  private <ReqT, RespT, MetaT> void mockOperationResult(
      OperationCallable<ReqT, RespT, MetaT> callable,
      ReqT request,
      RespT response,
      MetaT metadata) {
    OperationSnapshot operationSnapshot =
        FakeOperationSnapshot.newBuilder()
            .setDone(true)
            .setErrorCode(GrpcStatusCode.of(Code.OK))
            .setName("fake-name")
            .setResponse(response)
            .setMetadata(metadata)
            .build();
    OperationFuture<RespT, MetaT> operationFuture =
        OperationFutures.immediateOperationFuture(operationSnapshot);
    Mockito.when(callable.futureCall(request)).thenReturn(operationFuture);
  }
}
