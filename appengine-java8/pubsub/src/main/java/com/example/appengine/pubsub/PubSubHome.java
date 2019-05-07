/*
 * Copyright 2017 Google Inc.
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

package com.example.appengine.pubsub;

import com.googlecode.jatl.Html;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

public class PubSubHome {

  private static MessageRepository messageRepository = MessageRepositoryImpl.getInstance();
  private static int MAX_MESSAGES = 10;

  /**
   * Retrieve received messages in html.
   *
   * @return html representation of messages (one per row)
   */
  public static List<Message> getReceivedMessages() {
    List<Message> messageList = messageRepository.retrieve(MAX_MESSAGES);
    return messageList;
  }

  /**
   * Retrieve received claims in html.
   *
   * @return html representation of claims (one per row)
   */
  public static List<String> getReceivedClaims() {
    List<String> claimList = messageRepository.retrieveClaims(MAX_MESSAGES);
    return claimList;
  }

  /**
   * Retrieve received tokens in html.
   *
   * @return html representation of tokens (one per row)
   */
  public static List<String> getReceivedTokens() {
    List<String> tokenList = messageRepository.retrieveTokens(MAX_MESSAGES);
    return tokenList;
  }

  public static String convertToHtml() {
    Writer writer = new StringWriter(1024);
    new Html(writer) {
      {
        html();
        head();
        meta().httpEquiv("refresh").content("10").end();
        end();
        body();
        h3().text("Publish a message").end();
        form().action("pubsub/publish").method("POST");
        label().text("Message:").end();
        input().id("payload").type("input").name("payload").end();
        input().id("submit").type("submit").value("Send").end();
        end();
        h3().text("Last received tokens").end();
        table().border("1").cellpadding("10");
        tr();
        th().text("Tokens").end();
        end();
        markupString(getReceivedTokens());
        h3().text("Last received claims").end();
        table().border("1").cellpadding("10");
        tr();
        th().text("Claims").end();
        end();
        markupString(getReceivedClaims());
        h3().text("Last received messages").end();
        table().border("1").cellpadding("10");
        tr();
        th().text("Id").end();
        th().text("Data").end();
        th().text("PublishTime").end();
        end();
        markupMessage(getReceivedMessages());
        endAll();
        done();
      }

      Html markupString(List<String> strings) {
        for (String string : strings) {
          tr();
          th().text(string).end();
          end();
        }
        return end();
      }

      Html markupMessage(List<Message> messages) {
        for (Message message : messages) {
          tr();
          th().text(message.getMessageId()).end();
          th().text(message.getData()).end();
          th().text(message.getPublishTime()).end();
          end();
        }
        return end();
      }
    };
    return ((StringWriter) writer).getBuffer().toString();
  }

  private PubSubHome() {}
}
