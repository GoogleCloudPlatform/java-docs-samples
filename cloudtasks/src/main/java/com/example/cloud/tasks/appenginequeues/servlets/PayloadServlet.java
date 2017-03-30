/**
 * Copyright (c) 2016 Google Inc.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.cloud.tasks.appenginequeues.servlets;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Basic servlet to store and display a payload.
 *
 * <p>This servlet will store HTTP POST data in Google Cloud Datastore, and return it back when it
 * receives an HTTP GET request. This is meant to serve as a simple endpoint so that you can set
 * payloads via the Cloud Tasks API and verify the new payload has been set with a GET request.
 */
@SuppressWarnings("serial")
@WebServlet(name = "payload", value = "/payload")
public class PayloadServlet extends HttpServlet {

  /** Payload entity type in Cloud Datastore. */
  private static final String PAYLOAD_KIND = "JAVA_PAYLOAD";

  /** Google Cloud Datastore client object. */
  private Datastore datastore;

  /** Extract the POST data from an HTTP request. */
  private String extractPayload(HttpServletRequest req) throws IOException {
    StringBuffer jb = new StringBuffer();

    String line;
    BufferedReader reader = req.getReader();
    while ((line = reader.readLine()) != null) {
      jb.append(line);
    }
    return jb.toString();
  }

  public PayloadServlet() {
    this.datastore = DatastoreOptions.getDefaultInstance().getService();
  }

  /** * GET endpoint returns the payload set by the most recent POST call. */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    PrintWriter out = resp.getWriter();
    KeyFactory keyFactory = datastore.newKeyFactory().setKind(PAYLOAD_KIND);
    Key key = keyFactory.newKey(1);
    Entity payloadEntity = datastore.get(key);
    if (payloadEntity != null) {
      out.println("Payload is " + payloadEntity.getValue("value").get());
    } else {
      out.println("No payload");
    }
  }

  /** POST accepts a payload and stores it in Google Cloud Datastore. */
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    KeyFactory keyFactory = datastore.newKeyFactory().setKind(PAYLOAD_KIND);
    Key key = keyFactory.newKey(1);
    Entity payloadEntity = datastore.get(key);
    String payloadValue = extractPayload(req);

    if (payloadEntity != null) {
      payloadEntity = Entity.newBuilder(payloadEntity).set("value", payloadValue).build();
      datastore.update(payloadEntity);
    } else {
      Entity entity = Entity.newBuilder(key).set("value", payloadValue).build();
      datastore.put(entity);
    }

    PrintWriter out = resp.getWriter();
    out.println("Set payload value to " + payloadValue);
  }
}
