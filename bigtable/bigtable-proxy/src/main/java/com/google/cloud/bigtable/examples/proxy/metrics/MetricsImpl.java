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

package com.google.cloud.bigtable.examples.proxy.metrics;

import com.google.auth.Credentials;
import com.google.auto.value.AutoValue;
import com.google.cloud.bigtable.examples.proxy.core.CallLabels;
import com.google.cloud.bigtable.examples.proxy.core.CallLabels.ParsingException;
import com.google.cloud.opentelemetry.metric.GoogleCloudMetricExporter;
import com.google.cloud.opentelemetry.metric.MetricConfiguration;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import io.grpc.ConnectivityState;
import io.grpc.Status;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.metrics.ObservableLongGauge;
import io.opentelemetry.contrib.gcp.resource.GCPResourceProvider;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableGaugeData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableLongPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central definition of all the {@link OpenTelemetry} metrics in this application.
 *
 * <p>The metric definition themselves are only accessible via typesafe record methods.
 */
@SuppressWarnings("ClassEscapesDefinedScope")
public class MetricsImpl implements Closeable, Metrics {
  private static final Logger LOG = LoggerFactory.getLogger(MetricsImpl.class);

  private static final InstrumentationScopeInfo INSTRUMENTATION_SCOPE_INFO =
      InstrumentationScopeInfo.builder("bigtable-proxy").setVersion("0.0.1").build();

  private static final String METRIC_PREFIX = "bigtableproxy.";

  private static final AttributeKey<String> API_CLIENT_KEY = AttributeKey.stringKey("api_client");
  private static final AttributeKey<String> RESOURCE_KEY = AttributeKey.stringKey("resource");
  private static final AttributeKey<String> APP_PROFILE_KEY = AttributeKey.stringKey("app_profile");
  private static final AttributeKey<String> METHOD_KEY = AttributeKey.stringKey("method");
  private static final AttributeKey<String> STATUS_KEY = AttributeKey.stringKey("status");

  private static final AttributeKey<String> PREV_CHANNEL_STATE =
      AttributeKey.stringKey("prev_state");
  private static final AttributeKey<String> CURRENT_CHANNEL_STATE =
      AttributeKey.stringKey("current_state");

  private static final String METRIC_PRESENCE_NAME = METRIC_PREFIX + "presence";
  private static final String METRIC_PRESENCE_DESC = "Number of proxy processes";
  private static final String METRIC_PRESENCE_UNIT = "{process}";

  private final MeterProvider meterProvider;

  private final DoubleHistogram gfeLatency;
  private final LongCounter gfeResponseHeadersMissing;
  private final DoubleHistogram clientCredLatencies;
  private final DoubleHistogram clientQueueLatencies;
  private final DoubleHistogram clientCallLatencies;
  private final DoubleHistogram clientCallFirstByteLatencies;
  private final DoubleHistogram downstreamLatencies;
  private final LongCounter serverCallsStarted;
  private final LongHistogram requestSizes;
  private final LongHistogram responseSizes;
  private final LongCounter channelStateChangeCounter;

  private final ObservableLongGauge outstandingRpcCountGauge;
  private final ObservableLongGauge presenceGauge;

  private final LongUpDownCounter channelCounter;
  private final AtomicInteger numOutstandingRpcs = new AtomicInteger();
  private final AtomicInteger maxSeen = new AtomicInteger();

  public MetricsImpl(Credentials credentials, String projectId) throws IOException {
    this(createMeterProvider(credentials, projectId));
  }

  private static SdkMeterProvider createMeterProvider(Credentials credentials, String projectId) {
    MetricConfiguration config =
        MetricConfiguration.builder()
            .setProjectId(projectId)
            .setCredentials(credentials)
            .setInstrumentationLibraryLabelsEnabled(false)
            .build();

    MetricExporter exporter = GoogleCloudMetricExporter.createWithConfiguration(config);

    return SdkMeterProvider.builder()
        .setResource(Resource.create(new GCPResourceProvider().getAttributes()))
        .registerMetricReader(
            PeriodicMetricReader.builder(exporter).setInterval(Duration.ofMinutes(1)).build())
        .build();
  }

