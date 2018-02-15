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

import java.util.logging.Logger;
import java.util.regex.Matcher;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// [START example]
public class HandleDiscussionEmail extends MailHandlerBase {

  private static final Logger log = Logger.getLogger(HandleDiscussionEmail.class.getName());

  public HandleDiscussionEmail() {
    super("discuss-(.*)@(.*)");
  }

  @Override
  protected boolean processMessage(
      HttpServletRequest req, HttpServletResponse res) throws ServletException {
    log.info("Received e-mail sent to discuss list.");
    MimeMessage msg = getMessageFromRequest(req);
    Matcher match = getMatcherFromRequest(req);
    // ...
    return true;
  }
}
// [END example]
