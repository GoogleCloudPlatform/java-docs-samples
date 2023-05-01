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

// [START dlp_deidentify_table_with_crypto_hash]

import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.privacy.dlp.v2.ContentItem;
import com.google.privacy.dlp.v2.CryptoHashConfig;
import com.google.privacy.dlp.v2.CryptoKey;
import com.google.privacy.dlp.v2.DeidentifyConfig;
import com.google.privacy.dlp.v2.DeidentifyContentRequest;
import com.google.privacy.dlp.v2.DeidentifyContentResponse;
import com.google.privacy.dlp.v2.FieldId;
import com.google.privacy.dlp.v2.InfoType;
import com.google.privacy.dlp.v2.InfoTypeTransformations;
import com.google.privacy.dlp.v2.InspectConfig;
import com.google.privacy.dlp.v2.LocationName;
import com.google.privacy.dlp.v2.PrimitiveTransformation;
import com.google.privacy.dlp.v2.Table;
import com.google.privacy.dlp.v2.TransientCryptoKey;
import com.google.privacy.dlp.v2.Value;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DeIdentifyTableWithCryptoHash {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.

    // The Google Cloud project id to use as a parent resource.
    String projectId = "your-project-id";

    // The table to de-identify.
    Table tableToDeIdentify =
        Table.newBuilder()
            .addHeaders(FieldId.newBuilder().setName("userid").build())
            .addHeaders(FieldId.newBuilder().setName("comments").build())
            .addRows(
                Table.Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("user1@example.org").build())
                    .addValues(
                        Value.newBuilder()
                            .setStringValue(
                                "my email is user1@example.org and phone is 858-555-0222")
                            .build())
                    .build())
            .addRows(
                Table.Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("user2@example.org").build())
                    .addValues(
                        Value.newBuilder()
                            .setStringValue(
                                "my email is user2@example.org and phone is 858-555-0223")
                            .build())
                    .build())
            .addRows(
                Table.Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("user3@example.org").build())
                    .addValues(
                        Value.newBuilder()
                            .setStringValue(
                                "my email is user3@example.org and phone is 858-555-0224")
                            .build())
                    .build())
            .build();

    // The randomly generated crypto key to encrypt the data.
    String transientKeyName = "YOUR_TRANSIENT_CRYPTO_KEY";
    deIdentifyWithCryptHashTransformation(projectId, tableToDeIdentify, transientKeyName);
  }

  // Transforms findings using a cryptographic hash transformation.
  public static void deIdentifyWithCryptHashTransformation(
      String projectId, Table tableToDeIdentify, String transientKeyName) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (DlpServiceClient dlp = DlpServiceClient.create()) {
      // Specify what content you want the service to DeIdentify
      ContentItem contentItem = ContentItem.newBuilder().setTable(tableToDeIdentify).build();

      // Specify the type of info the inspection will look for.
      // See https://cloud.google.com/dlp/docs/infotypes-reference for complete list of info types
      List<InfoType> infoTypes =
          Stream.of("PHONE_NUMBER", "EMAIL_ADDRESS")
              .map(it -> InfoType.newBuilder().setName(it).build())
              .collect(Collectors.toList());

      InspectConfig inspectConfig = InspectConfig.newBuilder()
              .addAllInfoTypes(infoTypes)
              .build();

      // Specify the transient key which will encrypt the data.
      TransientCryptoKey transientCryptoKey = TransientCryptoKey.newBuilder()
              .setName(transientKeyName)
              .build();

      CryptoKey cryptoKey = CryptoKey.newBuilder()
              .setTransient(transientCryptoKey)
              .build();

      // Specify how the info from the inspection should be encrypted.
      CryptoHashConfig cryptoHashConfig = CryptoHashConfig.newBuilder()
              .setCryptoKey(cryptoKey)
              .build();

      // Define type of de-identification as cryptographic hash transformation.
      PrimitiveTransformation primitiveTransformation = PrimitiveTransformation.newBuilder()
              .setCryptoHashConfig(cryptoHashConfig)
              .build();

      InfoTypeTransformations.InfoTypeTransformation infoTypeTransformation =
          InfoTypeTransformations.InfoTypeTransformation.newBuilder()
              .setPrimitiveTransformation(primitiveTransformation)
              .addAllInfoTypes(infoTypes)
              .build();

      InfoTypeTransformations transformations = InfoTypeTransformations.newBuilder()
              .addTransformations(infoTypeTransformation)
              .build();

      // Specify the config for the de-identify request
      DeidentifyConfig deidentifyConfig = DeidentifyConfig.newBuilder()
              .setInfoTypeTransformations(transformations)
              .build();

      // Combine configurations into a request for the service.
      DeidentifyContentRequest request =
          DeidentifyContentRequest.newBuilder()
              .setParent(LocationName.of(projectId, "global").toString())
              .setItem(contentItem)
              .setInspectConfig(inspectConfig)
              .setDeidentifyConfig(deidentifyConfig)
              .build();

      // Send the request and receive response from the service
      DeidentifyContentResponse response = dlp.deidentifyContent(request);

      // Print the results
      System.out.println("Table after de-identification: " + response.getItem().getTable());
    }
  }
}

// [END dlp_deidentify_table_with_crypto_hash]
