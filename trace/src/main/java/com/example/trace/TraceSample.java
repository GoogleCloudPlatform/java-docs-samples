/*
 * Copyright 2018 Google LLC
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

package com.example.trace;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;

import io.opencensus.common.Scope;
import io.opencensus.exporter.trace.stackdriver.StackdriverTraceConfiguration;
import io.opencensus.exporter.trace.stackdriver.StackdriverTraceExporter;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.samplers.Samplers;

import java.io.IOException;
import java.util.Date;

import org.joda.time.DateTime;

public class TraceSample {

  // [START trace_setup_java_custom_span]
  private static final Tracer tracer = Tracing.getTracer();

  public static void doWork() {
    // Create a child Span of the current Span.
    try (Scope ss = tracer.spanBuilder("MyChildWorkSpan").startScopedSpan()) {
      doInitialWork();
      tracer.getCurrentSpan().addAnnotation("Finished initial work");
      doFinalWork();
    }
  }

  private static void doInitialWork() {
    // ...
    tracer.getCurrentSpan().addAnnotation("Doing initial work");
    // ...
  }

  private static void doFinalWork() {
    // ...
    tracer.getCurrentSpan().addAnnotation("Hello world!");
    // ...
  }
  // [END trace_setup_java_custom_span]

  // [START trace_setup_java_full_sampling]
  public static void doWorkFullSampled() {
    try (
        Scope ss = tracer.spanBuilder("MyChildWorkSpan")
                     .setSampler(Samplers.alwaysSample())
                     .startScopedSpan()) {
      doInitialWork();
      tracer.getCurrentSpan().addAnnotation("Finished initial work");
      doFinalWork();
    }
  }
  // [END trace_setup_java_full_sampling]

  // [START trace_setup_java_create_and_register]
  public static void createAndRegister() throws IOException {
    StackdriverTraceExporter.createAndRegister(
        StackdriverTraceConfiguration.builder().build());
  }
  // [END trace_setup_java_create_and_register]

  // [START trace_setup_java_create_and_register_with_token]
  public static void createAndRegisterWithToken(String accessToken) throws IOException {
    Date expirationTime = DateTime.now().plusSeconds(60).toDate();

    GoogleCredentials credentials =
        GoogleCredentials.create(new AccessToken(accessToken, expirationTime));
    StackdriverTraceExporter.createAndRegister(
        StackdriverTraceConfiguration.builder()
            .setProjectId("MyStackdriverProjectId")
            .setCredentials(credentials)
            .build());
  }
  // [END trace_setup_java_create_and_register_with_token]

  // [START trace_setup_java_register_exporter]
  public static void createAndRegisterGoogleCloudPlatform(String projectId) throws IOException {
    StackdriverTraceExporter.createAndRegister(
        StackdriverTraceConfiguration.builder()
            .setProjectId(projectId)
            .build());
  }
  // [END trace_setup_java_register_exporter]
}
