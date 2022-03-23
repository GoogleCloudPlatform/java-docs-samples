/*
 * Copyright 2019 Google LLC
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

package com.example.appengine.pubsub;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// [START gae_standard_pubsub_auth_push]
@WebServlet(value = "/pubsub/authenticated-push")
public class PubSubAuthenticatedPush extends HttpServlet {
  private final String pubsubVerificationToken = System.getenv("PUBSUB_VERIFICATION_TOKEN");
  private final MessageRepository messageRepository;
  private final GoogleIdTokenVerifier verifier =
      new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
          /**
           * Please change example.com to match with value you are providing while creating
           * subscription as provided in @see <a
           * href="https://github.com/GoogleCloudPlatform/java-docs-samples/tree/main/appengine-java8/pubsub">README</a>.
           */
          .setAudience(Collections.singletonList("example.com"))
          .build();
  private final Gson gson = new Gson();
  private final JsonParser jsonParser = new JsonParser();

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {

    // Verify that the request originates from the application.
    if (req.getParameter("token").compareTo(pubsubVerificationToken) != 0) {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    // Get the Cloud Pub/Sub-generated JWT in the "Authorization" header.
    String authorizationHeader = req.getHeader("Authorization");
    if (authorizationHeader == null
        || authorizationHeader.isEmpty()
        || authorizationHeader.split(" ").length != 2) {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    String authorization = authorizationHeader.split(" ")[1];

    try {
      // Verify and decode the JWT.
      // Note: For high volume push requests, it would save some network overhead
      // if you verify the tokens offline by decoding them using Google's Public
      // Cert; caching already seen tokens works best when a large volume of
      // messsages have prompted a single push server to handle them, in which
      // case they would all share the same token for a limited time window.
      GoogleIdToken idToken = verifier.verify(authorization);

      GoogleIdToken.Payload payload = idToken.getPayload();
      // IMPORTANT: you should validate claim details not covered by signature
      // and audience verification above, including:
      //   - Ensure that `payload.getEmail()` is equal to the expected service
      //     account set up in the push subscription settings.
      //   - Ensure that `payload.getEmailVerified()` is set to true.

      messageRepository.saveToken(authorization);
      messageRepository.saveClaim(payload.toPrettyString());
      // parse message object from "message" field in the request body json
      // decode message data from base64
      Message message = getMessage(req);
      messageRepository.save(message);
      // 200, 201, 204, 102 status codes are interpreted as success by the Pub/Sub system
      resp.setStatus(102);
      super.doPost(req, resp);
    } catch (Exception e) {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }
  }

  private Message getMessage(HttpServletRequest request) throws IOException {
    String requestBody = request.getReader().lines().collect(Collectors.joining("\n"));
    JsonElement jsonRoot = jsonParser.parse(requestBody);
    String messageStr = jsonRoot.getAsJsonObject().get("message").toString();
    Message message = gson.fromJson(messageStr, Message.class);
    // decode from base64
    String decoded = decode(message.getData());
    message.setData(decoded);
    return message;
  }

  private String decode(String data) {
    return new String(Base64.getDecoder().decode(data));
  }

  PubSubAuthenticatedPush(MessageRepository messageRepository) {
    this.messageRepository = messageRepository;
  }

  public PubSubAuthenticatedPush() {
    this(MessageRepositoryImpl.getInstance());
  }
}
// [END gae_standard_pubsub_auth_push]
