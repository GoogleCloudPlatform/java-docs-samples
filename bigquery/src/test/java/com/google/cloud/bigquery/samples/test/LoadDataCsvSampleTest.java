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

import com.google.cloud.bigquery.samples.LoadDataCsvSample;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Tests for sample that loads data from CSV.
 */
@RunWith(JUnit4.class)
public class LoadDataCsvSampleTest {

  @Test
  public void testLoadData() throws IOException, InterruptedException {
    InputStreamReader is =
        new InputStreamReader(LoadDataCsvSample.class.getResourceAsStream("/schema.json"));
    LoadDataCsvSample.run(
        Constants.CLOUD_STORAGE_INPUT_URI,
        Constants.PROJECT_ID,
        Constants.DATASET_ID,
        Constants.NEW_TABLE_ID,
        is,
        5000L);
  }
}
