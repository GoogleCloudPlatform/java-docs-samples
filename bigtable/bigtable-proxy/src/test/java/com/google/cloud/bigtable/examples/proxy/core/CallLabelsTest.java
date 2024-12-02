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

import static com.google.common.truth.Truth.assertAbout;
import static com.google.common.truth.Truth.assertThat;

import com.google.bigtable.v2.BigtableGrpc;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import io.grpc.Metadata;
import io.grpc.Metadata.Key;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CallLabelsTest {
  private static final Key<String> REQUEST_PARAMS =
      Key.of("x-goog-request-params", Metadata.ASCII_STRING_MARSHALLER);
  private static final Key<String> API_CLIENT =
      Key.of("x-goog-api-client", Metadata.ASCII_STRING_MARSHALLER);

  @Test
  public void testAllBasic() {
    Metadata md = new Metadata();
    md.put(REQUEST_PARAMS, "table_name=projects/p/instances/i/tables/t&app_profile_id=a");
    md.put(API_CLIENT, "some-client");
    CallLabels callLabels = CallLabels.create(BigtableGrpc.getMutateRowMethod(), md);

    assertThat(callLabels.getApiClient()).isEqualTo(Optional.of("some-client"));
    assertThat(callLabels.getAppProfileId()).isEqualTo(Optional.of("a"));
    assertThat(callLabels.getResourceName())
        .isEqualTo(Optional.of("projects/p/instances/i/tables/t"));
  }

  @Test
  public void testResourceEscaped() {
    Metadata md = new Metadata();
    md.put(REQUEST_PARAMS, "table_name=projects/p/instances/i/tables/t".replace("/", "%2F"));
    CallLabels callLabels = CallLabels.create(BigtableGrpc.getMutateRowMethod(), md);

    assertThat(callLabels.getResourceName())
        .isEqualTo(Optional.of("projects/p/instances/i/tables/t"));
  }

  @Test
  public void testEmpty() {
    Metadata md = new Metadata();
    CallLabels callLabels = CallLabels.create(BigtableGrpc.getMutateRowMethod(), md);

    assertThat(callLabels.getResourceName()).isEqualTo(Optional.empty());
  }

  @Test
  public void testMalformed1() {
    Metadata md = new Metadata();
    md.put(REQUEST_PARAMS, "table_name=");
    CallLabels callLabels = CallLabels.create(BigtableGrpc.getMutateRowMethod(), md);

    assertThat(callLabels.getResourceName()).isEqualTo(Optional.empty());
  }

  @Test
  public void testMalformed2() {
    Metadata md = new Metadata();
    md.put(REQUEST_PARAMS, "&");
    CallLabels callLabels = CallLabels.create(BigtableGrpc.getMutateRowMethod(), md);

    assertThat(callLabels.getResourceName()).isEqualTo(Optional.empty());
  }

  @Test
  public void testMalformed3() {
    Metadata md = new Metadata();
    md.put(REQUEST_PARAMS, "table_name=&");
    CallLabels callLabels = CallLabels.create(BigtableGrpc.getMutateRowMethod(), md);

    assertThat(callLabels.getResourceName()).isEqualTo(Optional.empty());
  }

  private static class CallLabelsSubject extends Subject {
    private final CallLabels actual;

    public CallLabelsSubject(FailureMetadata metadata, @Nullable CallLabels actual) {
      super(metadata, actual);
      this.actual = actual;
    }

    public static Factory<CallLabelsSubject, CallLabels> callLabels() {
      return CallLabelsSubject::new;
    }

    public static CallLabelsSubject assertThat(CallLabels callLabels) {
      return assertAbout(callLabels()).that(callLabels);
    }

    public void hasMethodName(String method) {
      check("getMethodName()").that(actual.getMethodName()).isEqualTo(method);
    }

    public void hasResourceName(String resourceName) {
      check("hasResourceName()")
          .that(actual.getResourceName())
          .isEqualTo(Optional.of(resourceName));
    }
  }
}
