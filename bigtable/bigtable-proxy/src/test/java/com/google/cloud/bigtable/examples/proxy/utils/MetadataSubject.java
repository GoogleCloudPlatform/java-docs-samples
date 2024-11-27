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

package com.google.cloud.bigtable.examples.proxy.utils;

import static com.google.common.truth.Truth.assertAbout;

import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import io.grpc.Metadata;
import java.util.ArrayList;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

public class MetadataSubject extends Subject {
  private final Metadata metadata;

  public MetadataSubject(FailureMetadata metadata, @Nullable Metadata actual) {
    super(metadata, actual);
    this.metadata = actual;
  }

  public static Factory<MetadataSubject, Metadata> metadata() {
    return MetadataSubject::new;
  }

  public static MetadataSubject assertThat(Metadata metadata) {
    return assertAbout(metadata()).that(metadata);
  }

  public void hasKey(String key) {
    hasKey(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER));
  }

  public void hasKey(Metadata.Key<?> key) {
    check("keys()").that(metadata.keys()).contains(key);
  }

  public void hasValue(String key, String value) {
    hasValue(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER), value);
  }

  public <T> void hasValue(Metadata.Key<T> key, T value) {
    Iterable<T> actualValues = Optional.ofNullable(metadata.getAll(key)).orElse(new ArrayList<>());
    check("get(" + key + ")").that(actualValues).containsExactly(value);
  }

  public void containsValue(String key, String value) {
    check("get(" + key + ")")
        .that(metadata.getAll(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER)))
        .contains(value);
  }

  public <T> void containsValue(Metadata.Key<T> key, T value) {
    check("get(" + key + ")").that(metadata.getAll(key)).contains(value);
  }
}
