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

package com.google.cloud.vision.samples.facedetect;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.Assert.fail;

import com.google.cloud.vision.v1.FaceAnnotation;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Integration (system) tests for {@link FaceDetectApp}. */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class FaceDetectAppIT {
  private static final int MAX_RESULTS = 3;

  private ImageAnnotatorClient client;
  private FaceDetectApp appUnderTest;

  @Before
  public void setUp() throws Exception {
    client = ImageAnnotatorClient.create();
    appUnderTest = new FaceDetectApp(client);
  }

  @After
  public void tearDown() throws Exception {
    if (client != null) {
      client.close();
    }
  }

  @Test
  public void detectFaces_withFace_returnsAtLeastOneFace() throws Exception {
    List<FaceAnnotation> faces = appUnderTest.detectFaces(Paths.get("data/face.jpg"), MAX_RESULTS);

    assertWithMessage("face.jpg faces").that(faces).isNotEmpty();
    assertThat(faces.get(0).getFdBoundingPoly().getVerticesList()).isNotEmpty();
  }

  @Test
  public void detectFaces_badImage_throwsException() throws Exception {
    try {
      appUnderTest.detectFaces(Paths.get("data/bad.txt"), MAX_RESULTS);
      fail("Expected IOException");
    } catch (IOException expected) {
      assertThat(expected.getMessage()).isNotEmpty();
    }
  }
}