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

package com.example.guestbook;

// [START gae_java11_form_data]
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet to sace the form data to your Firestore instance. */
@SuppressWarnings("serial")
@WebServlet(name = "SignGuestbookServlet", value = "/sign")
public class SignGuestbookServlet extends HttpServlet {

  // Process the HTTP POST of the form
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    Greeting greeting;

    String guestbookName = req.getParameter("guestbookName");
    String name = req.getParameter("name");
    String content = req.getParameter("content");

    greeting = new Greeting(guestbookName, content, name);
    greeting.save();

    resp.sendRedirect("/index.jsp?guestbookName=" + guestbookName);
  }
}
// [END gae_java11_form_data]
