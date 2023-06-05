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

// [START aiplatform_sdk_sentiment_analysis]

import com.google.cloud.aiplatform.v1beta1.EndpointName;
import com.google.cloud.aiplatform.v1beta1.PredictResponse;
import com.google.cloud.aiplatform.v1beta1.PredictionServiceClient;
import com.google.cloud.aiplatform.v1beta1.PredictionServiceSettings;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Text sentiment analysis with a Large Language Model
public class PredictTextSentimentSample {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    // The details of designing text prompts for supported large language models:
    // https://cloud.google.com/vertex-ai/docs/generative-ai/text/text-overview
    String instance =
        "{ \"content\": \"I had to compare two versions of Hamlet for my Shakespeare \n"
            + "class and unfortunately I picked this version. Everything from the acting \n"
            + "(the actors deliver most of their lines directly to the camera) to the camera \n"
            + "shots (all medium or close up shots...no scenery shots and very little back \n"
            + "ground in the shots) were absolutely terrible. I watched this over my spring \n"
            + "break and it is very safe to say that I feel that I was gypped out of 114 \n"
            + "minutes of my vacation. Not recommended by any stretch of the imagination.\n"
            + "Classify the sentiment of the message: negative\n"
            + "\n"
            + "Something surprised me about this movie - it was actually original. It was \n"
            + "not the same old recycled crap that comes out of Hollywood every month. I saw \n"
            + "this movie on video because I did not even know about it before I saw it at my \n"
            + "local video store. If you see this movie available - rent it - you will not \n"
            + "regret it.\n"
            + "Classify the sentiment of the message: positive\n"
            + "\n"
            + "My family has watched Arthur Bach stumble and stammer since the movie first \n"
            + "came out. We have most lines memorized. I watched it two weeks ago and still \n"
            + "get tickled at the simple humor and view-at-life that Dudley Moore portrays. \n"
            + "Liza Minelli did a wonderful job as the side kick - though I'm not her \n"
            + "biggest fan. This movie makes me just enjoy watching movies. My favorite scene \n"
            + "is when Arthur is visiting his fiancée's house. His conversation with the \n"
            + "butler and Susan's father is side-spitting. The line from the butler, \n"
            + "\\\"Would you care to wait in the Library\\\" followed by Arthur's reply, \n"
            + "\\\"Yes I would, the bathroom is out of the question\\\", is my NEWMAIL \n"
            + "notification on my computer.\n"
            + "Classify the sentiment of the message: positive\n"
            + "\n"
            + "This Charles outing is decent but this is a pretty low-key performance. Marlon \n"
            + "Brando stands out. There's a subplot with Mira Sorvino and Donald Sutherland \n"
            + "that forgets to develop and it hurts the film a little. I'm still trying to \n"
            + "figure out why Charlie want to change his name.\n"
            + "Classify the sentiment of the message: negative\n"
            + "\n"
            + "Tweet: The Pixel 7 Pro, is too big to fit in my jeans pocket, so I bought new \n"
            + "jeans.\n"
            + "Classify the sentiment of the message: \"}";
    String parameters =
        "{\n"
            + "  \"temperature\": 0,\n"
            + "  \"maxDecodeSteps\": 5,\n"
            + "  \"topP\": 0,\n"
            + "  \"topK\": 1\n"
            + "}";
    String project = "YOUR_PROJECT_ID";
    String location = "us-central1";
    String publisher = "google";
    String model = "text-bison@001";

    predictTextSentiment(instance, parameters, project, location, publisher, model);
  }

  static void predictTextSentiment(
      String instance,
      String parameters,
      String project,
      String location,
      String publisher,
      String model)
      throws IOException {
    String endpoint = String.format("%s-aiplatform.googleapis.com:443", location);
    PredictionServiceSettings predictionServiceSettings =
        PredictionServiceSettings.newBuilder().setEndpoint(endpoint).build();

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
// [END aiplatform_sdk_sentiment_analysis]
