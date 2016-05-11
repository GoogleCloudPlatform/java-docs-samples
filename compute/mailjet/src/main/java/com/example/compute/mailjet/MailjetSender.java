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

package com.example.compute.mailjet;

// [START mailjet_imports]
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.resource.Email;
// [END mailjet_imports]

import org.json.JSONArray;
import org.json.JSONObject;

// [START app]
public class MailjetSender{

  public static void main(String[] args) {
    final String mailjetApiKey = "YOUR-MAILJET-API-KEY";
    final String mailjetSecretKey = "YOUR-MAILJET-SECRET-KEY";
    MailjetClient client = new MailjetClient(mailjetApiKey, mailjetSecretKey);

    MailjetSender sender = new MailjetSender();
    sender.sendMailjet(args[0], args[1], client);
  }

  public MailjetResponse sendMailjet(String recipient, String sender, MailjetClient client) {
    MailjetRequest email = new MailjetRequest(Email.resource)
        .property(Email.FROMEMAIL, sender)
        .property(Email.FROMNAME, "pandora")
        .property(Email.SUBJECT, "Your email flight plan!")
        .property(Email.TEXTPART,
            "Dear passenger, welcome to Mailjet! May the delivery force be with you!")
        .property(Email.HTMLPART,
            "<h3>Dear passenger, welcome to Mailjet!</h3><br/>May the delivery force be with you!")
        .property(Email.RECIPIENTS, new JSONArray().put(new JSONObject().put("Email", recipient)));

    try {
      // trigger the API call
      MailjetResponse response = client.post(email);
      // Read the response data and status
      System.out.println(response.getStatus());
      System.out.println(response.getData());
      return response;
    } catch (MailjetException e) {
      System.out.println("Mailjet Exception: " + e);
      return null;
    }
  }
}
// [END app]
