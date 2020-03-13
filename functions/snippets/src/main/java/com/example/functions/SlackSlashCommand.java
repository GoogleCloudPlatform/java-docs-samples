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
import com.google.api.client.util.ArrayMap;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SlackSlashCommand implements HttpFunction {

  private Kgsearch kgClient;
  private static String API_KEY;
  private static String SLACK_SECRET;
  private static final Logger LOGGER = Logger.getLogger(HelloHttp.class.getName());
  private SlackSignature.Verifier verifier;
  private Gson gson = new Gson();

  public SlackSlashCommand() throws IOException, GeneralSecurityException {
    kgClient = new Kgsearch.Builder(
        GoogleNetHttpTransport.newTrustedTransport(), new JacksonFactory(), null).build();

    // Read + parse config file
    Path configPath = Path.of(System.getProperty("user.dir"), "config.json");
    JsonObject configJson = (new Gson()).fromJson(Files.readString(configPath), JsonObject.class);

    SLACK_SECRET = configJson.get("SLACK_SECRET").getAsString();
    API_KEY = configJson.get("KG_API_KEY").getAsString();

    verifier = new SlackSignature.Verifier(new SlackSignature.Generator(SLACK_SECRET));
  }

  private boolean isValidSlackWebhook(HttpRequest request, String requestBody) throws IOException {

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

  private void addPropertyIfPresent(
      JsonObject target, String targetName, ArrayMap source, String sourceName) {
    if (source.containsKey(sourceName)) {
      target.addProperty(targetName, source.get(sourceName).toString());
    }
  }

  private String formatSlackMessage(List<Object> kgResults, String query) {
    JsonObject attachmentJson = new JsonObject();
    JsonArray attachments = new JsonArray();

    JsonObject responseJson = new JsonObject();
    responseJson.addProperty("response_type", "in_channel");
    responseJson.addProperty("text", String.format("Query: %s", query));

    // Extract the first entity from the result list, if any
    if (kgResults.size() == 0) {
      attachmentJson.addProperty("text","No results match your query...");

      attachments.add(attachmentJson);
      responseJson.add("attachments", attachmentJson);

      return gson.toJson(responseJson);
    }

    ArrayMap entity = (ArrayMap) ((ArrayMap) kgResults.get(0)).get("result");

    // Construct Knowledge Graph response attachment
    String title = entity.get("name").toString();
    if (entity.containsKey("description")) {
      title = String.format("%s: %s", title, entity.get("description").toString());
    }
    attachmentJson.addProperty("title", title);

    if (entity.containsKey("detailedDescription")) {
      ArrayMap detailedDescJson = (ArrayMap) entity.get("detailedDescription");
      addPropertyIfPresent(attachmentJson, "title_link", detailedDescJson, "url");
      addPropertyIfPresent(attachmentJson, "text", detailedDescJson, "articleBody");
    }

    if (entity.containsKey("image")) {
      ArrayMap imageJson = (ArrayMap) entity.get("image");
      addPropertyIfPresent(attachmentJson, "image_url", imageJson, "contentUrl");
    }

    // Construct top level response
    attachments.add(attachmentJson);
    responseJson.add("attachments", attachmentJson);

    return gson.toJson(responseJson);
  }

  private List<Object> searchKnowledgeGraph(String query) throws IOException {
    Kgsearch.Entities.Search kgRequest = kgClient.entities().search();
    kgRequest.setQuery(query);
    kgRequest.setKey(API_KEY);

    return kgRequest.execute().getItemListElement();
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
    List<Object> kgResults = searchKnowledgeGraph(query);

    // Format response to Slack
    BufferedWriter writer = response.getWriter();
    writer.write(formatSlackMessage(kgResults, query));
  }
}
