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

package dlp.snippets;

// [START dlp_deidentify_time_extract]

import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.privacy.dlp.v2.ContentItem;
import com.google.privacy.dlp.v2.DeidentifyConfig;
import com.google.privacy.dlp.v2.DeidentifyContentRequest;
import com.google.privacy.dlp.v2.DeidentifyContentResponse;
import com.google.privacy.dlp.v2.FieldId;
import com.google.privacy.dlp.v2.FieldTransformation;
import com.google.privacy.dlp.v2.LocationName;
import com.google.privacy.dlp.v2.PrimitiveTransformation;
import com.google.privacy.dlp.v2.RecordTransformations;
import com.google.privacy.dlp.v2.Table;
import com.google.privacy.dlp.v2.TimePartConfig;
import com.google.privacy.dlp.v2.Value;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class DeIdentifyWithTimeExtraction {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    // The Google Cloud project id to use as a parent resource.
    String projectId = "your-project-id";
    Table tableToDeIdentify =
        Table.newBuilder()
            .addHeaders(FieldId.newBuilder().setName("Name").build())
            .addHeaders(FieldId.newBuilder().setName("Birth Date").build())
            .addHeaders(FieldId.newBuilder().setName("Credit Card").build())
            .addHeaders(FieldId.newBuilder().setName("Register Date").build())
            .addRows(
                Table.Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("Ann").build())
                    .addValues(Value.newBuilder().setStringValue("01/01/1970").build())
                    .addValues(Value.newBuilder().setStringValue("4532908762519852").build())
                    .addValues(Value.newBuilder().setStringValue("07/21/1996").build())
                    .build())
            .addRows(
                Table.Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("James").build())
                    .addValues(Value.newBuilder().setStringValue("03/06/1988").build())
                    .addValues(Value.newBuilder().setStringValue("4301261899725540").build())
                    .addValues(Value.newBuilder().setStringValue("04/09/2001").build())
                    .build())
            .build();
    deIdentifyWithDateShift(projectId, tableToDeIdentify);
  }

  public static Table deIdentifyWithDateShift(String projectId, Table tableToDeIdentify)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (DlpServiceClient dlp = DlpServiceClient.create()) {
      // Read the contents of the Table

      ContentItem item = ContentItem.newBuilder().setTable(tableToDeIdentify).build();

      // Specify the time part to extract
      TimePartConfig timePartConfig =
          TimePartConfig.newBuilder().setPartToExtract(TimePartConfig.TimePart.YEAR).build();

      PrimitiveTransformation transformation =
          PrimitiveTransformation.newBuilder().setTimePartConfig(timePartConfig).build();

      // Specify which fields the TimePart should apply too
      List<FieldId> dateFields =
          Arrays.asList(
              FieldId.newBuilder().setName("Birth Date").build(),
              FieldId.newBuilder().setName("Register Date").build());

      FieldTransformation fieldTransformation =
          FieldTransformation.newBuilder()
              .addAllFields(dateFields)
              .setPrimitiveTransformation(transformation)
              .build();

      RecordTransformations recordTransformations =
          RecordTransformations.newBuilder().addFieldTransformations(fieldTransformation).build();

      // Specify the config for the de-identify request
      DeidentifyConfig deidentifyConfig =
          DeidentifyConfig.newBuilder().setRecordTransformations(recordTransformations).build();

      // Combine configurations into a request for the service.
      DeidentifyContentRequest request =
          DeidentifyContentRequest.newBuilder()
              .setParent(LocationName.of(projectId, "global").toString())
              .setItem(item)
              .setDeidentifyConfig(deidentifyConfig)
              .build();

      // Send the request and receive response from the service
      DeidentifyContentResponse response = dlp.deidentifyContent(request);
      System.out.println("Table after de-identification: " + response.getItem().getTable());
      return response.getItem().getTable();
    }
  }
}

// [END dlp_deidentify_time_extract]
