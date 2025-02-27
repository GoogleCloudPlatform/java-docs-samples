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

package com.example.compute.mailjet;

// [START compute_mailjet_imports]

import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.resource.Emailv31;
import org.json.JSONArray;
import org.json.JSONObject;

// [END compute_mailjet_imports]

// [START compute_mailjet_send_email]
public class MailjetSender {

  public static void main(String[] args) throws MailjetException {
    final String mailjetApiKey = "YOUR-MAILJET-API-KEY";
    final String mailjetSecretKey = "YOUR-MAILJET-SECRET-KEY";
    ClientOptions options =
        ClientOptions.builder().apiKey(mailjetApiKey).apiSecretKey(mailjetSecretKey).build();
    MailjetClient client = new MailjetClient(options);

    MailjetSender sender = new MailjetSender();
    sender.sendMailjet(args[0], args[1], client);
  }

  public MailjetResponse sendMailjet(String recipient, String sender, MailjetClient client)
      throws MailjetException {
    MailjetRequest email =
        new MailjetRequest(Emailv31.resource)
            .property(
                Emailv31.MESSAGES,
                new JSONArray()
                    .put(
                        new JSONObject()
                            .put(
                                Emailv31.Message.FROM,
                                new JSONObject().put("Email", sender).put("Name", "pandora"))
                            .put(
                                Emailv31.Message.TO,
                                new JSONArray().put(new JSONObject().put("Email", recipient)))
                            .put(Emailv31.Message.SUBJECT, "Your email flight plan!")
                            .put(
                                Emailv31.Message.TEXTPART,
                                "Dear passenger, welcome to Mailjet!" 
                                + "May the delivery force be with you!")
                            .put(
                                Emailv31.Message.HTMLPART,
                                "<h3>Dear passenger, welcome to Mailjet!</h3>"
                                + "<br />May the delivery force be with you!")));

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
// [END compute_mailjet_send_email]
