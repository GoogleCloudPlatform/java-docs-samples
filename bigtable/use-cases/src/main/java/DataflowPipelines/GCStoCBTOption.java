/*
 * Copyright 2022 Google LLC
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
package DataflowPipelines;

import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.options.Description;

// This interface contains all the necessary command line arguments when writing to CBT.
public interface GCStoCBTOption extends DataflowPipelineOptions {

  @Description("The CBT project id.")
  String getProjectID();

  void setProjectID(String headers);

  @Description("The CBT instance id.")
  String getCBTInstanceId();

  void setCBTInstanceId(String headers);

  @Description("The destination CBT table id.")
  String getCBTTableId();

  void setCBTTableId(String headers);

  @Description("The CBT table family.")
  String getCBTTableFamily();

  void setCBTTableFamily(String headers);

  @Description("The headers for the CSV file.")
  String getHeaders();

  void setHeaders(String headers);

  @Description("The Cloud Storage path to the demographics CSV file.")
  String getDemographicsInputFile();

  void setDemographicsInputFile(String location);

  @Description("The Cloud Storage path to the history CSV file.")
  String getHistoryInputFile();

  void setHistoryInputFile(String location);
}
