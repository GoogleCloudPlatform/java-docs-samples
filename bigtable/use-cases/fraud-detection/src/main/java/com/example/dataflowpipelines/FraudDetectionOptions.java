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
package com.example.dataflowpipelines;

import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.Validation.Required;

/*
 * All the command line arguments needed for the fraud-detection
 * dataflow pipeline.
 */
public interface FraudDetectionOptions extends DataflowPipelineOptions {

  /**
   * @return Cloud project id.
   */
  @Description("The project id.")
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
   * @param instanceId the Cloud Bigtable instance id.
   */
  void setCBTInstanceId(String instanceId);

  /**
   * @return Cloud Bigtable table id.
   */
  @Description("The destination CBT table id.")
  String getCBTTableId();

  /**
   * @param tableId the Cloud Bigtable table id.
   */
  void setCBTTableId(String tableId);

  /**
   * @return Cloud Pubsub input topic.
   */
  @Description("The Cloud Pub/Sub subscription to read from.")
  @Required
  String getInputTopic();

  /**
   * @param topic the Cloud Pubsub input topic.
   */
  void setInputTopic(String topic);

  /**
   * @return Cloud Pubsub output topic.
   */
  @Description("The Cloud Pub/Sub topic to write to.")
  @Required
  String getOutputTopic();

  /**
   * @param topic the Cloud Pubsub output topic.
   */
  void setOutputTopic(String topic);

  /**
   * @return ML endpoint.
   */
  @Description("The ML endpoint to query.")
  String getMLEndpoint();

  /**
   * @param mlEndpoint the ML endpoint to use.
   */
  void setMLEndpoint(String mlEndpoint);

  /**
   * @return ML model region.
   */
  @Description("The ML model region used.")
  String getMLRegion();

  /**
   * @param region the region where the ML model is located.
   */
  void setMLRegion(String region);
}

