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

package com.example.managedvms.sendgrid;

import com.sendgrid.SendGrid;
import com.sendgrid.SendGridException;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// [START example]
@SuppressWarnings("serial")
@WebServlet(name = "sendemail", value = "/send/email")
public class SendEmailServlet extends HttpServlet {

  @Override
  public void service(HttpServletRequest req, HttpServletResponse resp) throws IOException,
      ServletException {
    final String SENDGRID_API_KEY = System.getenv("SENDGRID_API_KEY");
    final String SENDGRID_SENDER = System.getenv("SENDGRID_SENDER");
    final String TO_EMAIL = (String) req.getParameter("to");
    if (TO_EMAIL == null) {
      resp.getWriter().print("Please provide an email address in the \"to\" query string"
          + " parameter.");
      return;
    }

    SendGrid sendgrid = new SendGrid(SENDGRID_API_KEY);
    SendGrid.Email email = new SendGrid.Email();
    email.addTo(TO_EMAIL);
    email.setFrom(SENDGRID_SENDER);
    email.setSubject("This is a test email");
    email.setText("Example text body.");

    try {
      SendGrid.Response response = sendgrid.send(email);
      if (response.getCode() != 200) {
        resp.getWriter().print(String.format("An error occured: %s", response.getMessage()));
        return;
      }
      resp.getWriter().print("Email sent.");
    }
    catch (SendGridException e) {
      throw new ServletException("SendGrid error", e);
    }
  }
}
// [END example]
