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

package com.google.cloud.language.samples;

import static com.google.common.truth.Truth.assertThat;

import com.google.api.services.language.v1beta1.model.Entity;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Unit tests for {@link Analyze}.
 */
@RunWith(JUnit4.class)
public class AnalyzeTest {
  private static final int MAX_RESULTS = 3;

  @Test public void printEntities_withNull_printsNoneFound() throws Exception {
    // Arrange
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(bout);

    // Act
    Analyze.printEntities(out, null);

    // Assert
    String got = bout.toString();
    assertThat(got).contains("No entities found");
  }

  @Test public void printEntities_withEmpty_printsNoneFound() throws Exception {
    // Arrange
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(bout);
    ImmutableList<Entity> entities = ImmutableList.of();

    // Act
    Analyze.printEntities(out, entities);

    // Assert
    String got = bout.toString();
    assertThat(got).contains("No entities found");
  }

  @Test public void printEntities_withEntities_printsEntities() throws Exception {
    // Arrange
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(bout);
    ImmutableList<Entity> entities =
        ImmutableList.of(
            new Entity().setName("Larry Page").setSalience(0.426f).setType("PERSON").setMetadata(
                ImmutableMap.<String, String>builder()
                    .put("knowledge_graph_mid", "/m/0gjpq")
                    .put("wikipedia_url", "http://en.wikipedia.org/wiki/index.html?curid=60903")
                    .build()),
            new Entity().setName("search engine").setSalience(0.188f).setType("CONSUMER_GOOD"),
            new Entity().setName("something"));

    // Act
    Analyze.printEntities(out, entities);

    // Assert
    String got = bout.toString();
    assertThat(got).contains("Found 3 entities.");
    assertThat(got).contains("Larry Page");
    assertThat(got).contains("Salience: 0.426");
    assertThat(got).contains("Type: PERSON");
    assertThat(got).contains("Metadata: knowledge_graph_mid = /m/0gjpq");
    assertThat(got)
        .contains("Metadata: wikipedia_url = http://en.wikipedia.org/wiki/index.html?curid=60903");
    assertThat(got).contains("search engine");
    assertThat(got).contains("Salience: 0.188");
    assertThat(got).contains("Type: CONSUMER_GOOD");
    assertThat(got).contains("something");
  }

  @Test public void printSentiment_withNull_printsNoneFound() throws Exception {
    // Arrange
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(bout);

    // Act
    Analyze.printSentiment(out, null);

    // Assert
    String got = bout.toString();
    assertThat(got).contains("No sentiment found");
  }

  @Test public void printSyntax_withNull_printsNoneFound() throws Exception {
    // Arrange
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(bout);

    // Act
    Analyze.printSyntax(out, null);

    // Assert
    String got = bout.toString();
    assertThat(got).contains("No syntax found");
  }
}
