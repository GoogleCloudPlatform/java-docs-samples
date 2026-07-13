/*
 * Copyright 2026 Google LLC
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

package com.example.memorystore.redis;

// [START memorystore_redis_client_side_metrics]
import com.google.cloud.opentelemetry.metric.GoogleCloudMetricExporter;
import com.google.cloud.opentelemetry.trace.TraceExporter;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.time.Duration;
import java.util.function.Function;

/**
 * Sample application demonstrating client-side metrics and tracing for
 * Google Cloud Memorystore for Redis.
 */
public final class RedisTelemetryApp {
    /** Attribute key for Redis operation names. */
    private static final AttributeKey<String> ATTR_OPERATION =
            AttributeKey.stringKey("operation");

    /** Maximum number of Redis reconnection attempts. */
    private static final int MAX_RETRIES = 3;

    /** Maximum total connections for the Jedis pool. */
    private static final int POOL_MAX_TOTAL = 20;

    /** Interval in seconds for exporting metrics to Google Cloud. */
    private static final long METRIC_INTERVAL_SECONDS = 10L;

    /** Base multiplier for exponential backoff sleep (in milliseconds). */
    private static final long RETRY_BACKOFF_BASE_MS = 100L;

    /** Conversion factor from Nanoseconds to Milliseconds. */
    private static final double NANO_TO_MS = 1_000_000.0;

    /** Default Redis port. */
    private static final int DEFAULT_REDIS_PORT = 6379;

    /** OpenTelemetry Tracer instance for recording trace spans. */
    private static Tracer tracer;

    /** OpenTelemetry Histogram for Redis round-trip time. */
    private static DoubleHistogram rttHist;

    /** OpenTelemetry Histogram for pool blocking latency. */
    private static DoubleHistogram clientBlockHist;

    /** OpenTelemetry Histogram for application logic blocking latency. */
    private static DoubleHistogram appBlockHist;

    /** OpenTelemetry Counter for Redis reconnection retry events. */
    private static LongCounter retryCounter;

    /** OpenTelemetry Counter for Redis connectivity errors. */
    private static LongCounter connErrorCounter;

    /** Shared Jedis connection pool. */
    private static JedisPool jedisPool;

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private RedisTelemetryApp() {
    }

    /**
     * Main entry point for running the sample application.
     *
     * @param args Command line arguments (not used).
     */
    public static void main(final String[] args) {
        setupTelemetry();

        final String host = System.getenv()
                .getOrDefault("REDISHOST", "localhost");
        final int port = Integer.parseInt(System.getenv()
                .getOrDefault("REDISPORT",
                        String.valueOf(DEFAULT_REDIS_PORT)));

        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(POOL_MAX_TOTAL);
        poolConfig.setBlockWhenExhausted(true);
        jedisPool = new JedisPool(poolConfig, host, port);

        try {
            run();
        } finally {
            if (jedisPool != null) {
                jedisPool.close();
            }
        }
    }