  MetricsImpl(MeterProvider meterProvider) {
    this.meterProvider = meterProvider;
    @SuppressWarnings("DataFlowIssue")
    Meter meter =
        meterProvider
            .meterBuilder(INSTRUMENTATION_SCOPE_INFO.getName())
            .setInstrumentationVersion(INSTRUMENTATION_SCOPE_INFO.getVersion())
            .build();

    serverCallsStarted =
        meter
            .counterBuilder(METRIC_PREFIX + "server.call.started")
            .setDescription(
                "The total number of RPCs started, including those that have not completed.")
            .setUnit("{call}")
            .build();

    clientCredLatencies =
        meter
            .histogramBuilder(METRIC_PREFIX + "client.call.credential.duration")
            .setDescription("Latency of getting credentials")
            .setUnit("ms")
            .build();

    clientQueueLatencies =
        meter
            .histogramBuilder(METRIC_PREFIX + "client.call.queue.duration")
            .setDescription(
                "Duration of how long the outbound side of the proxy had the RPC queued")
            .setUnit("ms")
            .build();

    requestSizes =
        meter
            .histogramBuilder(METRIC_PREFIX + "client.call.sent_total_message_size")
            .setDescription(
                "Total bytes sent per call to Bigtable service (excluding metadata, grpc and"
                    + " transport framing bytes)")
            .setUnit("by")
            .ofLongs()
            .build();

    responseSizes =
        meter
            .histogramBuilder(METRIC_PREFIX + "client.call.rcvd_total_message_size")
            .setDescription(
                "Total bytes received per call from Bigtable service (excluding metadata, grpc and"
                    + " transport framing bytes)")
            .setUnit("by")
            .ofLongs()
            .build();

    gfeLatency =
        meter
            .histogramBuilder(METRIC_PREFIX + "client.gfe.duration")
            .setDescription(
                "Latency as measured by Google load balancer from the time it "
                    + "received the first byte of the request until it received the first byte of"
                    + " the response from the Cloud Bigtable service.")
            .setUnit("ms")
            .build();

    gfeResponseHeadersMissing =
        meter
            .counterBuilder(METRIC_PREFIX + "client.gfe.duration_missing.count")
            .setDescription("Count of calls missing gfe response headers")
            .setUnit("{call}")
            .build();

    clientCallLatencies =
        meter
            .histogramBuilder(METRIC_PREFIX + "client.call.duration")
            .setDescription("Total duration of how long the outbound call took")
            .setUnit("ms")
            .build();

    clientCallFirstByteLatencies =
        meter
            .histogramBuilder(METRIC_PREFIX + "client.first_byte.duration")
            .setDescription("Latency from start of request until first response is received")
            .setUnit("ms")
            .build();

    downstreamLatencies =
        meter
            .histogramBuilder(METRIC_PREFIX + "server.write_wait.duration")
            .setDescription(
                "Total amount of time spent waiting for the downstream client to be"
                    + " ready for data")
            .setUnit("ms")
            .build();

    channelCounter =
        meter
            .upDownCounterBuilder(METRIC_PREFIX + "client.channel.count")
            .setDescription("Number of open channels")
            .setUnit("{channel}")
            .build();

    outstandingRpcCountGauge =
        meter
            .gaugeBuilder(METRIC_PREFIX + "client.call.max_outstanding_count")
            .setDescription("Maximum number of concurrent RPCs in a single minute window")
            .setUnit("{call}")
            .ofLongs()
            .buildWithCallback(o -> o.record(maxSeen.getAndSet(0)));

    presenceGauge =
        meter
            .gaugeBuilder(METRIC_PRESENCE_NAME)
            .setDescription(METRIC_PRESENCE_DESC)
            .setUnit(METRIC_PRESENCE_UNIT)
            .ofLongs()
            .buildWithCallback(o -> o.record(1));

    channelStateChangeCounter =
        meter
            .counterBuilder(METRIC_PREFIX + "client.channel_change_count")
            .setDescription("Counter of channel state transitions")
            .setUnit("{change}")
            .build();
  }

  @Override
  public void close() throws IOException {
    outstandingRpcCountGauge.close();
    presenceGauge.close();

    if (meterProvider instanceof Closeable) {
      ((Closeable) meterProvider).close();
    }
  }

  @Override
  public MetricsAttributesImpl createAttributes(CallLabels callLabels) {
    AttributesBuilder attrs =
        Attributes.builder()
            .put(METHOD_KEY, callLabels.getMethodName())
            .put(API_CLIENT_KEY, callLabels.getApiClient().orElse("<missing>"));

    String resourceValue;
    try {
      resourceValue = callLabels.extractResourceName().orElse("<missing>");
    } catch (ParsingException e) {
      LOG.warn("Failed to extract resource from callLabels: {}", callLabels, e);
      resourceValue = "<error>";
    }
    attrs.put(MetricsImpl.RESOURCE_KEY, resourceValue);

    String appProfile;
    try {
      appProfile = callLabels.extractAppProfileId().orElse("<missing>");
    } catch (ParsingException e) {
      LOG.warn("Failed to extract app profile from callLabels: {}", callLabels, e);
      appProfile = "<error>";
    }
    attrs.put(MetricsImpl.APP_PROFILE_KEY, appProfile);

    return new AutoValue_MetricsImpl_MetricsAttributesImpl(attrs.build());
  }

