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

package com.google.cloud.bigtable.examples.proxy.core;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import com.google.bigtable.v2.BigtableGrpc;
import com.google.bigtable.v2.PingAndWarmRequest;
import com.google.cloud.bigtable.examples.proxy.core.CallLabels.ParsingException;
import com.google.cloud.bigtable.examples.proxy.core.CallLabels.PrimingKey;
import io.grpc.Metadata;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CallLabelsTest {
  @Test
  public void testAllBasic() throws ParsingException {
    Metadata md = new Metadata();
    md.put(
        CallLabels.REQUEST_PARAMS,
        "table_name=projects/p/instances/i/tables/t&app_profile_id=a".replaceAll("/", "%2F"));
    md.put(CallLabels.LEGACY_RESOURCE_PREFIX, "projects/p/instances/i/tables/t");
    md.put(CallLabels.ROUTING_COOKIE, "some-opaque-string");
    md.put(CallLabels.FEATURE_FLAGS, "some-serialized-features-string");
    md.put(CallLabels.API_CLIENT, "some-client");
    CallLabels callLabels = CallLabels.create(BigtableGrpc.getMutateRowMethod(), md);

    assertThat(callLabels.getRequestParams())
        .isEqualTo(
            Optional.of("table_name=projects%2Fp%2Finstances%2Fi%2Ftables%2Ft&app_profile_id=a"));
    assertThat(callLabels.getLegacyResourcePrefix())
        .isEqualTo(Optional.of("projects/p/instances/i/tables/t"));
    assertThat(callLabels.getRoutingCookie()).isEqualTo(Optional.of("some-opaque-string"));
    assertThat(callLabels.getEncodedFeatures())
        .isEqualTo(Optional.of("some-serialized-features-string"));
    assertThat(callLabels.getApiClient()).isEqualTo(Optional.of("some-client"));

    assertThat(callLabels.extractAppProfileId()).isEqualTo(Optional.of("a"));
    assertThat(callLabels.extractResourceName())
        .isEqualTo(Optional.of("projects/p/instances/i/tables/t"));
  }

  @Test
  public void testResourceEscaped() throws ParsingException {
    Metadata md = new Metadata();
    md.put(
        CallLabels.REQUEST_PARAMS,
        "table_name=projects/p/instances/i/tables/t".replace("/", "%2F"));
    CallLabels callLabels = CallLabels.create(BigtableGrpc.getMutateRowMethod(), md);

    assertThat(callLabels.extractResourceName())
        .isEqualTo(Optional.of("projects/p/instances/i/tables/t"));
  }

  @Test
  public void testEmpty() throws ParsingException {
    Metadata md = new Metadata();
    CallLabels callLabels = CallLabels.create(BigtableGrpc.getMutateRowMethod(), md);

    assertThat(callLabels.extractResourceName()).isEqualTo(Optional.empty());
    assertThat(callLabels.extractAppProfileId()).isEqualTo(Optional.empty());
  }

  @Test
  public void testLegacyFallback() throws ParsingException {
    Metadata md = new Metadata();
    md.put(CallLabels.LEGACY_RESOURCE_PREFIX, "projects/p/instances/i/tables/t");
    CallLabels callLabels = CallLabels.create(BigtableGrpc.getMutateRowMethod(), md);

    assertThat(callLabels.extractResourceName())
        .isEqualTo(Optional.of("projects/p/instances/i/tables/t"));
  }

  @Test
  public void testMalformed1() throws ParsingException {
    Metadata md = new Metadata();
    md.put(CallLabels.REQUEST_PARAMS, "table_name=");
    CallLabels callLabels = CallLabels.create(BigtableGrpc.getMutateRowMethod(), md);

    assertThat(callLabels.extractResourceName()).isEqualTo(Optional.empty());
  }

  @Test
  public void testMalformed2() throws ParsingException {
    Metadata md = new Metadata();
    md.put(CallLabels.REQUEST_PARAMS, "&");
    CallLabels callLabels = CallLabels.create(BigtableGrpc.getMutateRowMethod(), md);

    assertThat(callLabels.extractResourceName()).isEqualTo(Optional.empty());
  }

  @Test
  public void testMalformed3() throws ParsingException {
    Metadata md = new Metadata();
    md.put(CallLabels.REQUEST_PARAMS, "table_name=&");
    CallLabels callLabels = CallLabels.create(BigtableGrpc.getMutateRowMethod(), md);

    assertThat(callLabels.extractResourceName()).isEqualTo(Optional.empty());
  }

  @Test
  public void testMalformed4() throws ParsingException {
    Metadata md = new Metadata();
    md.put(CallLabels.REQUEST_PARAMS, "table_name=%s");
    CallLabels callLabels = CallLabels.create(BigtableGrpc.getMutateRowMethod(), md);

    assertThrows(ParsingException.class, callLabels::extractResourceName);
  }

  @Test
  public void testPrimingKey() throws ParsingException {
    final String tableName = "projects/myp/instances/myi/tables/myt";
    final String encodedTableName = "projects%2Fmyp%2Finstances%2Fmyi%2Ftables%2Fmyt";
    final String instanceName = "projects/myp/instances/myi";
    final String encodedInstanceName = "projects%2Fmyp%2Finstances%2Fmyi";
    final String appProfileId = "mya";

    CallLabels callLabels =
        CallLabels.create(
            BigtableGrpc.getMutateRowMethod(),
            Optional.of(
                String.format("table_name=%s&app_profile_id=%s", encodedTableName, appProfileId)),
            Optional.of(tableName),
            Optional.of("opaque-cookie"),
            Optional.of("encoded-features"),
            Optional.of("some-client"));
    PrimingKey key = PrimingKey.from(callLabels).get();

    assertThat(key.getAppProfileId()).isEqualTo(Optional.of("mya"));
    assertThat(key.getName()).isEqualTo(instanceName);

    Metadata m = new Metadata();

    m.put(
        CallLabels.REQUEST_PARAMS,
        String.format("name=%s&app_profile_id=%s", encodedInstanceName, appProfileId));
    m.put(CallLabels.LEGACY_RESOURCE_PREFIX, instanceName);
    m.put(CallLabels.ROUTING_COOKIE, "opaque-cookie");
    m.put(CallLabels.FEATURE_FLAGS, "encoded-features");
    m.put(CallLabels.API_CLIENT, "some-client");

    assertThat(key.composeMetadata().toString()).isEqualTo(m.toString());

    assertThat(key.composeProto())
        .isEqualTo(
            PingAndWarmRequest.newBuilder()
                .setName(instanceName)
                .setAppProfileId(appProfileId)
                .build());
  }
}
