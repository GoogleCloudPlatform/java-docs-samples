/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// This sample is HAND-WRITTEN

package com.google.cloud.examples.automl.v1beta1;

import com.google.cloud.automl.v1beta1.AutoMlClient;
import com.google.longrunning.Operation;
import com.google.longrunning.OperationsClient;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class AutomlGetOperation {
  // [START automl_get_operation]

  /**
   * Get Long-Running Operation Status
   *
   * @param operationId Required. The ID of the operation.
   * @param project Required. Your Google Cloud Project ID.
   */
  public static void sampleGetOperation(String project, String operationId) {
    try (AutoMlClient client = AutoMlClient.create()) {

      OperationsClient operationsClient = client.getOperationsClient();

      // project = '[Google Cloud Project ID]'
      // operation_id = '[Operation ID]'
      String name = String.format("projects/%s/locations/us-central1/operations/%s",
              project, operationId);

      Operation operation = operationsClient.getOperation(name);

      // Print Operation status and info
      System.out.printf("Operation Name: %s", (operation.getName()));
      System.out.printf("Done: %s", (operation.getDone()));
      System.out.printf("Response: %s",(operation.getResponse()));
      System.out.printf("Metadata: %s", (operation.getMetadata()));

    } catch (Exception exception) {
      System.err.println("Failed to create the client due to: " + exception);
    }
  }
  // [END automl_get_operation]

  public static void main(String[] args) throws Exception {
    Options options = new Options();
    options.addOption(Option.builder("").required(true).hasArg(true).longOpt("operation_id").build());
    options.addOption(Option.builder("").required(true).hasArg(true).longOpt("project").build());

    CommandLine cl = (new DefaultParser()).parse(options, args);

    String modelId = cl.getOptionValue("operation_id", "[Operation ID]");
    String project = cl.getOptionValue("project", "[Google Cloud Project ID]");
    sampleGetOperation(project, modelId);

  }
}

