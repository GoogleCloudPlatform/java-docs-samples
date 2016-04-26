/*
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

package com.example.appengine.xmpp;

import com.google.appengine.api.xmpp.Subscription;
import com.google.appengine.api.xmpp.XMPPService;
import com.google.appengine.api.xmpp.XMPPServiceFactory;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// [START example]
@SuppressWarnings("serial")
public class SubscriptionServlet extends HttpServlet {
  private static final Logger log = Logger.getLogger(SubscriptionServlet.class.getName());

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res)
        throws IOException {
    XMPPService xmppService = XMPPServiceFactory.getXMPPService();
    Subscription sub = xmppService.parseSubscription(req);

    // Split the bare XMPP address (e.g., user@gmail.com)
    // from the resource (e.g., gmail.CD6EBC4A)
    String from = sub.getFromJid().getId().split("/")[0];

    log.info("Received subscription event from: " + from);
  }
}
// [END example]
