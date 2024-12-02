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
import com.google.bigtable.v2.PingAndWarmRequest;
import com.google.bigtable.v2.PingAndWarmRequest.Builder;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import io.grpc.Metadata;
import io.grpc.Metadata.Key;
import io.grpc.MethodDescriptor;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  private static final Logger LOG = LoggerFactory.getLogger(CallLabels.class);

  // All RLS headers
  static final Key<String> REQUEST_PARAMS =
      Key.of("x-goog-request-params", Metadata.ASCII_STRING_MARSHALLER);
  static final Key<String> LEGACY_RESOURCE_PREFIX =
      Key.of("google-cloud-resource-prefix", Metadata.ASCII_STRING_MARSHALLER);
  static final Key<String> ROUTING_COOKIE =
      Key.of("x-goog-cbt-cookie-routing", Metadata.ASCII_STRING_MARSHALLER);
  static final Key<String> FEATURE_FLAGS =
      Key.of("bigtable-features", Metadata.ASCII_STRING_MARSHALLER);
  static final Key<String> API_CLIENT =
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

  public abstract String getMethodName();

  abstract Optional<String> getRequestParams();

  abstract Optional<String> getLegacyResourcePrefix();

  abstract Optional<String> getRoutingCookie();

  abstract Optional<String> getEncodedFeatures();

  public abstract Optional<String> getApiClient();

  public static CallLabels create(MethodDescriptor<?, ?> method, Metadata headers) {
    Optional<String> apiClient = Optional.ofNullable(headers.get(API_CLIENT));

    Optional<String> requestParams = Optional.ofNullable(headers.get(REQUEST_PARAMS));
    Optional<String> legacyResourcePrefix =
        Optional.ofNullable(headers.get(LEGACY_RESOURCE_PREFIX));
    Optional<String> routingCookie = Optional.ofNullable(headers.get(ROUTING_COOKIE));
    Optional<String> encodedFeatures = Optional.ofNullable(headers.get(FEATURE_FLAGS));

    return create(
        method, requestParams, legacyResourcePrefix, routingCookie, encodedFeatures, apiClient);
  }

  @VisibleForTesting
  public static CallLabels create(
      MethodDescriptor<?, ?> method,
      Optional<String> requestParams,
      Optional<String> legacyResourcePrefix,
      Optional<String> routingCookie,
      Optional<String> encodedFeatures,
      Optional<String> apiClient) {

    return new AutoValue_CallLabels(
        method.getFullMethodName(),
        requestParams,
        legacyResourcePrefix,
        routingCookie,
        encodedFeatures,
        apiClient);
  }

  public Optional<String> extractResourceName() throws ParsingException {
    if (getRequestParams().isEmpty()) {
      return getLegacyResourcePrefix();
    }

    String requestParams = getRequestParams().orElse("");
    String[] encodedKvPairs = requestParams.split("&");
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
    return resourceName.map(ResourceName::getValue);
  }

  private static Optional<ResourceNameType> findType(String encodedKey) throws ParsingException {
    String decodedKey = percentDecode(encodedKey);

    for (ResourceNameType type : ResourceNameType.values()) {
      if (type.name.equals(decodedKey)) {
        return Optional.of(type);
      }
    }
    return Optional.empty();
  }

  public Optional<String> extractAppProfileId() throws ParsingException {
    String requestParams = getRequestParams().orElse("");

    for (String encodedPair : requestParams.split("&")) {
      if (!encodedPair.startsWith("app_profile_id=")) {
        continue;
      }
      String[] parts = encodedPair.split("=", 2);
      String encodedValue = parts.length > 1 ? parts[1] : "";
      return Optional.of(percentDecode(encodedValue));
    }
    return Optional.empty();
  }

  private static String percentDecode(String s) throws ParsingException {
    try {
      return URLDecoder.decode(s, StandardCharsets.UTF_8);
    } catch (RuntimeException e) {
      throw new ParsingException("Failed to url decode " + s, e);
    }
  }

  @AutoValue
  public abstract static class PrimingKey {
    abstract Map<String, String> getMetadata();

    abstract String getName();

    abstract Optional<String> getAppProfileId();

    public static Optional<PrimingKey> from(CallLabels labels) throws ParsingException {
      final ImmutableMap.Builder<String, String> md = ImmutableMap.builder();

      Optional<String> resourceName = labels.extractResourceName();
      if (resourceName.isEmpty()) {
        return Optional.empty();
      }
      String[] resourceNameParts = resourceName.get().split("/", 5);
      if (resourceNameParts.length < 4
          || !resourceNameParts[0].equals("projects")
          || !resourceNameParts[2].equals("instances")) {
        return Optional.empty();
      }
      String instanceName =
          "projects/" + resourceNameParts[1] + "/instances/" + resourceNameParts[3];
      StringBuilder reqParams =
          new StringBuilder()
              .append("name=")
              .append(URLEncoder.encode(instanceName, StandardCharsets.UTF_8));

      Optional<String> appProfileId = labels.extractAppProfileId();
      appProfileId.ifPresent(val -> reqParams.append("&app_profile_id=").append(val));
      md.put(REQUEST_PARAMS.name(), reqParams.toString());

      labels
          .getLegacyResourcePrefix()
          .ifPresent(ignored -> md.put(LEGACY_RESOURCE_PREFIX.name(), instanceName));

      labels.getRoutingCookie().ifPresent(c -> md.put(ROUTING_COOKIE.name(), c));

      labels.getEncodedFeatures().ifPresent(c -> md.put(FEATURE_FLAGS.name(), c));

      labels.getApiClient().ifPresent(c -> md.put(API_CLIENT.name(), c));

      return Optional.of(
          new AutoValue_CallLabels_PrimingKey(md.build(), instanceName, appProfileId));
    }

    public Metadata composeMetadata() {
      Metadata md = new Metadata();
      for (Entry<String, String> e : getMetadata().entrySet()) {
        md.put(Key.of(e.getKey(), Metadata.ASCII_STRING_MARSHALLER), e.getValue());
      }
      return md;
    }

    public PingAndWarmRequest composeProto() {
      Builder builder = PingAndWarmRequest.newBuilder().setName(getName());
      getAppProfileId().ifPresent(builder::setAppProfileId);
      return builder.build();
    }
  }

  public static class ParsingException extends Exception {

    public ParsingException(String message) {
      super(message);
    }

    public ParsingException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
