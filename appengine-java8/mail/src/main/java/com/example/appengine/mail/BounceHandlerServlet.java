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

package com.example.appengine.mail;

// [START bounce_handler_servlet]

import com.google.appengine.api.mail.BounceNotification;
import com.google.appengine.api.mail.BounceNotificationParser;
import java.io.IOException;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BounceHandlerServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(BounceHandlerServlet.class.getName());

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      BounceNotification bounce = BounceNotificationParser.parse(req);
      log.warning("Bounced email notification.");
      // The following data is available in a BounceNotification object
      // bounce.getOriginal().getFrom()
      // bounce.getOriginal().getTo()
      // bounce.getOriginal().getSubject()
      // bounce.getOriginal().getText()
      // bounce.getNotification().getFrom()
      // bounce.getNotification().getTo()
      // bounce.getNotification().getSubject()
      // bounce.getNotification().getText()
      // ...
    } catch (MessagingException e) {
      // ...
    }
  }
}
// [END bounce_handler_servlet]
