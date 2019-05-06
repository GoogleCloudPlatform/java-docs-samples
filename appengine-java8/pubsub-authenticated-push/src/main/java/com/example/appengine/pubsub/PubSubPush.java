/*
 * Copyright 2019 Google Inc.
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
import com.google.api.client.json.jackson2.JacksonFactory;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;
import org.apache.http.HttpStatus;

@WebServlet(name = "PubSubPush", value = "/_ah/push-handlers/receive_messages")
public class PubSubPush extends HttpServlet {
  private Data data;

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

    if (null == req.getParameter("token")
        || null == req.getHeader("Authorization")
        || !System.getenv("PUBSUB_VERIFICATION_TOKEN").equals(req.getParameter("token"))) {
      resp.sendError(HttpStatus.SC_BAD_REQUEST, "Invalid request");
    }

    String headers[] = req.getHeader("Authorization").split(" ");
    String bearerToken = headers[1];

    if (data == null) {
      data = new Data();
    }
    data.getTokens().add(bearerToken);

    JacksonFactory jacksonFactory = new JacksonFactory();
    GoogleIdToken token = GoogleIdToken.parse(jacksonFactory, bearerToken);
    Claims claims = decodeJWT(bearerToken);
    if (null != claims && claims.size() > 0) {
      String issValue = (String) claims.get("iss");
      if (issValue.equals("https://accounts.google.com")
          || issValue.equals("accounts.google.com")) {
        GoogleIdToken.Payload payload = token.getPayload();
        data.messages.add(payload);
        data.getClaims().add(claims);
        getServletContext().setAttribute("data", data);
      } else {
        resp.sendError(HttpStatus.SC_BAD_REQUEST, "Invalid request");
      }
    }
  }

  public static Claims decodeJWT(String jwt) {
    Claims claims = null;
    try {
      claims =
          Jwts.parser()
              .setSigningKey(DatatypeConverter.parseBase64Binary(readFile("privatekey.pem")))
              .parseClaimsJws(jwt)
              .getBody();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return claims;
  }

  public static String readFile(String path) throws IOException {
    ClassLoader classLoader = new PubSubPush().getClass().getClassLoader();

    File file = new File(classLoader.getResource(path).getFile());

    String privateKeyContent = new String(Files.readAllBytes(file.toPath()));

    privateKeyContent =
        privateKeyContent
            .replaceAll("\\n", "")
            .replace("-----BEGIN RSA PRIVATE KEY-----", "")
            .replace("-----END RSA PRIVATE KEY-----", "");

    return privateKeyContent;
  }
}
