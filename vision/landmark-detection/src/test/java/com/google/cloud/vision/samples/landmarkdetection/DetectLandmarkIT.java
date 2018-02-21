/*
 * Copyright 2016 Google Inc.
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

package com.google.cloud.vision.samples.landmarkdetection;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.google.api.services.vision.v1.model.EntityAnnotation;
import java.io.IOException;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Integration (system) tests for {@link DetectLandmark}.
 **/
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class DetectLandmarkIT {
  private static final int MAX_RESULTS = 3;
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String BUCKET = PROJECT_ID;
  private static final String LANDMARK_URI = "gs://" + BUCKET + "/vision/water.jpg";
  private static final String PRIVATE_LANDMARK_URI =
      "gs://" + BUCKET + "/vision/water-private.jpg";

  private DetectLandmark appUnderTest;

  @Before public void setUp() throws Exception {
    appUnderTest = new DetectLandmark(DetectLandmark.getVisionService());
  }

  @Test public void identifyLandmark_withLandmark_returnsKnownLandmark() throws Exception {
    List<EntityAnnotation> landmarks = appUnderTest.identifyLandmark(LANDMARK_URI, MAX_RESULTS);

    assertThat(landmarks).named("water.jpg landmarks").isNotEmpty();
    assertThat(landmarks.get(0).getDescription())
        .named("water.jpg landmark #0 description")
        .isEqualTo("Taitung, Famous Places \"up the water flow\" marker");
  }

  @Test public void identifyLandmark_noImage_throwsNotFound() throws Exception {
    try {
      appUnderTest.identifyLandmark(LANDMARK_URI + "/nonexistent.jpg", MAX_RESULTS);
      fail("Expected IOException");
    } catch (IOException expected) {
      assertThat(expected.getMessage()).named("IOException message").contains("file");
    }
  }

  // TODO(lesv): Currently we can access it, need to set better attributes.
  //   @Test public void identifyLandmark_noImage_throwsForbidden() throws Exception {
  //     try {
  //       appUnderTest.identifyLandmark(PRIVATE_LANDMARK_URI, MAX_RESULTS);
  //       fail("Expected IOException");
  //     } catch (IOException expected) {
  //       assertThat(expected.getMessage()).named("IOException message").contains("permission");
  //     }
  //   }
}
