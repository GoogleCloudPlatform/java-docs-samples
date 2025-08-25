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

package genai.tools;

// [START googlegenaisdk_tools_code_exec_with_txt_local_img]

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.Part;
import com.google.genai.types.Tool;
import com.google.genai.types.ToolCodeExecution;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ToolsCodeExecWithTextLocalImage {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "gemini-2.5-flash";
    String localImagePath = "your-local-image.png";
    generateContent(modelId, localImagePath);
  }

  // Generates text using the Code Execution tool with text and image input
  public static String generateContent(String modelId, String localImagePath) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (Client client =
        Client.builder()
            .location("global")
            .vertexAI(true)
            .httpOptions(HttpOptions.builder().apiVersion("v1").build())
            .build()) {

      String prompt =
          "Run a simulation of the Monty Hall Problem with 1,000 trials.\n"
              + "Here's how this works as a reminder. In the Monty Hall Problem, you're on a game"
              + " show with three doors. Behind one is a car, and behind the others are goats. You"
              + " pick a door. The host, who knows what's behind the doors, opens a different door"
              + " to reveal a goat. Should you switch to the remaining unopened door?\n"
              + " The answer has always been a little difficult for me to understand when people"
              + " solve it with math - so please run a simulation with Python to show me what the"
              + " best strategy is.\n"
              + " Thank you!";

      // Read content from the local image
      byte[] imageData = Files.readAllBytes(Paths.get(localImagePath));

      // Create a GenerateContentConfig and set codeExecution tool
      GenerateContentConfig contentConfig =
          GenerateContentConfig.builder()
              .tools(Tool.builder().codeExecution(ToolCodeExecution.builder().build()).build())
              .temperature(0.0F)
              .build();

      GenerateContentResponse response =
          client.models.generateContent(
              modelId,
              Content.fromParts(Part.fromBytes(imageData, "image/png"), Part.fromText(prompt)),
              contentConfig);

      System.out.println("Code: \n" + response.executableCode());
      System.out.println("Outcome: \n" + response.codeExecutionResult());
      // Example response
      // Code:
      // import random
      //
      // def run_monty_hall_trial():
      //    doors = [0, 1, 2] # Represent doors as indices 0, 1, 2
      //
      //    # 1. Randomly place the car behind one door
      //    car_door = random.choice(doors)
      //    ...
      //
      // Outcome:
      // Number of trials: 1000
      // Stick strategy wins: 327 (32.70%)
      // Switch strategy wins: 673 (67.30%)
      return response.executableCode();
    }
  }
}
// [END googlegenaisdk_tools_code_exec_with_txt_local_img]