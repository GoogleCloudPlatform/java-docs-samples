/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.bigquerystorage;

// [START bigquerystorage_quickstart]

import com.google.api.gax.rpc.ServerStream;
import com.google.cloud.bigquery.storage.v1beta1.ArrowProto.ArrowRecordBatch;
import com.google.cloud.bigquery.storage.v1beta1.ArrowProto.ArrowSchema;
import com.google.cloud.bigquery.storage.v1beta1.BigQueryStorageClient;
import com.google.cloud.bigquery.storage.v1beta1.ReadOptions.TableReadOptions;
import com.google.cloud.bigquery.storage.v1beta1.Storage;
import com.google.cloud.bigquery.storage.v1beta1.Storage.CreateReadSessionRequest;
import com.google.cloud.bigquery.storage.v1beta1.Storage.DataFormat;
import com.google.cloud.bigquery.storage.v1beta1.Storage.ReadRowsRequest;
import com.google.cloud.bigquery.storage.v1beta1.Storage.ReadRowsResponse;
import com.google.cloud.bigquery.storage.v1beta1.Storage.ReadSession;
import com.google.cloud.bigquery.storage.v1beta1.Storage.StreamPosition;
import com.google.cloud.bigquery.storage.v1beta1.TableReferenceProto.TableModifiers;
import com.google.cloud.bigquery.storage.v1beta1.TableReferenceProto.TableReference;
import com.google.common.base.Preconditions;
import com.google.protobuf.Timestamp;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.FieldVector;
import org.apache.arrow.vector.VectorLoader;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.apache.arrow.vector.ipc.ReadChannel;
import org.apache.arrow.vector.ipc.message.MessageSerializer;
import org.apache.arrow.vector.types.pojo.Field;
import org.apache.arrow.vector.types.pojo.Schema;
import org.apache.arrow.vector.util.ByteArrayReadableSeekableByteChannel;

public class StorageArrowSample {

  /*
   * SimpleRowReader handles deserialization of the Apache Arrow-encoded row batches transmitted
   * from the storage API using a generic datum decoder.
   */
  private static class SimpleRowReader implements AutoCloseable {

    BufferAllocator allocator = new RootAllocator(Long.MAX_VALUE);

    // Decoder object will be reused to avoid re-allocation and too much garbage collection.
    private final VectorSchemaRoot root;
    private final VectorLoader loader;


    public SimpleRowReader(ArrowSchema arrowSchema) throws IOException {
      Schema schema = MessageSerializer.deserializeSchema(new ReadChannel(
          new ByteArrayReadableSeekableByteChannel(
              arrowSchema.getSerializedSchema().toByteArray())));
      Preconditions.checkNotNull(schema);
      List<FieldVector> vectors = new ArrayList<>();
      for (Field field : schema.getFields()) {
        vectors.add(field.createVector(allocator));
      }
      root = new VectorSchemaRoot(vectors);
      loader = new VectorLoader(root);
    }

    /**
     * Sample method for processing Arrow data which only validates decoding.
     *
     * @param batch object returned from the ReadRowsResponse.
     */
    public void processRows(ArrowRecordBatch batch) throws IOException {
      org.apache.arrow.vector.ipc.message.ArrowRecordBatch deserializedBatch = MessageSerializer
          .deserializeRecordBatch(
              new ReadChannel(new ByteArrayReadableSeekableByteChannel(
                  batch.getSerializedRecordBatch().toByteArray())),
              allocator);

      loader.load(deserializedBatch);
      // Release buffers from batch (they are still held in the vectors in root).
      deserializedBatch.close();
      System.out.println(root.contentToTSVString());
      // Release buffers from vectors in root.
      root.clear();

    }

    @Override
    public void close() {
      root.close();
      allocator.close();
    }
  }

  public static void main(String... args) throws Exception {
    // Sets your Google Cloud Platform project ID.
    // String projectId = "YOUR_PROJECT_ID";
    String projectId = args[0];
    Integer snapshotMillis = null;
    if (args.length > 1) {
      snapshotMillis = Integer.parseInt(args[1]);
    }

    try (BigQueryStorageClient client = BigQueryStorageClient.create()) {
      String parent = String.format("projects/%s", projectId);

      // This example uses baby name data from the public datasets.
      TableReference tableReference =
          TableReference.newBuilder()
              .setProjectId("bigquery-public-data")
              .setDatasetId("usa_names")
              .setTableId("usa_1910_current")
              .build();

      // We specify the columns to be projected by adding them to the selected fields,
      // and set a simple filter to restrict which rows are transmitted.
      TableReadOptions options =
          TableReadOptions.newBuilder()
              .addSelectedFields("name")
              .addSelectedFields("number")
              .addSelectedFields("state")
              .setRowRestriction("state = \"WA\"")
              .build();

      // Begin building the session request.
      CreateReadSessionRequest.Builder builder =
          CreateReadSessionRequest.newBuilder()
              .setParent(parent)
              .setTableReference(tableReference)
              .setReadOptions(options)
              // This API can also deliver data serialized in Apache Avro format.
              // This example leverages Apache Arrow.
              .setFormat(DataFormat.ARROW)
              // We use a LIQUID strategy in this example because we only
              // read from a single stream.  Consider BALANCED if you're consuming
              // multiple streams concurrently and want more consistent stream sizes.
              .setShardingStrategy(Storage.ShardingStrategy.LIQUID)
              .setRequestedStreams(1);

      // Optionally specify the snapshot time.  When unspecified, snapshot time is "now".
      if (snapshotMillis != null) {
        Timestamp t =
            Timestamp.newBuilder()
                .setSeconds(snapshotMillis / 1000)
                .setNanos((int) ((snapshotMillis % 1000) * 1000000))
                .build();
        TableModifiers modifiers = TableModifiers.newBuilder().setSnapshotTime(t).build();
        builder.setTableModifiers(modifiers);
      }

      ReadSession session = client.createReadSession(builder.build());
      // Setup a simple reader and start a read session.
      try (SimpleRowReader reader = new SimpleRowReader(session.getArrowSchema())) {

        // Assert that there are streams available in the session.  An empty table may not have
        // data available.  If no sessions are available for an anonymous (cached) table, consider
        // writing results of a query to a named table rather than consuming cached results
        // directly.
        Preconditions.checkState(session.getStreamsCount() > 0);

        // Use the first stream to perform reading.
        StreamPosition readPosition =
            StreamPosition.newBuilder().setStream(session.getStreams(0)).build();

        ReadRowsRequest readRowsRequest =
            ReadRowsRequest.newBuilder().setReadPosition(readPosition).build();

        // Process each block of rows as they arrive and decode using our simple row reader.
        ServerStream<ReadRowsResponse> stream = client.readRowsCallable().call(readRowsRequest);
        for (ReadRowsResponse response : stream) {
          Preconditions.checkState(response.hasArrowRecordBatch());
          reader.processRows(response.getArrowRecordBatch());
        }
      }
    }
  }
}

// [END bigquerystorage_quickstart]