  @Override
  public void recordCallStarted(MetricsAttributes attrs) {
    serverCallsStarted.add(1, unwrap(attrs));

    int outstanding = numOutstandingRpcs.incrementAndGet();
    maxSeen.updateAndGet(n -> Math.max(outstanding, n));
  }

  @Override
  public void recordCredLatency(MetricsAttributes attrs, Status status, Duration duration) {
    Attributes attributes =
        unwrap(attrs).toBuilder().put(STATUS_KEY, status.getCode().name()).build();
    clientCredLatencies.record(toMs(duration), attributes);
  }

  @Override
  public void recordQueueLatency(MetricsAttributes attrs, Duration duration) {
    clientQueueLatencies.record(toMs(duration), unwrap(attrs));
  }

  @Override
  public void recordRequestSize(MetricsAttributes attrs, long size) {
    requestSizes.record(size, unwrap(attrs));
  }

  @Override
  public void recordResponseSize(MetricsAttributes attrs, long size) {
    responseSizes.record(size, unwrap(attrs));
  }

  @Override
  public void recordGfeLatency(MetricsAttributes attrs, Duration duration) {
    gfeLatency.record(toMs(duration), unwrap(attrs));
  }

  @Override
  public void recordGfeHeaderMissing(MetricsAttributes attrs) {
    gfeResponseHeadersMissing.add(1, unwrap(attrs));
  }

  @Override
  public void recordCallLatency(MetricsAttributes attrs, Status status, Duration duration) {
    Attributes attributes =
        unwrap(attrs).toBuilder().put(STATUS_KEY, status.getCode().name()).build();

    clientCallLatencies.record(toMs(duration), attributes);
    numOutstandingRpcs.decrementAndGet();
  }

  @Override
  public void recordFirstByteLatency(MetricsAttributes attrs, Duration duration) {
    clientCallFirstByteLatencies.record(toMs(duration), unwrap(attrs));
  }

  @Override
  public void updateChannelCount(int delta) {
    channelCounter.add(delta);
  }

  @Override
  public void recordChannelStateChange(ConnectivityState prevState, ConnectivityState newState) {
    Attributes attributes =
        Attributes.builder()
            .put(
                PREV_CHANNEL_STATE, Optional.ofNullable(prevState).map(Enum::name).orElse("<null>"))
            .put(
                CURRENT_CHANNEL_STATE,
                Optional.ofNullable(newState).map(Enum::name).orElse("<null>"))
            .build();
    channelStateChangeCounter.add(1, attributes);
  }

  @Override
  public void recordDownstreamLatency(MetricsAttributes attrs, Duration latency) {
    downstreamLatencies.record(toMs(latency), unwrap(attrs));
  }

  private static double toMs(Duration duration) {
    return duration.toNanos() / 1_000_000.0;
  }

  private static Attributes unwrap(MetricsAttributes wrapped) {
    return ((MetricsAttributesImpl) wrapped).getAttributes();
  }

  /**
   * Generate a test data point to test permissions for exporting metrics. Used in {@link
   * com.google.cloud.bigtable.examples.proxy.commands.Verify}.
   */
  public static MetricData generateTestPresenceMeasurement(Resource resource) {
    Instant end = Instant.now().truncatedTo(ChronoUnit.MINUTES);
    Instant start = end.minus(Duration.ofMinutes(1));

    return ImmutableMetricData.createLongGauge(
        resource,
        INSTRUMENTATION_SCOPE_INFO,
        METRIC_PRESENCE_NAME,
        METRIC_PRESENCE_DESC,
        METRIC_PRESENCE_UNIT,
        ImmutableGaugeData.create(
            ImmutableList.of(
                ImmutableLongPointData.create(
                    TimeUnit.MILLISECONDS.toNanos(start.toEpochMilli()),
                    TimeUnit.MILLISECONDS.toNanos(end.toEpochMilli()),
                    Attributes.empty(),
                    1L))));
  }

  @VisibleForTesting
  @AutoValue
  abstract static class MetricsAttributesImpl implements MetricsAttributes {
    abstract Attributes getAttributes();
  }
}
