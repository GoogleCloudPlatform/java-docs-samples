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

package genai.live;

// [START googlegenaisdk_live_structured_output_with_txt]

import com.google.auth.oauth2.GoogleCredentials;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.StructuredChatCompletion;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Optional;

public class LiveStructuredOutputWithTxt {

  public static class CalendarEvent {
    public String name;
    public String date;
    public List<String> participants;

    @Override
    public String toString() {
      return "name=" + name + " date=" + date + " participants=" + participants;
    }
  }

  public static void main(String[] args) throws GeneralSecurityException, IOException {
    // TODO(developer): Replace these variables before running the sample
    String projectId = "your-project-id";
    String location = "us-central1";
    // If you are calling a Gemini model, set the endpointId variable to use openapi.
    // If you are calling a self-deployed model from Model Garden, set the endpointId variable
    // and set the client's base URL to use your endpoint.
    String endpointId = "openapi";
    generateContent(projectId, location, endpointId);
  }

  // Shows how to use structured output using the OpenAI client.
  public static Optional<CalendarEvent> generateContent(
      String projectId, String location, String endpointId)
      throws GeneralSecurityException, IOException {

    // Programmatically get an access token for authentication.
    GoogleCredentials credential =
        GoogleCredentials.getApplicationDefault()
            .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));

    OpenAIClient client =
        OpenAIOkHttpClient.builder()
            .baseUrl(
                String.format(
                    "https://%s-aiplatform.googleapis.com/v1/projects/%s/locations/%s/endpoints/%s",
                    location, projectId, location, endpointId))
            .apiKey(credential.refreshAccessToken().getTokenValue())
            .build();

    // Creates and sends the chat completion request.
    StructuredChatCompletion<CalendarEvent> chatCompletion =
        client
            .chat()
            .completions()
            .create(
                ChatCompletionCreateParams.builder()
                    .model("google/gemini-2.5-flash")
                    .addSystemMessage("Extract the event information.")
                    .addUserMessage("Alice and Bob are going to a science fair on Friday.")
                    .responseFormat(CalendarEvent.class)
                    .build());

    Optional<CalendarEvent> calendarEvent =
        chatCompletion.choices().stream().findFirst().flatMap(choice -> choice.message().content());

    calendarEvent.ifPresent(System.out::println);
    // System message: Extract the event information.
    // User message: Alice and Bob are going to a science fair on Friday.
    // output message: name=science fair date=Friday participants=[Alice, Bob]
    return calendarEvent;
  }
}
// [END googlegenaisdk_live_structured_output_with_txt]
