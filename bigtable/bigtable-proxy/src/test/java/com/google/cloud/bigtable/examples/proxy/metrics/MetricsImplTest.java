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

import static com.google.common.truth.Truth.assertThat;

import com.google.bigtable.v2.BigtableGrpc;
import com.google.cloud.bigtable.examples.proxy.core.CallLabels;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.MeterProvider;
import java.util.Optional;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@RunWith(JUnit4.class)
public class MetricsImplTest {
  @Rule public final MockitoRule mockitoTestRule = MockitoJUnit.rule();

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  MeterProvider mockMeterProvider;

  private MetricsImpl metrics;

  @Before
  public void setUp() throws Exception {
    metrics = new MetricsImpl(mockMeterProvider);
  }

  @Test
  public void testBasic() {
    CallLabels callLabels =
        CallLabels.create(
            BigtableGrpc.getMutateRowMethod(),
            Optional.of("some-client"),
            Optional.of("projects/p/instances/i/tables/t"),
            Optional.of("a"));
    Attributes attrs = metrics.createAttributes(callLabels).getAttributes();
    assertThat(attrs.asMap())
        .containsAtLeast(
            AttributeKey.stringKey("api_client"), "some-client",
            AttributeKey.stringKey("resource"), "projects/p/instances/i/tables/t",
            AttributeKey.stringKey("app_profile"), "a",
            AttributeKey.stringKey("method"), "google.bigtable.v2.Bigtable/MutateRow");
  }

  @Test
  public void testMissing() {
    CallLabels callLabels =
        CallLabels.create(
            BigtableGrpc.getMutateRowMethod(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty());
    Attributes attrs = metrics.createAttributes(callLabels).getAttributes();
    assertThat(attrs.asMap())
        .containsAtLeast(
            AttributeKey.stringKey("api_client"), "<missing>",
            AttributeKey.stringKey("resource"), "<missing>",
            AttributeKey.stringKey("app_profile"), "<missing>",
            AttributeKey.stringKey("method"), "google.bigtable.v2.Bigtable/MutateRow");
  }
}
