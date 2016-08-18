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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.util.Iterator;

/**
 * Tests for synchronous query sample.
 */
@RunWith(JUnit4.class)
public class SyncQuerySampleTest {

  @Test
  public void testSyncQuery() throws IOException {
    Iterator<GetQueryResultsResponse> pages =
        SyncQuerySample.run(
            Constants.PROJECT_ID,
            "SELECT corpus FROM `publicdata.samples.shakespeare` GROUP BY corpus;",
            10000,
            false /* useLegacySql */);
    while (pages.hasNext()) {
      assertThat(pages.next().getRows()).isNotEmpty();
    }
  }

  @Test
  public void testSyncQueryLegacySql() throws IOException {
    Iterator<GetQueryResultsResponse> pages =
        SyncQuerySample.run(Constants.PROJECT_ID, Constants.QUERY, 10000, true /* useLegacySql */);
    while (pages.hasNext()) {
      assertThat(pages.next().getRows()).isNotEmpty();
    }
  }
}
