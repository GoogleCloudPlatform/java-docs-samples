/*
 * Copyright 2016 Google Inc.
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

package com.example.compute.sendgrid;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import java.io.IOException;

// [START example]
public class SendEmailServlet {
  static final String SENDGRID_API_KEY = "YOUR-SENDGRID-API-KEY";
  static final String SENDGRID_SENDER = "YOUR-SENDGRID-FROM-EMAIL";
  static final String TO_EMAIL = "DESTINATION-EMAIL";

  public static void main(String[] args) throws IOException {


    // Set content for request.
    Email to = new Email(TO_EMAIL);
    Email from = new Email(SENDGRID_SENDER);
    String subject = "This is a test email";
    Content content = new Content("text/plain", "Example text body.");
    Mail mail = new Mail(from, subject, to, content);

    // Instantiates SendGrid client.
    SendGrid sendgrid = new SendGrid(SENDGRID_API_KEY);

    // Instantiate SendGrid request.
    Request request = new Request();

    // Set request configuration.
    request.setMethod(Method.POST);
    request.setEndpoint("mail/send");
    request.setBody(mail.build());

    // Use the client to send the API request.
    Response response = sendgrid.api(request);

    if (response.getStatusCode() != 202) {
      System.out.print(String.format("An error occurred: %s", response.getStatusCode()));
      return;
    }

    System.out.print("Email sent.");
  }

}
// [END example]
