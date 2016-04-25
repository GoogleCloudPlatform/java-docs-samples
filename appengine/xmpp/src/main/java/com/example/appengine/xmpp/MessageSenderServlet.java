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

import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.Message;
import com.google.appengine.api.xmpp.MessageBuilder;
import com.google.appengine.api.xmpp.SendResponse;
import com.google.appengine.api.xmpp.XMPPService;
import com.google.appengine.api.xmpp.XMPPServiceFactory;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// [START example]
@SuppressWarnings("serial")
public class MessageSenderServlet extends HttpServlet {
  private static final Logger log = Logger.getLogger(MessageSenderServlet.class.getName());

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws IOException {

    JID jid = new JID("example@gmail.com");
    String msgBody = "Someone has sent you a gift on Example.com. To view: http://example.com/gifts/";
    Message msg =
        new MessageBuilder()
            .withRecipientJids(jid)
            .withBody(msgBody)
            .build();

    boolean messageSent = false;
    XMPPService xmpp = XMPPServiceFactory.getXMPPService();
    SendResponse status = xmpp.sendMessage(msg);
    messageSent = (status.getStatusMap().get(jid) == SendResponse.Status.SUCCESS);

    log.info("Message sent? " + messageSent);

    if (!messageSent) {
      // Send an email message instead...
    }
  }
}
// [END example]
