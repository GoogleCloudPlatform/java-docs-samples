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
import com.google.cloud.automl.v1beta1.LocationName;
import com.google.longrunning.Operation;
import com.google.longrunning.OperationsClient;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class AutomlListOperations {
  // [START automl_list_operations]

  /**
   * List Long-Running Operations
   *
   * @param filter Filter operations.
   * @param project Required. Your Google Cloud Project ID.
   */
  public static void sampleListOperations(String project, String filter) {
    try (AutoMlClient client = AutoMlClient.create()) {

      OperationsClient operationsClient = client.getOperationsClient();

      // project = '[Google Cloud Project ID]'
      LocationName parent = LocationName.of(project, "us-central1");

      // Iterate over all results
      for (Operation responseItem :
              operationsClient.listOperations(parent.toString(), filter).iterateAll()) {
        System.out.printf("Operation Name: %s", (responseItem.getName()));
        System.out.printf("Done: %s", (responseItem.getDone()));
        System.out.printf("Response: %s",(responseItem.getResponse()));
        System.out.printf("Metadata: %s", (responseItem.getMetadata()));
      }

    } catch (Exception exception) {
      System.err.println("Failed to create the client due to: " + exception);
    }
  }
  // [END automl_list_operations]

  public static void main(String[] args) throws Exception {
    Options options = new Options();
    options.addOption(Option.builder("").required(false).hasArg(true).longOpt("filter").build());
    options.addOption(Option.builder("").required(true).hasArg(true).longOpt("project").build());

    CommandLine cl = (new DefaultParser()).parse(options, args);

    String modelId = cl.getOptionValue("filter", "");
    String project = cl.getOptionValue("project", "[Google Cloud Project ID]");
    sampleListOperations(project, modelId);

  }
}
