/*
 * Copyright 2017 Google LLC
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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.Base64;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// [START gae_java21_standard_pubsub_push]
@WebServlet(value = "/pubsub/push")
public class PubSubPush extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    String pubsubVerificationToken = System.getenv("PUBSUB_VERIFICATION_TOKEN");
    // Do not process message if request token does not match pubsubVerificationToken
    if (req.getParameter("token").compareTo(pubsubVerificationToken) != 0) {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    // parse message object from "message" field in the request body json
    // decode message data from base64
    Message message = getMessage(req);
    try {
      messageRepository.save(message);
      // 200, 201, 204, 102 status codes are interpreted as success by the Pub/Sub system
      resp.setStatus(102);
    } catch (Exception e) {
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  private Message getMessage(HttpServletRequest request) throws IOException {
    String requestBody = request.getReader().lines().collect(Collectors.joining("\n"));
    JsonElement jsonRoot = JsonParser.parseString(requestBody).getAsJsonObject();
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

  private final Gson gson = new Gson();
  private MessageRepository messageRepository;

  PubSubPush(MessageRepository messageRepository) {
    this.messageRepository = messageRepository;
  }

  public PubSubPush() {
    this.messageRepository = MessageRepositoryImpl.getInstance();
  }
}
// [END gae_java21_standard_pubsub_push]
