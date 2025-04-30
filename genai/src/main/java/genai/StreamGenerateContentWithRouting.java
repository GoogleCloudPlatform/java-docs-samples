/*
 * Copyright 2025 Google LLC
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

 package genai;

 // [START genai_stream_generate_content_with_routing]
 import com.google.genai.Client;
 import com.google.genai.types.GenerateContentResponse;
 import com.google.genai.types.GenerateContentConfig;
 import com.google.genai.types.ModelSelectionConfig;
 import com.google.genai.types.HttpOptions;
 import com.google.genai.ResponseStream;

 
public class StreamGenerateContentWithRouting {
 
   public static void main(String[] args) throws Exception {


     String modelName = "model-optimizer-exp-04-09";

     HttpOptions httpOptions = HttpOptions.builder().apiVersion("v1beta1").build();

     Client client = Client.builder().httpOptions(httpOptions).vertexAI(true).build();

     ModelSelectionConfig modelSelectionConfig = ModelSelectionConfig.builder().featureSelectionPreference("BALANCED").build();
     
     GenerateContentConfig generateContentConfig = GenerateContentConfig.builder().modelSelectionConfig(modelSelectionConfig).build();

     ResponseStream<GenerateContentResponse> responseStream =
        client.models.generateContentStream(modelName, "Why do we have 365 days in a year?", generateContentConfig);
     
    System.out.println("Streaming response: ");
        for (GenerateContentResponse res : responseStream) {
          System.out.print(res.text());
        }

    responseStream.close();
   }
 }
 // [END genai_stream_generate_content_with_routing]