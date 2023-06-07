/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package aiplatform;

// [START aiplatform_sdk_summarization]

import com.google.cloud.aiplatform.v1beta1.EndpointName;
import com.google.cloud.aiplatform.v1beta1.PredictResponse;
import com.google.cloud.aiplatform.v1beta1.PredictionServiceClient;
import com.google.cloud.aiplatform.v1beta1.PredictionServiceSettings;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Text Summarization with a Large Language Model
public class PredictTextSummarizationSample {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    // Designing prompts for text summerization with supported large language models:
    // https://cloud.google.com/vertex-ai/docs/generative-ai/text/summarization-prompts
    String instance =
        "{ \"content\": \"Background: There is evidence that there have been significant changes \n"
            + "in Amazon rainforest vegetation over the last 21,000 years through the Last \n"
            + "Glacial Maximum (LGM) and subsequent deglaciation. Analyses of sediment \n"
            + "deposits from Amazon basin paleo lakes and from the Amazon Fan indicate that \n"
            + "rainfall in the basin during the LGM was lower than for the present, and this \n"
            + "was almost certainly associated with reduced moist tropical vegetation cover \n"
            + "in the basin. There is debate, however, over how extensive this reduction \n"
            + "was. Some scientists argue that the rainforest was reduced to small, isolated \n"
            + "refugia separated by open forest and grassland; other scientists argue that \n"
            + "the rainforest remained largely intact but extended less far to the north, \n"
            + "south, and east than is seen today. This debate has proved difficult to \n"
            + "resolve because the practical limitations of working in the rainforest mean \n"
            + "that data sampling is biased away from the center of the Amazon basin, and \n"
            + "both explanations are reasonably well supported by the available data.\n"
            + "\n"
            + "Q: What does LGM stands for?\n"
            + "A: Last Glacial Maximum.\n"
            + "\n"
            + "Q: What did the analysis from the sediment deposits indicate?\n"
            + "A: Rainfall in the basin during the LGM was lower than for the present.\n"
            + "\n"
            + "Q: What are some of scientists arguments?\n"
            + "A: The rainforest was reduced to small, isolated refugia separated by open forest"
            + " and grassland.\n"
            + "\n"
            + "Q: There have been major changes in Amazon rainforest vegetation over the last how"
            + " many years?\n"
            + "A: 21,000.\n"
            + "\n"
            + "Q: What caused changes in the Amazon rainforest vegetation?\n"
            + "A: The Last Glacial Maximum (LGM) and subsequent deglaciation\n"
            + "\n"
            + "Q: What has been analyzed to compare Amazon rainfall in the past and present?\n"
            + "A: Sediment deposits.\n"
            + "\n"
            + "Q: What has the lower rainfall in the Amazon during the LGM been attributed to?\n"
            + "A:\"}";
    String parameters =
        "{\n"
            + "  \"temperature\": 0,\n"
            + "  \"maxOutputTokens\": 32,\n"
            + "  \"topP\": 0,\n"
            + "  \"topK\": 1\n"
            + "}";
    String project = "YOUR_PROJECT_ID";
    String location = "us-central1";
    String publisher = "google";
    String model = "text-bison@001";

    predictTextSummarization(instance, parameters, project, location, publisher, model);
  }

  // Get summarization from a supported text model
  public static void predictTextSummarization(
      String instance,
      String parameters,
      String project,
      String location,
      String publisher,
      String model)
      throws IOException {
    String endpoint = String.format("%s-aiplatform.googleapis.com:443", location);
    PredictionServiceSettings predictionServiceSettings =
        PredictionServiceSettings.newBuilder()
            .setEndpoint(endpoint)
            .build();

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (PredictionServiceClient predictionServiceClient =
        PredictionServiceClient.create(predictionServiceSettings)) {
      final EndpointName endpointName =
          EndpointName.ofProjectLocationPublisherModelName(project, location, publisher, model);

      // Use Value.Builder to convert instance to a dynamically typed value that can be
      // processed by the service.
      Value.Builder instanceValue = Value.newBuilder();
      JsonFormat.parser().merge(instance, instanceValue);
      List<Value> instances = new ArrayList<>();
      instances.add(instanceValue.build());

      // Use Value.Builder to convert parameter to a dynamically typed value that can be
      // processed by the service.
      Value.Builder parameterValueBuilder = Value.newBuilder();
      JsonFormat.parser().merge(parameters, parameterValueBuilder);
      Value parameterValue = parameterValueBuilder.build();

      PredictResponse predictResponse =
          predictionServiceClient.predict(endpointName, instances, parameterValue);
      System.out.println("Predict Response");
      System.out.println(predictResponse);
    }
  }
}
// [END aiplatform_sdk_summarization]
