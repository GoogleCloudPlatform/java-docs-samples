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

import com.google.auto.value.AutoValue;
import io.grpc.Metadata;
import io.grpc.Metadata.Key;
import io.grpc.MethodDescriptor;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * A value class to encapsulate call identity.
 *
 * <p>This call extracts relevant information from request headers and makes it accessible to
 * metrics & the upstream client. The primary headers consulted are:
 *
 * <ul>
 *   <li>{@code x-goog-request-params} - contains the resource and app profile id
 *   <li>{@code x-goog-api-client} - contains the client info of the downstream client
 * </ul>
 */
@AutoValue
public abstract class CallLabels {
  public static final Key<String> REQUEST_PARAMS =
      Key.of("x-goog-request-params", Metadata.ASCII_STRING_MARSHALLER);
  private static final Key<String> API_CLIENT =
      Key.of("x-goog-api-client", Metadata.ASCII_STRING_MARSHALLER);

  enum ResourceNameType {
    Parent("parent", 0),
    Name("name", 1),
    TableName("table_name", 2);

    private final String name;
    private final int priority;

    ResourceNameType(String name, int priority) {
      this.name = name;
      this.priority = priority;
    }
  }

  @AutoValue
  abstract static class ResourceName {

    abstract ResourceNameType getType();

    abstract String getValue();

    static ResourceName create(ResourceNameType type, String value) {
      return new AutoValue_CallLabels_ResourceName(type, value);
    }
  }

  public abstract Optional<String> getApiClient();

  public abstract Optional<String> getResourceName();

  public abstract Optional<String> getAppProfileId();

  public abstract String getMethodName();

  public static CallLabels create(MethodDescriptor<?, ?> method, Metadata headers) {
    Optional<String> apiClient = Optional.ofNullable(headers.get(API_CLIENT));

    String requestParams = Optional.ofNullable(headers.get(REQUEST_PARAMS)).orElse("");
    String[] encodedKvPairs = requestParams.split("&");
    Optional<String> resourceName = extractResourceName(encodedKvPairs).map(ResourceName::getValue);
    Optional<String> appProfile = extractAppProfileId(encodedKvPairs);

    return create(method, apiClient, resourceName, appProfile);
  }

  public static CallLabels create(
      MethodDescriptor<?, ?> method,
      Optional<String> apiClient,
      Optional<String> resourceName,
      Optional<String> appProfile) {

    return new AutoValue_CallLabels(
        apiClient, resourceName, appProfile, method.getFullMethodName());
  }

  private static Optional<ResourceName> extractResourceName(String[] encodedKvPairs) {
    Optional<ResourceName> resourceName = Optional.empty();

    for (String encodedKv : encodedKvPairs) {
      String[] split = encodedKv.split("=", 2);
      if (split.length != 2) {
        continue;
      }
      String encodedKey = split[0];
      String encodedValue = split[1];
      if (encodedKey.isEmpty() || encodedValue.isEmpty()) {
        continue;
      }

      Optional<ResourceNameType> newType = findType(encodedKey);

      if (newType.isEmpty()) {
        continue;
      }
      // Skip if we previously found a resource name and the new resource name type has a lower
      // priority
      if (resourceName.isPresent()
          && newType.get().priority <= resourceName.get().getType().priority) {
        continue;
      }
      String decodedValue = percentDecode(encodedValue);

      resourceName = Optional.of(ResourceName.create(newType.get(), decodedValue));
    }
    return resourceName;
  }

  private static Optional<ResourceNameType> findType(String encodedKey) {
    String decodedKey = percentDecode(encodedKey);

    for (ResourceNameType type : ResourceNameType.values()) {
      if (type.name.equals(decodedKey)) {
        return Optional.of(type);
      }
    }
    return Optional.empty();
  }

  private static Optional<String> extractAppProfileId(String[] encodedKvPairs) {
    for (String encodedPair : encodedKvPairs) {
      if (!encodedPair.startsWith("app_profile_id=")) {
        continue;
      }
      String[] parts = encodedPair.split("=", 2);
      String encodedValue = parts.length > 1 ? parts[1] : "";
      return Optional.of(percentDecode(encodedValue));
    }
    return Optional.empty();
  }

  private static String percentDecode(String s) {
    return URLDecoder.decode(s, StandardCharsets.UTF_8);
  }
}
