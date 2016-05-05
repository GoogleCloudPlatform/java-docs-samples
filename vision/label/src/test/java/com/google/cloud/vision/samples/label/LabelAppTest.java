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

package com.google.cloud.vision.samples.label;

import static com.google.common.truth.Truth.assertThat;

import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.common.collect.ImmutableList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Paths;

/**
 * Unit tests for {@link LabelApp}.
 */
@RunWith(JUnit4.class)
public class LabelAppTest {

  @Test public void printLabels_emptyList_printsNoLabelsFound() throws Exception {
    // Arrange
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(bout);

    // Act
    LabelApp.printLabels(
        out, Paths.get("path/to/some/image.jpg"), ImmutableList.<EntityAnnotation>of());

    // Assert
    assertThat(bout.toString()).contains("No labels found.");
  }

  @Test public void printLabels_manyLabels_printsLabels() throws Exception {
    // Arrange
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(bout);
    ImmutableList<EntityAnnotation> labels =
        ImmutableList.of(
            new EntityAnnotation().setDescription("dog").setScore(0.7564f),
            new EntityAnnotation().setDescription("husky").setScore(0.67891f),
            new EntityAnnotation().setDescription("poodle").setScore(0.1233f));

    // Act
    LabelApp.printLabels(out, Paths.get("path/to/some/image.jpg"), labels);

    // Assert
    String got = bout.toString();
    assertThat(got).contains("dog (score: 0.756)");
    assertThat(got).contains("husky (score: 0.679)");
    assertThat(got).contains("poodle (score: 0.123)");
  }
}
