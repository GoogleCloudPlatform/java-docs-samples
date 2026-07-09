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
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.trace.export.SpanExporter;
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
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.time.Duration;
import java.util.function.Function;

public class RedisTelemetryApp {
    private static final AttributeKey<String> ATTR_OPERATION = AttributeKey.stringKey("operation");
    static Tracer tracer;
    static DoubleHistogram rttHist, clientBlockHist, appBlockHist;
    static LongCounter retryCounter, connErrorCounter;
    static JedisPool jedisPool;

    public static void main(String[] args) {
        setupTelemetry();

        String host = System.getenv().getOrDefault("REDISHOST", "localhost");
        int port = Integer.parseInt(System.getenv().getOrDefault("REDISPORT", "6379"));

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(20);
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
     * Extracts the core business logic so it can be invoked safely in unit tests
     * with mocked Redis pools and openTelemetry instances.
     */
    static String run() {
        Span span = tracer.spanBuilder("process_user_span").startSpan();
        try {
            // Simple write and read operations
            smartRedisCall("set_user", jedis -> jedis.set("user:123", "active"));

            String result = smartRedisCall("get_user", jedis -> jedis.get("user:123"));
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
     * Initializer hook used exclusively by the test suite to safely inject
     * mocked/noop OpenTelemetry and JedisPool instances into the static context.
     */
    static void initForTest(JedisPool pool, OpenTelemetry testOpenTelemetry) {
        jedisPool = pool;
        tracer = testOpenTelemetry.getTracer("jedis.client");
        Meter meter = testOpenTelemetry.getMeter("jedis.metrics");

        rttHist = meter.histogramBuilder("redis_client_rtt").setUnit("ms").build();
        clientBlockHist = meter.histogramBuilder("redis_client_blocking_latency").setUnit("ms").build();
        appBlockHist = meter.histogramBuilder("redis_application_blocking_latency").setUnit("ms").build();
        retryCounter = meter.counterBuilder("redis_retry_count").build();
        connErrorCounter = meter.counterBuilder("redis_connectivity_error_count").build();

        retryCounter.add(0, Attributes.of(ATTR_OPERATION, "startup"));
        connErrorCounter.add(0, Attributes.of(ATTR_OPERATION, "startup"));
    }

    private static void setupTelemetry() {
        SpanExporter traceExporter = TraceExporter.createWithDefaultConfiguration();
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor.builder(traceExporter).build()).build();

        MetricExporter metricExporter = GoogleCloudMetricExporter.createWithDefaultConfiguration();
        SdkMeterProvider meterProvider = SdkMeterProvider.builder()
                .registerMetricReader(PeriodicMetricReader.builder(metricExporter).setInterval(Duration.ofSeconds(10)).build()).build();

        OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider).setMeterProvider(meterProvider).buildAndRegisterGlobal();

        tracer = openTelemetry.getTracer("jedis.client");
        Meter meter = openTelemetry.getMeter("jedis.metrics");

        rttHist = meter.histogramBuilder("redis_client_rtt").setUnit("ms").build();
        clientBlockHist = meter.histogramBuilder("redis_client_blocking_latency").setUnit("ms").build();
        appBlockHist = meter.histogramBuilder("redis_application_blocking_latency").setUnit("ms").build();
        retryCounter = meter.counterBuilder("redis_retry_count").build();
        connErrorCounter = meter.counterBuilder("redis_connectivity_error_count").build();

        retryCounter.add(0, Attributes.of(ATTR_OPERATION, "startup"));
        connErrorCounter.add(0, Attributes.of(ATTR_OPERATION, "startup"));
    }

    private static <T> T smartRedisCall(String operationName, Function<Jedis, T> operation) {
        int maxRetries = 3;
        int attempt = 0;
        Attributes attrs = Attributes.of(ATTR_OPERATION, operationName);

        // Create a dedicated child span for the Redis command
        Span span = tracer.spanBuilder(operationName).startSpan();

        try {
            while (attempt < maxRetries) {
                long poolStart = System.nanoTime();
                try (Jedis jedis = jedisPool.getResource()) {
                    clientBlockHist.record((System.nanoTime() - poolStart) / 1_000_000.0, attrs);

                    long reqStart = System.nanoTime();
                    T response = operation.apply(jedis);
                    rttHist.record((System.nanoTime() - reqStart) / 1_000_000.0, attrs);

                    long appStart = System.nanoTime();
                    @SuppressWarnings("unused")
                    String dummy = String.valueOf(response);
                    appBlockHist.record((System.nanoTime() - appStart) / 1_000_000.0, attrs);

                    return response;
                } catch (JedisConnectionException e) {
                    attempt++;
                    connErrorCounter.add(1, attrs);
                    retryCounter.add(1, attrs);
                    span.recordException(e); // Attach error to trace
                    if (attempt >= maxRetries) {
                        throw e;
                    }
                    try {
                        Thread.sleep((long) (Math.pow(2, attempt) * 100));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            return null;
        } finally {
            span.end(); // Ensure span always closes
        }
    }
}
// [END memorystore_redis_client_side_metrics]