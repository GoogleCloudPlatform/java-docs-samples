/*
 * Copyright 2020 Google LLC
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

package com.example.functions.slack;

import com.github.seratch.jslack.app_backend.SlackSignature;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.kgsearch.v1.Kgsearch;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SlackSlashCommand implements HttpFunction {

  // [START functions_slack_setup]
  private static final String API_KEY = System.getenv("KG_API_KEY");
  private static final String SLACK_SECRET = System.getenv("SLACK_SECRET");

  private Kgsearch kgClient;
  private SlackSignature.Verifier verifier;
  private Gson gson = new Gson();

  public SlackSlashCommand() throws IOException, GeneralSecurityException {
    kgClient = new Kgsearch.Builder(
        GoogleNetHttpTransport.newTrustedTransport(), new JacksonFactory(), null).build();

    verifier = new SlackSignature.Verifier(new SlackSignature.Generator(SLACK_SECRET));
  }
  // [END functions_slack_setup]

  // [START functions_verify_webhook]
  /**
   * Verify that the webhook request came from Slack.
   * @param request Cloud Function request object in {@link HttpRequest} format.
   * @param requestBody Raw body of webhook request to check signature against.
   * @return true if the provided request came from Slack, false otherwise
   */
  boolean isValidSlackWebhook(HttpRequest request, String requestBody) {

    // Check for headers
    HashMap<String, List<String>> headers = new HashMap(request.getHeaders());
    if (!headers.containsKey("X-Slack-Request-Timestamp")
        || !headers.containsKey("X-Slack-Signature")) {
      return false;
    }
    return verifier.isValid(
        headers.get("X-Slack-Request-Timestamp").get(0),
        requestBody,
        headers.get("X-Slack-Signature").get(0),
        1L);
  }
  // [END functions_verify_webhook]

  // [START functions_slack_format]
  /**
   * Helper method to copy properties between {@link JsonObject}s
   */
  void addPropertyIfPresent(
      JsonObject target, String targetName, JsonObject source, String sourceName) {
    if (source.has(sourceName)) {
      target.addProperty(targetName, source.get(sourceName).getAsString());
    }
  }

  /**
   * Format the Knowledge Graph API response into a richly formatted Slack message.
   * @param kgResponse The response from the Knowledge Graph API as a {@link JsonObject}.
   * @param query The user's search query.
   * @return The formatted Slack message as a JSON string.
   */
  String formatSlackMessage(JsonObject kgResponse, String query) {
    JsonObject attachmentJson = new JsonObject();

    JsonObject responseJson = new JsonObject();
    responseJson.addProperty("response_type", "in_channel");
    responseJson.addProperty("text", String.format("Query: %s", query));

    JsonArray entityList = kgResponse.getAsJsonArray("itemListElement");

    // Extract the first entity from the result list, if any
    if (entityList.size() == 0) {
      attachmentJson.addProperty("text","No results match your query...");
      responseJson.add("attachments", attachmentJson);

      return gson.toJson(responseJson);
    }

    JsonObject entity = entityList.get(0).getAsJsonObject().getAsJsonObject("result");

    // Construct Knowledge Graph response attachment
    String title = entity.get("name").getAsString();
    if (entity.has("description")) {
      title = String.format("%s: %s", title, entity.get("description").getAsString());
    }
    attachmentJson.addProperty("title", title);

    if (entity.has("detailedDescription")) {
      JsonObject detailedDescJson = entity.getAsJsonObject("detailedDescription");
      addPropertyIfPresent(attachmentJson, "title_link", detailedDescJson, "url");
      addPropertyIfPresent(attachmentJson, "text", detailedDescJson, "articleBody");
    }

    if (entity.has("image")) {
      JsonObject imageJson = entity.getAsJsonObject("image");
      addPropertyIfPresent(attachmentJson, "image_url", imageJson, "contentUrl");
    }

    responseJson.add("attachments", attachmentJson);

    return gson.toJson(responseJson);
  }
  // [END functions_slack_format]

  // [START functions_slack_request]
  /**
   * Send the user's search query to the Knowledge Graph API.
   * @param query The user's search query.
   * @return The Knowledge graph API results as a {@link JsonObject}.
   * @throws IOException if Knowledge Graph request fails
   */
  JsonObject searchKnowledgeGraph(String query) throws IOException {
    Kgsearch.Entities.Search kgRequest = kgClient.entities().search();
    kgRequest.setQuery(query);
    kgRequest.setKey(API_KEY);

    return gson.fromJson(kgRequest.execute().toString(), JsonObject.class);
  }
  // [END functions_slack_request]

  // [START functions_slack_search]
  /**
   * Receive a Slash Command request from Slack.
   * @param request Cloud Function request object.
   * @param response Cloud Function response object.
   * @throws IOException if Knowledge Graph request fails
   */
  @Override
  public void service(HttpRequest request, HttpResponse response) throws IOException {

    // Validate request
    if (!"POST".equals(request.getMethod())) {
      response.setStatusCode(HttpURLConnection.HTTP_BAD_METHOD);
      return;
    }

    // reader can only be read once per request, so we preserve its contents
    String bodyString = request.getReader().lines().collect(Collectors.joining());
    JsonObject body = (new Gson()).fromJson(bodyString, JsonObject.class);

    if (body == null || !body.has("text")) {
      response.setStatusCode(HttpURLConnection.HTTP_BAD_REQUEST);
      return;
    }

    if (!isValidSlackWebhook(request, bodyString)) {
      response.setStatusCode(HttpURLConnection.HTTP_UNAUTHORIZED);
      return;
    }

    String query = body.get("text").getAsString();

    // Call knowledge graph API
    JsonObject kgResponse = searchKnowledgeGraph(query);

    // Format response to Slack
    // See https://api.slack.com/docs/message-formatting
    BufferedWriter writer = response.getWriter();
    writer.write(formatSlackMessage(kgResponse, query));
  }
  // [END functions_slack_search]
}
