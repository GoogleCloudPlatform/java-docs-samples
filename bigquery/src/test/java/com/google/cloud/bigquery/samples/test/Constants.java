/*
 * Copyright (c) 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not  use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http=//www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.cloud.bigquery.samples.test;

public class Constants {
  public static final String PROJECT_ID = "cloud-samples-tests";
  public static final String DATASET_ID = "test_dataset_java";
  public static final String CURRENT_TABLE_ID = "test_table_java";
  public static final String NEW_TABLE_ID = "test_table_java_2";
  public static final String CLOUD_STORAGE_INPUT_URI = "gs://cloud-samples-tests/data.csv";
  public static final String CLOUD_STORAGE_OUTPUT_URI = "gs://cloud-samples-tests/output.csv";
  public static final String QUERY =
      "SELECT corpus FROM publicdata:samples.shakespeare GROUP BY corpus;";
}
