/*
 * Copyright 2016 Google Inc. All Rights Reserved.
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

package com.google.cloud.vision.samples.text;

import static com.google.common.truth.Truth.assertThat;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.json.Json;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.api.services.vision.v1.Vision;
import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Unit tests for {@link TextApp}.
 */
@RunWith(JUnit4.class)
public class TextAppTest {
  private TextApp appUnderTest;

  @Before public void setUp() throws Exception {
    // Mock out the vision service for unit tests.
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpTransport transport = new MockHttpTransport() {
      @Override
      public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
        return new MockLowLevelHttpRequest() {
          @Override
          public LowLevelHttpResponse execute() throws IOException {
            MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
            response.setStatusCode(200);
            response.setContentType(Json.MEDIA_TYPE);
            response.setContent("{\"responses\": [{\"textAnnotations\": []}]}");
            return response;
          }
        };
      }
    };
    Vision vision = new Vision(transport, jsonFactory, null);

    appUnderTest = new TextApp(vision, null /* index */);
  }

  @Test public void detectText_withImage_returnsPath() throws Exception {
    List<ImageText> image =
        appUnderTest.detectText(ImmutableList.<Path>of(Paths.get("data/wakeupcat.jpg")));

    assertThat(image.get(0).path().toString())
        .named("wakeupcat.jpg path")
        .isEqualTo("data/wakeupcat.jpg");
  }
}
