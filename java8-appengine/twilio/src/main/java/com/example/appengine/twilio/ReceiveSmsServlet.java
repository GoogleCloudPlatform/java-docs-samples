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

package com.example.appengine.twilio;

import com.twilio.sdk.verbs.Message;
import com.twilio.sdk.verbs.TwiMLException;
import com.twilio.sdk.verbs.TwiMLResponse;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// [START example]
@SuppressWarnings("serial")
public class ReceiveSmsServlet extends HttpServlet {

  @Override
  public void service(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    String fromNumber = request.getParameter("From");
    String body = request.getParameter("Body");
    String message = String.format("Hello, %s, you said %s", fromNumber, body);

    TwiMLResponse twiml = new TwiMLResponse();
    Message sms = new Message(message);
    try {
      twiml.append(sms);
    } catch (TwiMLException e) {
      throw new ServletException("Twilio error", e);
    }

    response.setContentType("application/xml");
    response.getWriter().print(twiml.toXML());
  }
}
// [END example]
