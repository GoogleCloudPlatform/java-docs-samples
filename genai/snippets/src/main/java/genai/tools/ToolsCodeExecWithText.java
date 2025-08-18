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

// [START googlegenaisdk_tools_code_exec_with_txt]

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.Tool;
import com.google.genai.types.ToolCodeExecution;

public class ToolsCodeExecWithText {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "gemini-2.5-flash";
    generateContent(modelId);
  }

  // Generates text using the Code Execution tool
  public static String generateContent(String modelId) {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (Client client =
        Client.builder()
            .location("global")
            .vertexAI(true)
            .httpOptions(HttpOptions.builder().apiVersion("v1").build())
            .build()) {

      // Create a config and set codeExecution tool
      GenerateContentConfig contentConfig =
          GenerateContentConfig.builder()
              .tools(Tool.builder().codeExecution(ToolCodeExecution.builder().build()).build())
              .temperature(0.0F)
              .build();

      GenerateContentResponse response =
          client.models.generateContent(
              modelId,
              "Calculate 20th fibonacci number. Then find the nearest palindrome to it.",
              contentConfig);

      System.out.println("Code: \n" + response.executableCode());
      System.out.println("Outcome: \n" + response.codeExecutionResult());
      // Example response
      // Code:
      // def fibonacci(n):
      //    if n <= 0:
      //        return 0
      //    elif n == 1:
      //        return 1
      //    else:
      //        a, b = 1, 1
      //        for _ in range(2, n):
      //            a, b = b, a + b
      //        return b
      //
      // fib_20 = fibonacci(20)
      // print(f'{fib_20=}')
      //
      // Outcome:
      // fib_20=6765
      return response.executableCode();
    }
  }
}
// [END googlegenaisdk_tools_code_exec_with_txt]