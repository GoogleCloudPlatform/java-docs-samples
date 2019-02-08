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

package com.google.cloud.vision.samples.label;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Integration (system) tests for {@link LabelApp}.
 */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class LabelAppIT {
  private static final int MAX_LABELS = 3;

  private LabelApp appUnderTest;

  @Before public void setUp() throws Exception {
    appUnderTest = new LabelApp(LabelApp.getVisionService());
  }

  @Test public void labelImage_cat_returnsCatDescription() throws Exception {
    List<EntityAnnotation> labels =
        appUnderTest.labelImage(Paths.get("data/cat.jpg"), MAX_LABELS);

    ImmutableSet.Builder<String> builder = ImmutableSet.builder();
    for (EntityAnnotation label : labels) {
      builder.add(label.getDescription().toLowerCase());
    }
    ImmutableSet<String> descriptions = builder.build();

    assertThat(descriptions).named("cat.jpg labels").contains("cat");
  }

  @Test public void labelImage_badImage_throwsException() throws Exception {
    try {
      appUnderTest.labelImage(Paths.get("data/bad.txt"), MAX_LABELS);
      fail("Expected IOException");
    } catch (IOException expected) {
      assertThat(expected.getMessage()).isNotEmpty();
    }
  }
}
