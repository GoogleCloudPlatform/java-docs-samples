/**
 * Copyright 2016 Google Inc. All Rights Reserved.
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

package com.example.appengine.mail;

// [START mail_handler_servlet]
import java.io.IOException;
import java.util.logging.Logger;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MailHandlerServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(MailHandlerServlet.class.getName());

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    Properties props = new Properties();
    Session session = Session.getDefaultInstance(props, null);
    try {
      MimeMessage message = new MimeMessage(session, req.getInputStream());
      log.info("Received mail message.");
    } catch (MessagingException e) {
      // ...
    }
    // ...
  }
}
// [END mail_handler_servlet]
