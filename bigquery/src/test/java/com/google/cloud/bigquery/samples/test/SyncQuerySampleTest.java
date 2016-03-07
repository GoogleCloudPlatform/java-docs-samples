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

import com.google.api.services.bigquery.model.GetQueryResultsResponse;
import com.google.cloud.bigquery.samples.SyncQuerySample;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

/**
 * Tests for synchronous query sample.
 */
public class SyncQuerySampleTest extends BigquerySampleTest {

  public SyncQuerySampleTest()
      throws JsonSyntaxException, JsonIOException, FileNotFoundException {
    super();
  }

  @Test
  public void testSyncQuery() throws IOException {
    Iterator<GetQueryResultsResponse> pages =
        SyncQuerySample.run(
            CONSTANTS.getProjectId(),
            CONSTANTS.getQuery(),
            10000);
    while (pages.hasNext()) {
      assertThat(pages.next().getRows()).isNotEmpty();
    }
  }
}
