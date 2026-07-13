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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.OpenTelemetry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

/** Unit tests for {@link RedisTelemetryApp} verifying Redis interaction and Retry logic. */
@RunWith(JUnit4.class)
public class RedisTelemetryAppTest {

    private JedisPool mockJedisPool;
    private Jedis mockJedis;

    @Before
    public void setUp() {
        mockJedisPool = mock(JedisPool.class);
        mockJedis = mock(Jedis.class);

        // Inject the mocked pool and a NO-OP OpenTelemetry instance so tests
        // run hermetically without contacting real GCP Trace/Metric APIs.
        RedisTelemetryApp.initForTest(mockJedisPool, OpenTelemetry.noop());

        // Configure the mocked pool to hand out our mocked Jedis connection
        when(mockJedisPool.getResource()).thenReturn(mockJedis);

        // Configure expected mocked Redis responses
        when(mockJedis.set(eq("user:123"), eq("active"))).thenReturn("OK");
        when(mockJedis.get(eq("user:123"))).thenReturn("active");
    }

    @Test
    public void testRun_successfulExecution() {
        // Execute the extracted sample logic
        String result = RedisTelemetryApp.run();

        // Verify the application correctly read the final state
        assertNotNull("Result should not be null", result);
        assertEquals("active", result);

        // Verify the Redis pool handed out a connection, and the sample called set & get
        verify(mockJedisPool, atLeastOnce()).getResource();
        verify(mockJedis, times(1)).set("user:123", "active");
        verify(mockJedis, times(1)).get("user:123");
        verify(mockJedis, atLeastOnce()).close(); // try-with-resources closes connection
    }

    @Test
    public void testRun_retryMechanismOnConnectivityFailure() {
        // Configure the mocked pool to throw a Connection Exception on the FIRST call,
        // and return the valid mock connection on the SECOND call.
        when(mockJedisPool.getResource())
                .thenThrow(new JedisConnectionException("Temporary network blip"))
                .thenReturn(mockJedis);

        String result = RedisTelemetryApp.run();

        // Verify it still succeeds due to the retry handler in smartRedisCall
        assertEquals("active", result);

        // Verify getResource was called 3 times (1 failed + 1 successful for 'set', and 1 successful for 'get')
        verify(mockJedisPool, times(3)).getResource();
        verify(mockJedis, times(1)).set("user:123", "active");
        verify(mockJedis, times(1)).get("user:123");
    }

    @Test
    public void testRun_failureAfterMaxRetries() {
        // Configure the mocked pool to fail CONSTANTLY
        when(mockJedisPool.getResource())
                .thenThrow(new JedisConnectionException("Redis is down permanently"));

        // Verify it stops retrying and propagates the exception upward to alert CI
        assertThrows(JedisConnectionException.class, RedisTelemetryApp::run);

        // Verify it attempted exactly 3 retries before quitting
        verify(mockJedisPool, times(3)).getResource();
    }
}