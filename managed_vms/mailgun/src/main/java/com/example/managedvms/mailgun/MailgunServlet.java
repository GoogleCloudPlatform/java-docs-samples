/**
 * Copyright 2015 Google Inc. All Rights Reserved.
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

package com.example.managedvms.mailgun;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;

import java.io.File;
import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

// [START example]
@SuppressWarnings("serial")
@WebServlet(name = "mailgun", value = "/send/email")
public class MailgunServlet extends HttpServlet {

  private static final String MAILGUN_DOMAIN_NAME = System.getenv("MAILGUN_DOMAIN_NAME");
  private static final String MAILGUN_API_KEY = System.getenv("MAILGUN_API_KEY");

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String type = req.getParameter("submit");
    String recipient = req.getParameter("to");
    ClientResponse clientResponse;
    if (type.equals("Send simple email")) {
      clientResponse = sendSimpleMessage(recipient);
    } else {
      clientResponse = sendComplexMessage(recipient);
    }
    if (clientResponse.getStatus() == 200) {
      resp.getWriter().print("Email sent.");
    }
  }

  // [START simple]
  private ClientResponse sendSimpleMessage(String recipient) {
    Client client = Client.create();
    client.addFilter(new HTTPBasicAuthFilter("api", MAILGUN_API_KEY));
    WebResource webResource = client.resource("https://api.mailgun.net/v3/" + MAILGUN_DOMAIN_NAME
        + "/messages");
    MultivaluedMapImpl formData = new MultivaluedMapImpl();
    formData.add("from", "Mailgun User <mailgun@" + MAILGUN_DOMAIN_NAME + ">");
    formData.add("to", recipient);
    formData.add("subject", "Simple Mailgun Example");
    formData.add("text", "Plaintext content");
    return webResource.type(MediaType.APPLICATION_FORM_URLENCODED).post(ClientResponse.class,
        formData);
  }
  // [END simple]

  // [START complex]
  private ClientResponse sendComplexMessage(String recipient) {
    Client client = Client.create();
    client.addFilter(new HTTPBasicAuthFilter("api", MAILGUN_API_KEY));
    WebResource webResource = client.resource("https://api.mailgun.net/v3/" + MAILGUN_DOMAIN_NAME
        + "/messages");
    FormDataMultiPart formData = new FormDataMultiPart();
    formData.field("from", "Mailgun User <mailgun@" + MAILGUN_DOMAIN_NAME + ">");
    formData.field("to", recipient);
    formData.field("subject", "Complex Mailgun Example");
    formData.field("html", "<html>HTML <strong>content</strong></html>");
    ClassLoader classLoader = getClass().getClassLoader();
    File txtFile = new File(classLoader.getResource("example-attachment.txt").getFile());
    formData.bodyPart(new FileDataBodyPart("attachment", txtFile, MediaType.TEXT_PLAIN_TYPE));
    return webResource.type(MediaType.MULTIPART_FORM_DATA_TYPE)
        .post(ClientResponse.class, formData);
  }
  // [END complex]
}
// [END example]
