/*
 * Copyright 2018 Google LLC
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

package com.example.appengine.sendgrid;

// [START gae_sendgrid_import]
import com.sendgrid.Content;
import com.sendgrid.Email;
import com.sendgrid.Mail;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
// [END gae_sendgrid_import]

@SuppressWarnings("serial")
public class SendEmailServlet extends HttpServlet {

  @Override
  public void service(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    // Get parameters from environment variables.
    final String sendgridApiKey = System.getenv("SENDGRID_API_KEY");
    final String sendgridSender = System.getenv("SENDGRID_SENDER");

    // Get email from query string.
    final String toEmail = req.getParameter("to");
    if (toEmail == null) {
      resp.getWriter()
          .print("Please provide an email address in the \"to\" query string parameter.");
      return;
    }

    // [START gae_sendgrid]
    // Set content for request.
    Email to = new Email(toEmail);
    Email from = new Email(sendgridSender);
    String subject = "This is a test email";
    Content content = new Content("text/plain", "Example text body.");
    Mail mail = new Mail(from, subject, to, content);

    // Instantiates SendGrid client.
    SendGrid sendgrid = new SendGrid(sendgridApiKey);

    // Instantiate SendGrid request.
    Request request = new Request();

    try {
      // Set request configuration.
      request.setMethod(Method.POST);
      request.setEndpoint("mail/send");
      request.setBody(mail.build());

      // Use the client to send the API request.
      Response response = sendgrid.api(request);

      if (response.getStatusCode() != 202) {
        resp.getWriter().print(String.format("An error occurred: %s", response.getStatusCode()));
        return;
      }

      // Print response.
      resp.getWriter().print("Email sent.");
    } catch (IOException e) {
      throw new ServletException("SendGrid error", e);
    }
    // [END gae_sendgrid]
  }
}
