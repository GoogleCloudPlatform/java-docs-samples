/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
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
import java.io.IOException;
import java.util.Collections;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(value = "/pubsub/authenticated-push")
public class PubSubAuthenticatedPush extends PubSubPush {
  private final String pubsubVerificationToken = System.getenv("PUBSUB_VERIFICATION_TOKEN");
  private final MessageRepository messageRepository;
  private final GoogleIdTokenVerifier verifier =
      new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
          .setAudience(Collections.singletonList("example.com"))
          .build();

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {

    if (req.getParameter("token").compareTo(pubsubVerificationToken) != 0) {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    String authorizationHeader = req.getHeader("Authorization");
    if (authorizationHeader == null
        || authorizationHeader.isEmpty()
        || authorizationHeader.split(" ").length != 2) {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    String authorization = authorizationHeader.split(" ")[1];

    try {
      GoogleIdToken idToken = verifier.verify(authorization);
      messageRepository.saveToken(authorization);
      messageRepository.saveClaim(idToken.getPayload().toPrettyString());
      super.doPost(req, resp);
    } catch (Exception e) {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }
  }

  PubSubAuthenticatedPush(MessageRepository messageRepository) {
    super(messageRepository);
    this.messageRepository = messageRepository;
  }

  public PubSubAuthenticatedPush() {
    this(MessageRepositoryImpl.getInstance());
  }
}
