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

package com.google.cloud.bigtable.examples.proxy.commands;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.bigtable.v2.BigtableGrpc;
import com.google.bigtable.v2.BigtableGrpc.BigtableBlockingStub;
import com.google.bigtable.v2.CheckAndMutateRowRequest;
import com.google.bigtable.v2.CheckAndMutateRowResponse;
import com.google.bigtable.v2.Mutation;
import com.google.bigtable.v2.Mutation.DeleteFromRow;
import com.google.bigtable.v2.ReadRowsRequest;
import com.google.bigtable.v2.ReadRowsResponse;
import com.google.bigtable.v2.RowFilter;
import com.google.bigtable.v2.RowFilter.Chain;
import com.google.bigtable.v2.RowSet;
import com.google.cloud.opentelemetry.metric.GoogleCloudMetricExporter;
import com.google.cloud.opentelemetry.metric.MetricConfiguration;
import com.google.cloud.opentelemetry.resource.GcpResource;
import com.google.cloud.opentelemetry.resource.ResourceTranslator;
import com.google.common.collect.ImmutableList;
import com.google.common.net.PercentEscaper;
import com.google.protobuf.ByteString;
import io.grpc.CallCredentials;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.Deadline;
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.Metadata.Key;
import io.grpc.MethodDescriptor;
import io.grpc.StatusRuntimeException;
import io.grpc.auth.MoreCallCredentials;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.contrib.gcp.resource.GCPResourceProvider;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableGaugeData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableLongPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;

@Command(name = "verify", description = "Verify environment is properly set up")
public class Verify implements Callable<Void> {
  @Option(
      names = "--bigtable-project-id",
      required = true,
      description = "Project that contains a Bigtable instance to use for connectivity test")
  String bigtableProjectId;

  @Option(
      names = "--bigtable-instance-id",
      required = true,
      description = "Bigtable instance to use for connectivity test")
  String bigtableInstanceId;

  @Option(
      names = "--bigtable-table-id",
      required = true,
      description = "Bigtable table to use for connectivity test")
  String bigtableTableId;

  @Option(
      names = "--metrics-project-id",
      required = true,
      description = "The project id where metrics should be exported")
  String metricsProjectId = null;

  @Option(
      names = "--bigtable-data-endpoint",
      converter = Endpoint.ArgConverter.class,
      showDefaultValue = Visibility.ALWAYS)
  Endpoint dataEndpoint = Endpoint.create("bigtable.googleapis.com", 443);


  Credentials credentials = null;

  @Override
  public Void call() throws Exception {
    if (credentials == null) {
      credentials = GoogleCredentials.getApplicationDefault();
    }
    checkBigtable(
        MoreCallCredentials.from(credentials),
        String.format(
            "projects/%s/instances/%s/tables/%s",
            bigtableProjectId, bigtableInstanceId, bigtableTableId));

    checkMetrics(credentials);
    return null;
  }

  private void checkBigtable(CallCredentials callCredentials, String tableName) {
    ManagedChannel channel =
        ManagedChannelBuilder.forAddress(dataEndpoint.getName(), dataEndpoint.getPort()).build();

    try {
      Metadata md = new Metadata();
      PercentEscaper escaper = new PercentEscaper("", true);
      md.put(
          Key.of("x-goog-request-params", Metadata.ASCII_STRING_MARSHALLER),
          String.format("table_name=%s&app_profile_id=%s", escaper.escape(tableName), ""));

      BigtableBlockingStub stub =
          BigtableGrpc.newBlockingStub(channel)
              .withCallCredentials(callCredentials)
              .withInterceptors(new MetadataInterceptor(md));

      ReadRowsRequest readRequest =
          ReadRowsRequest.newBuilder()
              .setTableName(
                  String.format(
                      "projects/%s/instances/%s/tables/%s",
                      bigtableProjectId, bigtableTableId, bigtableTableId))
              .setRowsLimit(1)
              .setRows(
                  RowSet.newBuilder().addRowKeys(ByteString.copyFromUtf8("some-nonexistent-row")))
              .setFilter(
                  RowFilter.newBuilder()
                      .setChain(
                          Chain.newBuilder()
                              .addFilters(RowFilter.newBuilder().setCellsPerRowLimitFilter(1))
                              .addFilters(
                                  RowFilter.newBuilder().setStripValueTransformer(true).build())))
              .build();

      Iterator<ReadRowsResponse> readIt =
          stub.withDeadline(Deadline.after(1, TimeUnit.SECONDS)).readRows(readRequest);

      try {
        while (readIt.hasNext()) {
          readIt.next();
        }
        System.out.println("Bigtable Read: OK");
      } catch (StatusRuntimeException e) {
        System.out.println("Bigtable Read: Failed - " + e.getStatus());
        return;
      }

      CheckAndMutateRowRequest rwReq =
          CheckAndMutateRowRequest.newBuilder()
              .setTableName(tableName)
              .setRowKey(ByteString.copyFromUtf8("some-non-existent-row"))
              .setPredicateFilter(RowFilter.newBuilder().setBlockAllFilter(true))
              .addTrueMutations(
                  Mutation.newBuilder().setDeleteFromRow(DeleteFromRow.getDefaultInstance()))
              .build();

      try {
        CheckAndMutateRowResponse ignored = stub.checkAndMutateRow(rwReq);
        System.out.println("Bigtable Read/Write: OK");
      } catch (StatusRuntimeException e) {
        System.out.println("Bigtable Read/Write: Failed - " + e.getStatus());
        return;
      }
    } finally {
      channel.shutdown();
    }
  }

  void checkMetrics(Credentials creds) throws IOException {
    Instant now = Instant.now().truncatedTo(ChronoUnit.MINUTES);
    Instant end = Instant.now().truncatedTo(ChronoUnit.MINUTES);

    GCPResourceProvider resourceProvider = new GCPResourceProvider();
    Resource resource = Resource.create(resourceProvider.getAttributes());
    GcpResource gcpResource = ResourceTranslator.mapResource(resource);

    MetricExporter exporter =
        GoogleCloudMetricExporter.createWithConfiguration(
            MetricConfiguration.builder()
                .setCredentials(creds)
                .setProjectId(metricsProjectId)
                .setInstrumentationLibraryLabelsEnabled(false)
                .build());

    ImmutableList<MetricData> metricData =
        ImmutableList.of(
            ImmutableMetricData.createLongGauge(
                resource,
                InstrumentationScopeInfo.create("bigtable-proxy"),
                "bigtableproxy.presence",
                "Number of proxy processes",
                "{process}",
                ImmutableGaugeData.create(
                    ImmutableList.of(
                        ImmutableLongPointData.create(
                            TimeUnit.MILLISECONDS.toNanos(now.toEpochMilli()),
                            TimeUnit.MILLISECONDS.toNanos(end.toEpochMilli()),
                            Attributes.empty(),
                            1L)))));
    CompletableResultCode result = exporter.export(metricData);
    result.join(1, TimeUnit.MINUTES);

    if (result.isSuccess()) {
      System.out.println("Metrics write: OK");
    } else {
      System.out.println("Metrics write: FAILED: " + result.getFailureThrowable().getMessage());
    }
  }

  private static class MetadataInterceptor implements ClientInterceptor {
    private final Metadata metadata;

    private MetadataInterceptor(Metadata metadata) {
      this.metadata = metadata;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
        MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
      return new SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {
        @Override
        public void start(Listener<RespT> responseListener, Metadata headers) {
          headers.merge(metadata);
          super.start(responseListener, headers);
        }
      };
    }
  }
}
