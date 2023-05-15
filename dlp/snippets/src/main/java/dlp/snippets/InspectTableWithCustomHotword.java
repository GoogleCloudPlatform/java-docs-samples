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

// [START dlp_inspect_column_values_w_custom_hotwords]

import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.privacy.dlp.v2.ContentItem;
import com.google.privacy.dlp.v2.CustomInfoType;
import com.google.privacy.dlp.v2.FieldId;
import com.google.privacy.dlp.v2.Finding;
import com.google.privacy.dlp.v2.InfoType;
import com.google.privacy.dlp.v2.InspectConfig;
import com.google.privacy.dlp.v2.InspectContentRequest;
import com.google.privacy.dlp.v2.InspectContentResponse;
import com.google.privacy.dlp.v2.InspectionRule;
import com.google.privacy.dlp.v2.InspectionRuleSet;
import com.google.privacy.dlp.v2.Likelihood;
import com.google.privacy.dlp.v2.LocationName;
import com.google.privacy.dlp.v2.Table;
import com.google.privacy.dlp.v2.Value;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InspectTableWithCustomHotword {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    // The Google Cloud project id to use as a parent resource.
    String projectId = "your-project-id";
    // Specify the table to be considered for de-identification.
    Table tableToDeIdentify =
        Table.newBuilder()
            .addHeaders(FieldId.newBuilder().setName("Some Social Security Number").build())
            .addHeaders(FieldId.newBuilder().setName("Real Social Security Number").build())
            .addRows(
                Table.Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("111-11-1111").build())
                    .addValues(Value.newBuilder().setStringValue("222-22-2222").build())
                    .build())
            .build();
    // Specify the regex pattern to be detected.
    // Refer https://github.com/google/re2/wiki/Syntax for creating regular expression.
    String hotwordRegexPattern = "Some Social Security Number";
    inspectDemotingFindingsWithHotwords(projectId, tableToDeIdentify, hotwordRegexPattern);
  }

  //  Inspects the provided table, excluding the findings of entire column matching regular
  // expression.
  public static void inspectDemotingFindingsWithHotwords(
      String projectId, Table tableToDeIdentify, String hotwordRegexPattern) throws IOException {

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (DlpServiceClient dlp = DlpServiceClient.create()) {
      // Specify what content you want the service to de-identify.
      ContentItem contentItem = ContentItem.newBuilder().setTable(tableToDeIdentify).build();

      CustomInfoType.DetectionRule.LikelihoodAdjustment likelihoodAdjustment =
          CustomInfoType.DetectionRule.LikelihoodAdjustment.newBuilder()
              .setFixedLikelihood(Likelihood.VERY_UNLIKELY)
              .build();

      // Specify the type of info the inspection will look for.
      // See https://cloud.google.com/dlp/docs/infotypes-reference for complete list of info types
      List<InfoType> infoTypes =
          Stream.of("US_SOCIAL_SECURITY_NUMBER")
              .map(it -> InfoType.newBuilder().setName(it).build())
              .collect(Collectors.toList());

      CustomInfoType.DetectionRule.Proximity proximity =
          CustomInfoType.DetectionRule.Proximity.newBuilder().setWindowBefore(1).build();

      // Construct hotword rule.
      CustomInfoType.DetectionRule.HotwordRule hotwordRule =
          CustomInfoType.DetectionRule.HotwordRule.newBuilder()
              .setHotwordRegex(
                  CustomInfoType.Regex.newBuilder().setPattern(hotwordRegexPattern).build())
              .setLikelihoodAdjustment(likelihoodAdjustment)
              .setProximity(proximity)
              .build();

      // Construct rule set for the inspect config.
      InspectionRuleSet inspectionRuleSet =
          InspectionRuleSet.newBuilder()
              .addAllInfoTypes(infoTypes)
              .addRules(InspectionRule.newBuilder().setHotwordRule(hotwordRule))
              .build();

      // Construct the configuration for the Inspect request.
      InspectConfig config =
          InspectConfig.newBuilder()
              .setIncludeQuote(true)
              .setMinLikelihood(Likelihood.POSSIBLE)
              .addRuleSet(inspectionRuleSet)
              .addAllInfoTypes(infoTypes)
              .build();

      // Construct the Inspect request to be sent by the client.
      InspectContentRequest request =
          InspectContentRequest.newBuilder()
              .setParent(LocationName.of(projectId, "global").toString())
              .setItem(contentItem)
              .setInspectConfig(config)
              .build();

      InspectContentResponse response = dlp.inspectContent(request);
      // Parse the response and process results.
      System.out.println("Findings: " + response.getResult().getFindingsCount());
      for (Finding f : response.getResult().getFindingsList()) {
        System.out.println("\tQuote: " + f.getQuote());
        System.out.println("\tInfo type: " + f.getInfoType().getName());
        System.out.println("\tLikelihood: " + f.getLikelihood());
      }
    }
  }
}

// [END dlp_inspect_column_values_w_custom_hotwords]
