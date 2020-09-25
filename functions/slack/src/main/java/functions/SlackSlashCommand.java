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

package functions;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.kgsearch.v1.Kgsearch;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.slack.api.app_backend.SlackSignature;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.GeneralSecurityException;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class SlackSlashCommand implements HttpFunction {

  // [START functions_slack_setup]
  private static final Logger logger = Logger.getLogger(SlackSlashCommand.class.getName());
  private static final String API_KEY = getenv("KG_API_KEY");
  private static final String SLACK_SECRET = getenv("SLACK_SECRET");
  private static final Gson gson = new Gson();

  private final String apiKey;
  private final Kgsearch kgClient;
  private final SlackSignature.Verifier verifier;

  public SlackSlashCommand() throws IOException, GeneralSecurityException {
    this(new SlackSignature.Verifier(new SlackSignature.Generator(SLACK_SECRET)));
  }

  SlackSlashCommand(SlackSignature.Verifier verifier) throws IOException, GeneralSecurityException {
    this(verifier, API_KEY);
  }

  SlackSlashCommand(SlackSignature.Verifier verifier, String apiKey)
      throws IOException, GeneralSecurityException {
    this.verifier = verifier;
    this.apiKey = apiKey;
    this.kgClient = new Kgsearch.Builder(
        GoogleNetHttpTransport.newTrustedTransport(), new JacksonFactory(), null).build();
  }

  // Avoid ungraceful deployment failures due to unset environment variables.
  // If you get this warning you should redeploy with the variable set.
  private static String getenv(String name) {
    String value = System.getenv(name);
    if (value == null) {
      logger.warning("Environment variable " + name + " was not set");
      value = "MISSING";
    }
    return value;
  }
  // [END functions_slack_setup]

  // [START functions_verify_webhook]
  /**
   * Verify that the webhook request came from Slack.
   *
   * @param request Cloud Function request object in {@link HttpRequest} format.
   * @param requestBody Raw body of webhook request to check signature against.
   * @return true if the provided request came from Slack, false otherwise
   */
  boolean isValidSlackWebhook(HttpRequest request, String requestBody) {
    // Check for headers
    Optional<String> maybeTimestamp = request.getFirstHeader("X-Slack-Request-Timestamp");
    Optional<String> maybeSignature = request.getFirstHeader("X-Slack-Signature");
    if (!maybeTimestamp.isPresent() || !maybeSignature.isPresent()) {
      return false;
    }

    Long nowInMs = ZonedDateTime.now().toInstant().toEpochMilli();

    return verifier.isValid(maybeTimestamp.get(), requestBody, maybeSignature.get(), nowInMs);
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
   *
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
      attachmentJson.addProperty("text", "No results match your query...");
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

    JsonArray attachmentList = new JsonArray();
    attachmentList.add(attachmentJson);

    responseJson.add("attachments", attachmentList);

    return gson.toJson(responseJson);
  }
  // [END functions_slack_format]

  // [START functions_slack_request]
  /**
   * Send the user's search query to the Knowledge Graph API.
   *
   * @param query The user's search query.
   * @return The Knowledge graph API results as a {@link JsonObject}.
   * @throws IOException if Knowledge Graph request fails
   */
  JsonObject searchKnowledgeGraph(String query) throws IOException {
    Kgsearch.Entities.Search kgRequest = kgClient.entities().search();
    kgRequest.setQuery(query);
    kgRequest.setKey(apiKey);

    return gson.fromJson(kgRequest.execute().toString(), JsonObject.class);
  }
  // [END functions_slack_request]

  // [START functions_slack_search]
  /**
   * Receive a Slash Command request from Slack.
   *
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

    // Slack sends requests as URL-encoded strings
    //   Java 11 doesn't have a standard library
    //   function for this, so do it manually
    Map<String, String> body = new HashMap<>();
    for (String keyValuePair : bodyString.split("&")) {
      String[] keyAndValue = keyValuePair.split("=");
      if (keyAndValue.length == 2) {
        String key = keyAndValue[0];
        String value = keyAndValue[1];

        body.put(key, value);
      }
    }

    if (body == null || !body.containsKey("text")) {
      response.setStatusCode(HttpURLConnection.HTTP_BAD_REQUEST);
      return;
    }

    if (!isValidSlackWebhook(request, bodyString)) {
      response.setStatusCode(HttpURLConnection.HTTP_UNAUTHORIZED);
      return;
    }

    String query = body.get("text");

    // Call knowledge graph API
    JsonObject kgResponse = searchKnowledgeGraph(query);

    // Format response to Slack
    // See https://api.slack.com/docs/message-formatting
    BufferedWriter writer = response.getWriter();

    writer.write(formatSlackMessage(kgResponse, query));

    response.setContentType("application/json");
  }
  // [END functions_slack_search]
}
