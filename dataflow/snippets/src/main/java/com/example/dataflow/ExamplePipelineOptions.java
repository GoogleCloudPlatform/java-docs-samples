/*
 * Copyright 2023 Google LLC
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

package com.example.dataflow;

import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.StreamingOptions;

/**
 * Extends PipelineOptions and adds custom pipeline options for this sample.
 */
public interface ExamplePipelineOptions extends StreamingOptions {
  @Description("Project ID for the BigQuery table")
  String getProjectId();

  void setProjectId(String value);

  @Description("Dataset for the BigQuery table")
  String getDatasetName();

  void setDatasetName(String value);

  @Description("BigQuery table name")
  String getTableName();

  void setTableName(String value);
}
