/*
 * Copyright 2019 Google LLC
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

package com.google.cloud.language.automl.entityextraction.samples;

// [START automl_natural_language_entity_get_operation_status]

import com.google.cloud.automl.v1beta1.AutoMlClient;
import com.google.longrunning.Operation;
import java.io.IOException;

class GetOperationStatus {

  // Get the status of a given operation
  static void getOperationStatus(String operationFullId) throws IOException {
    // String operationFullId = "COMPLETE_NAME_OF_OPERATION";
    // like: projects/[projectId]/locations/us-central1/operations/[operationId]

    // Instantiates a client
    try (AutoMlClient client = AutoMlClient.create()) {

      // Get the latest state of a long-running operation.
      Operation response = client.getOperationsClient().getOperation(operationFullId);

      // Display operation details.
      System.out.println(String.format("Operation details:"));
      System.out.println(String.format("\tName: %s", response.getName()));
      System.out.println(String.format("\tMetadata:"));
      System.out.println(String.format("\t\tType Url: %s", response.getMetadata().getTypeUrl()));
      System.out.println(
          String.format(
              "\t\tValue: %s", response.getMetadata().getValue().toStringUtf8().replace("\n", "")));
      System.out.println(String.format("\tDone: %s", response.getDone()));
      if (response.hasResponse()) {
        System.out.println("\tResponse:");
        System.out.println(String.format("\t\tType Url: %s", response.getResponse().getTypeUrl()));
        System.out.println(
            String.format(
                "\t\tValue: %s",
                response.getResponse().getValue().toStringUtf8().replace("\n", "")));
      }
      if (response.hasError()) {
        System.out.println("\tResponse:");
        System.out.println(String.format("\t\tError code: %s", response.getError().getCode()));
        System.out
            .println(String.format("\t\tError message: %s", response.getError().getMessage()));
      }
    }
  }
}
// [END automl_natural_language_entity_get_operation_status]
