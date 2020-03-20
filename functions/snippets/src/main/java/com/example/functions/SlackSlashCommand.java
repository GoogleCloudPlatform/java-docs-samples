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

package com.example.functions;

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

  private Kgsearch kgClient;
  private static final String API_KEY = System.getenv("KG_API_KEY");
  private static final String SLACK_SECRET = System.getenv("SLACK_SECRET");
  private static final Logger LOGGER = Logger.getLogger(SlackSlashCommand.class.getName());
  private SlackSignature.Verifier verifier;
  private Gson gson = new Gson();

  public SlackSlashCommand() throws IOException, GeneralSecurityException {
    kgClient = new Kgsearch.Builder(
        GoogleNetHttpTransport.newTrustedTransport(), new JacksonFactory(), null).build();

    verifier = new SlackSignature.Verifier(new SlackSignature.Generator(SLACK_SECRET));
  }

  boolean isValidSlackWebhook(HttpRequest request, String requestBody) throws IOException {

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

  void addPropertyIfPresent(
      JsonObject target, String targetName, JsonObject source, String sourceName) {
    if (source.has(sourceName)) {
      target.addProperty(targetName, source.get(sourceName).getAsString());
    }
  }

  String formatSlackMessage(JsonObject kgResponse, String query) {
    JsonObject attachmentJson = new JsonObject();
    JsonArray attachments = new JsonArray();

    JsonObject responseJson = new JsonObject();
    responseJson.addProperty("response_type", "in_channel");
    responseJson.addProperty("text", String.format("Query: %s", query));

    JsonArray entityList = kgResponse.getAsJsonArray("itemListElement");

    // Extract the first entity from the result list, if any
    if (entityList.size() == 0) {
      attachmentJson.addProperty("text","No results match your query...");

      attachments.add(attachmentJson);
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

    // Construct top level response
    attachments.add(attachmentJson);
    responseJson.add("attachments", attachmentJson);

    return gson.toJson(responseJson);
  }

  JsonObject searchKnowledgeGraph(String query) throws IOException {
    Kgsearch.Entities.Search kgRequest = kgClient.entities().search();

    LOGGER.info("KG KEY");
    LOGGER.info(API_KEY);

    kgRequest.setQuery(query);
    kgRequest.setKey(API_KEY);

    return gson.fromJson(kgRequest.execute().toString(), JsonObject.class);
  }

  @Override
  public void service(HttpRequest request, HttpResponse response) throws IOException {

    // Validate request
    if (request.getMethod() != "POST") {
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
    BufferedWriter writer = response.getWriter();
    writer.write(formatSlackMessage(kgResponse, query));
  }
}
