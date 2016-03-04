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
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Tests for sample that loads data from CSV.
 */
public class LoadDataCsvSampleTest extends BigquerySampleTest {

  public LoadDataCsvSampleTest()
      throws JsonSyntaxException, JsonIOException, FileNotFoundException {
    super();
  }

  @Test
  public void testLoadData() throws IOException, InterruptedException {
    InputStreamReader is =
        new InputStreamReader(LoadDataCsvSample.class.getResourceAsStream("/schema.json"));
    LoadDataCsvSample.run(
        CONSTANTS.getCloudStorageInputUri(),
        CONSTANTS.getProjectId(),
        CONSTANTS.getDatasetId(),
        CONSTANTS.getNewTableId(),
        is,
        5000L);
  }


}
