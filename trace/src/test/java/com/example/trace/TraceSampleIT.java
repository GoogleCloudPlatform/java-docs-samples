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

import com.google.common.base.Strings;

import io.opencensus.exporter.trace.stackdriver.StackdriverTraceExporter;

import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for stackdriver tracing sample.
 */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class TraceSampleIT {
  private static final String CLOUD_PROJECT_KEY = "GOOGLE_CLOUD_PROJECT";

  @BeforeClass
  public static void setup() {
    Assert.assertFalse(Strings.isNullOrEmpty(System.getenv(CLOUD_PROJECT_KEY)));
  }

  @After
  public void tearDown() {
    StackdriverTraceExporter.unregister();
  }

  @Test
  public void testCreateAndRegister() throws IOException {
    TraceSample.createAndRegister();
    TraceSample.doWork();
  }

  @Test
  public void testCreateAndRegisterFullSampled() throws IOException {
    TraceSample.createAndRegister();
    TraceSample.doWorkFullSampled();
  }

  @Test
  public void testCreateAndRegisterGoogleCloudPlatform() throws IOException {
    TraceSample.createAndRegisterGoogleCloudPlatform(System.getenv(CLOUD_PROJECT_KEY));
    TraceSample.doWork();
  }
}
