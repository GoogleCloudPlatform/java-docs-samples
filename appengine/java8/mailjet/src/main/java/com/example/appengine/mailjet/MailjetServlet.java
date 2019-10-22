/*
 * Copyright 2015 Google Inc.
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

// [START mailjet_imports]

package com.example.appengine.mailjet;

import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import com.mailjet.client.resource.Email;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

// [END mailjet_imports]

// [START app]
@SuppressWarnings("serial")
public class MailjetServlet extends HttpServlet {

  private static final String MAILJET_API_KEY = System.getenv("MAILJET_API_KEY");
  private static final String MAILJET_SECRET_KEY = System.getenv("MAILJET_SECRET_KEY");
  private MailjetClient client = new MailjetClient(MAILJET_API_KEY, MAILJET_SECRET_KEY);

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    String recipient = req.getParameter("to");
    String sender = req.getParameter("from");

    MailjetRequest email =
        new MailjetRequest(Email.resource)
            .property(Email.FROMEMAIL, sender)
            .property(Email.FROMNAME, "pandora")
            .property(Email.SUBJECT, "Your email flight plan!")
            .property(
                Email.TEXTPART,
                "Dear passenger, welcome to Mailjet! May the delivery force be with you!")
            .property(
                Email.HTMLPART,
                "<h3>Dear passenger, welcome to Mailjet!</h3><br/>May the delivery force be with "
                    + "you!")
            .property(
                Email.RECIPIENTS, new JSONArray().put(new JSONObject().put("Email", recipient)));

    try {
      // trigger the API call
      MailjetResponse response = client.post(email);
      // Read the response data and status
      resp.getWriter().print(response.getStatus());
      resp.getWriter().print(response.getData());
    } catch (MailjetException e) {
      throw new ServletException("Mailjet Exception", e);
    } catch (MailjetSocketTimeoutException e) {
      throw new ServletException("Mailjet socket timed out", e);
    }
  }
}
// [END app]
