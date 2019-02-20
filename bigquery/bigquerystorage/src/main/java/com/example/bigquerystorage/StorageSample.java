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
import com.google.cloud.bigquery.storage.v1beta1.AvroProto.AvroRows;
import com.google.cloud.bigquery.storage.v1beta1.BigQueryStorageClient;
import com.google.cloud.bigquery.storage.v1beta1.ReadOptions.TableReadOptions;
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
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;


public class StorageSample {

  /*
   * SimpleRowReader handles deserialization of the Avro-encoded row blocks transmitted
   * from the storage API using a generic datum decoder.
   */
  private static class SimpleRowReader {

    private final DatumReader<GenericRecord> datumReader;

    // Decoder object will be reused to avoid re-allocation and too much garbage collection.
    private BinaryDecoder decoder = null;

    // GenericRecord object will be reused.
    private GenericRecord row = null;

    public SimpleRowReader(Schema schema) {
      Preconditions.checkNotNull(schema);
      datumReader = new GenericDatumReader<>(schema);
    }

    /**
     * Sample method for processing AVRO rows which only validates decoding.
     *
     * @param avroRows object returned from the ReadRowsResponse.
     */
    public void processRows(AvroRows avroRows) throws IOException {
      decoder = DecoderFactory.get()
          .binaryDecoder(avroRows.getSerializedBinaryRows().toByteArray(), decoder);

      while (!decoder.isEnd()) {
        // Reusing object row
        row = datumReader.read(row, decoder);
        System.out.println(row.toString());
      }
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
      TableReference tableReference = TableReference.newBuilder()
          .setProjectId("bigquery-public-data")
          .setDatasetId("usa_names")
          .setTableId("usa_1910_current")
          .build();

      // We specify the columns to be projected by adding them to the selected fields,
      // and set a simple filter to restrict which rows are transmitted.
      TableReadOptions options = TableReadOptions.newBuilder()
          .addSelectedFields("name")
          .addSelectedFields("number")
          .addSelectedFields("state")
          .setRowRestriction("state = \"WA\"")
          .build();

      // Begin building the session request.
      CreateReadSessionRequest.Builder builder = CreateReadSessionRequest.newBuilder()
          .setParent(parent)
          .setTableReference(tableReference)
          .setReadOptions(options)
          .setRequestedStreams(1)
          .setFormat(DataFormat.AVRO);

      // Optionally specify the snapshot time.  When unspecified, snapshot time is "now".
      if (snapshotMillis != null) {
        Timestamp t = Timestamp.newBuilder()
            .setSeconds(snapshotMillis / 1000)
            .setNanos((int) ((snapshotMillis % 1000) * 1000000))
            .build();
        TableModifiers modifiers = TableModifiers.newBuilder()
            .setSnapshotTime(t)
            .build();
        builder.setTableModifiers(modifiers);
      }

      // Request the session creation.
      ReadSession session = client.createReadSession(builder.build());

      SimpleRowReader reader = new SimpleRowReader(
          new Schema.Parser().parse(session.getAvroSchema().getSchema()));

      // Assert that there are streams available in the session.  An empty table may not have
      // data available.  If no sessions are available for an anonymous (cached) table, consider
      // writing results of a query to a named table rather than consuming cached results directly.
      Preconditions.checkState(session.getStreamsCount() > 0);

      // Use the first stream to perform reading.
      StreamPosition readPosition = StreamPosition.newBuilder()
          .setStream(session.getStreams(0))
          .build();

      ReadRowsRequest readRowsRequest = ReadRowsRequest.newBuilder()
          .setReadPosition(readPosition)
          .build();

      // Process each block of rows as they arrive and decode using our simple row reader.
      ServerStream<ReadRowsResponse> stream = client.readRowsCallable().call(readRowsRequest);
      for (ReadRowsResponse response : stream) {
        Preconditions.checkState(response.hasAvroRows());
        reader.processRows(response.getAvroRows());
      }
      client.close();
    }
  }
}

// [END bigquerystorage_quickstart]