    /**
     * Executes the core business logic of reading and writing to Redis.
     *
     * @return The string retrieved from the Redis 'get' operation.
     */
    static String run() {
        final Span span = tracer.spanBuilder("process_user_span")
                .startSpan();
        try {
            smartRedisCall("set_user", jedis ->
                    jedis.set("user:123", "active"));

            final String result = smartRedisCall("get_user", jedis ->
                    jedis.get("user:123"));
            System.out.println("Retrieved: " + result);
            return result;
        } catch (Exception e) {
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    /**
     * Injects mocked or no-op telemetry and pool instances for unit testing.
     *
     * @param pool                The mocked or test JedisPool instance.
     * @param testOpenTelemetry The OpenTelemetry instance to use for testing.
     */
    static void initForTest(
            final JedisPool pool,
            final OpenTelemetry testOpenTelemetry) {
        jedisPool = pool;
        tracer = testOpenTelemetry.getTracer("jedis.client");
        final Meter meter = testOpenTelemetry.getMeter("jedis.metrics");

        rttHist = meter.histogramBuilder("redis_client_rtt")
                .setUnit("ms").build();
        clientBlockHist = meter
                .histogramBuilder("redis_client_blocking_latency")
                .setUnit("ms").build();
        appBlockHist = meter
                .histogramBuilder("redis_application_blocking_latency")
                .setUnit("ms").build();
        retryCounter = meter.counterBuilder("redis_retry_count").build();
        connErrorCounter = meter
                .counterBuilder("redis_connectivity_error_count")
                .build();

        retryCounter.add(0, Attributes.of(ATTR_OPERATION, "startup"));
        connErrorCounter.add(0, Attributes.of(ATTR_OPERATION, "startup"));
    }

    /**
     * Configures the production OpenTelemetry SDK to export Traces and Metrics
     * to Google Cloud Operations.
     */
    private static void setupTelemetry() {
        final SpanExporter traceExporter =
                TraceExporter.createWithDefaultConfiguration();
        final SdkTracerProvider tracerProvider =
                SdkTracerProvider.builder()
                        .addSpanProcessor(
                                BatchSpanProcessor.builder(traceExporter)
                                        .build())
                        .build();

        final MetricExporter metricExporter =
                GoogleCloudMetricExporter.createWithDefaultConfiguration();
        final SdkMeterProvider meterProvider =
                SdkMeterProvider.builder()
                        .registerMetricReader(
                                PeriodicMetricReader.builder(metricExporter)
                                        .setInterval(Duration.ofSeconds(
                                                METRIC_INTERVAL_SECONDS))
                                        .build())
                        .build();

        final OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .setMeterProvider(meterProvider)
                .buildAndRegisterGlobal();

        tracer = openTelemetry.getTracer("jedis.client");
        final Meter meter = openTelemetry.getMeter("jedis.metrics");

        rttHist = meter.histogramBuilder("redis_client_rtt")
                .setUnit("ms").build();
        clientBlockHist = meter
                .histogramBuilder("redis_client_blocking_latency")
                .setUnit("ms").build();
        appBlockHist = meter
                .histogramBuilder("redis_application_blocking_latency")
                .setUnit("ms").build();
        retryCounter = meter.counterBuilder("redis_retry_count").build();
        connErrorCounter = meter
                .counterBuilder("redis_connectivity_error_count")
                .build();

        retryCounter.add(0, Attributes.of(ATTR_OPERATION, "startup"));
        connErrorCounter.add(0, Attributes.of(ATTR_OPERATION, "startup"));
    }

    /**
     * Wraps a Redis operation with latency metrics, reconnection retry logic,
     * and trace spans.
     *
     * @param <T>           The return type of the Redis operation.
     * @param operationName The name of the operation for metric attributes.
     * @param operation     The Redis command lambda to execute safely.
     * @return The return value from the Redis command.
     */
    private static <T> T smartRedisCall(
            final String operationName,
            final Function<Jedis, T> operation) {
        int attempt = 0;
        final Attributes attrs = Attributes.of(ATTR_OPERATION,
                operationName);

        final Span span = tracer.spanBuilder(operationName).startSpan();

        try {
            while (attempt < MAX_RETRIES) {
                final long poolStart = System.nanoTime();
                try (Jedis jedis = jedisPool.getResource()) {
                    clientBlockHist.record((System.nanoTime() - poolStart)
                            / NANO_TO_MS, attrs);

                    final long reqStart = System.nanoTime();
                    final T response = operation.apply(jedis);
                    rttHist.record((System.nanoTime() - reqStart)
                            / NANO_TO_MS, attrs);

                    final long appStart = System.nanoTime();
                    @SuppressWarnings("unused")
                    final String dummy = String.valueOf(response);
                    appBlockHist.record((System.nanoTime() - appStart)
                            / NANO_TO_MS, attrs);

                    return response;
                } catch (JedisConnectionException e) {
                    attempt++;
                    connErrorCounter.add(1, attrs);
                    retryCounter.add(1, attrs);
                    span.recordException(e);
                    if (attempt >= MAX_RETRIES) {
                        throw e;
                    }
                    try {
                        Thread.sleep((long) (Math.pow(2, attempt)
                                * RETRY_BACKOFF_BASE_MS));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            return null;
        } finally {
            span.end();
        }
    }
}
// [END memorystore_redis_client_side_metrics]
