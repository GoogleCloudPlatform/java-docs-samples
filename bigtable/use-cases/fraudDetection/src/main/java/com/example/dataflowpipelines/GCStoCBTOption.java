/*
 * Copyright 2020 Google LLC
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
package com.example.dataflowpipelines;

import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.options.Description;

/*
 * This interface contains all the necessary command line arguments when
 * writing to CBT.
 */
public interface GCStoCBTOption extends DataflowPipelineOptions {

  /**
   * @return Cloud project id.
   */
  @Description("The CBT project id.")
  String getProjectID();

  /**
   * @param projectID the Cloud project id.
   */
  void setProjectID(String projectID);

  /**
   * @return Cloud Bigtable instance id.
   */
  @Description("The CBT instance id.")
  String getCBTInstanceId();

  /**
   * @param instanceID the Cloud Bigtable instance id.
   */
  void setCBTInstanceId(String instanceID);

  /**
   * @return Cloud Bigtable table id.
   */
  @Description("The destination CBT table id.")
  String getCBTTableId();

  /**
   * @param tableID Cloud Bigtable table id.
   */
  void setCBTTableId(String tableID);

  /**
   * @return customer demographics input file.
   */
  @Description("The Cloud Storage path to the demographics CSV file.")
  String getDemographicsInputFile();

  /**
   * @param location customer demographics file location.
   */
  void setDemographicsInputFile(String location);

  /**
   * @return transactions history input file.
   */
  @Description("The Cloud Storage path to the history CSV file.")
  String getHistoryInputFile();

  /**
   * @param location transaction history file location.
   */
  void setHistoryInputFile(String location);
}
