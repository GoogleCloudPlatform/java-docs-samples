// Copyright 2018 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example;

import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.Validation;

public interface SampleOptions extends DataflowPipelineOptions {

  /**
   * Set inuptFile required parameter to a file path or glob. A local path and Google Cloud Storage
   * path are both supported.
   */
  @Description(
      "Set inuptFile required parameter to a file path or glob. A local path and Google Cloud "
          + "Storage path are both supported.")
  @Validation.Required
  String getInputFile();

  void setInputFile(String value);

  /**
   * Set output required parameter to define output path. A local path and Google Cloud Storage path
   * are both supported.
   */
  @Description(
      "Set output required parameter to define output path. A local path and Google Cloud Storage"
          + " path are both supported.")
  @Validation.Required
  String getOutput();

  void setOutput(String value);

  /**
   * Set avroSchema required parameter to specify location of the schema. A local path and Google
   * Cloud Storage path are supported.
   */
  @Description(
      "Set avroSchema required parameter to specify location of the schema. A local path and "
          + "Google Cloud Storage path are supported.")
  @Validation.Required
  String getAvroSchema();

  void setAvroSchema(String value);


  /**
   * Set csvFormat to one of the predefined Apache-commons CSV formats
   *
   * @see <a href="https://commons.apache.org/proper/commons-csv/apidocs/index.html?org/apache/commons/csv/CSVFormat.Predefined.html">
   *     Apache docs for possible values</a>
   */
  @Description(
      "Set csvFormat to one of the predefined Apache-commons CSV formats. the Default is with" +
              "delimiter:',' quote:'\"' recordSeparator:'\\r\\n' ignoring empty lines")
  @Default.String("Default")
  String getCsvFormat();

  void setCsvFormat(String csvFormat);
}
