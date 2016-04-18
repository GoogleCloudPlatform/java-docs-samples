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

package com.example.compute.sendgrid;

import com.sendgrid.SendGrid;
import com.sendgrid.SendGridException;

// [START example]
public class SendEmailServlet {
  final static String SENDGRID_API_KEY = "YOUR-SENDGRID-API-KEY";
  final static String SENDGRID_SENDER = "YOUR-SENDGRID-FROM-EMAIL";
  final static String TO_EMAIL = "DESTINATION-EMAIL";

  public static void main(String[] args) throws SendGridException {

    SendGrid sendgrid = new SendGrid(SENDGRID_API_KEY);
    SendGrid.Email email = new SendGrid.Email();
    email.addTo(TO_EMAIL);
    email.setFrom(SENDGRID_SENDER);
    email.setSubject("This is a test email");
    email.setText("Example text body.");

    SendGrid.Response response = sendgrid.send(email);
    if (response.getCode() != 200) {
      System.out.print(String.format("An error occured: %s", response.getMessage()));
      return;
    }
    System.out.print("Email sent.");
  }

}
// [END example]
