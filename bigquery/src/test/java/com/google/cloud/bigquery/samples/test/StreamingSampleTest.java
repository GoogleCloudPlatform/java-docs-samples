/*
 * Copyright (c) 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not  use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.cloud.bigquery.samples.test;

import static com.google.common.truth.Truth.assertThat;

import com.google.api.services.bigquery.model.TableDataInsertAllResponse;
import com.google.cloud.bigquery.samples.StreamingSample;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

/**
 * Tests for streaming sample.
 */
public class StreamingSampleTest extends BigquerySampleTest {

  public StreamingSampleTest() throws JsonSyntaxException, JsonIOException, FileNotFoundException {
    super();
  }

  @Test
  public void testStream() throws IOException {
    JsonReader json =
        new JsonReader(
            new InputStreamReader(
                BigquerySampleTest.class.getResourceAsStream("/streamrows.json")));
    Iterator<TableDataInsertAllResponse> response =
        StreamingSample.run(
            CONSTANTS.getProjectId(),
            CONSTANTS.getDatasetId(),
            CONSTANTS.getCurrentTableId(),
            json);

    while (response.hasNext()) {
      assertThat(response.next()).isNotEmpty();
    }
  }
}
