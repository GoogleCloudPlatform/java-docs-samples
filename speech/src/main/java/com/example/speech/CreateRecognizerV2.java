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

package com.example.speech;

// [START speech_create_recognizer_v2]
import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.speech.v2.CreateRecognizerRequest;
import com.google.cloud.speech.v2.OperationMetadata;
import com.google.cloud.speech.v2.Recognizer;
import com.google.cloud.speech.v2.SpeechClient;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class CreateRecognizerV2 {
  public static void main(String[] args) throws IOException, InterruptedException,
      ExecutionException {
    String projectId = "my-project-id";
    String recognizerId = "my-recognizer";
    
    createRecognizerV2(projectId, recognizerId);
  }

  public static void createRecognizerV2(String projectId, String recognizerId) throws IOException,
      InterruptedException, ExecutionException {

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (SpeechClient speechClient = SpeechClient.create()) {
      String parent = String.format("projects/%s/locations/global", projectId);

      Recognizer recognizer = Recognizer.newBuilder()
          .setModel("latest_long")
          .addLanguageCodes("en-US")
          .build();

      CreateRecognizerRequest createRecognizerRequest = CreateRecognizerRequest.newBuilder()
          .setParent(parent)
          .setRecognizerId(recognizerId)
          .setRecognizer(recognizer)
          .build();

      OperationFuture<Recognizer, OperationMetadata> operationFuture =
          speechClient.createRecognizerAsync(createRecognizerRequest);
      recognizer = operationFuture.get();

      System.out.printf("Recognizer created: %s", recognizer.getName());
    }
  }
}
// [END speech_create_recognizer_v2]
