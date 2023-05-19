/*
 *  Copyright 2023 Google LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package secretmanager;

// [START secretmanager_consume_event_notification]

import java.util.Base64;
import java.util.Map;
import java.util.logging.Logger;

// Demonstrates how to consume and process a Pub/Sub notification from Secret Manager.Triggered
// from a message on a Cloud Pub/Sub topic.
// Ideally the class should implement a background function that accepts a PubSub message.
// public class ConsumeEventNotification implements BackgroundFunction<PubSubMessage> { }
public class ConsumeEventNotification {

  // You can configure the logs to print the message in Cloud Logging.
  private static final Logger logger = Logger.getLogger(ConsumeEventNotification.class.getName());

  // Accepts a message from a PubSub topic and writes it to logger.
  public static String accept(PubSubMessage message) {
    String eventType = message.attributes.get("eventType");
    String secretId = message.attributes.get("secretId");
    String data = new String(Base64.getDecoder().decode(message.data));
    String log = String.format("Received %s for %s. New metadata: %s", eventType, secretId, data);
    logger.info(log);
    return log;
  }

  // Event payload. Mock of the actual PubSub message.
  public static class PubSubMessage {

    byte[] data;
    Map<String, String> attributes;
    String messageId;
    String publishTime;

    public byte[] getData() {
      return data;
    }

    public void setData(byte[] data) {
      this.data = data;
    }

    public Map<String, String> getAttributes() {
      return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
      this.attributes = attributes;
    }

    public String getMessageId() {
      return messageId;
    }

    public void setMessageId(String messageId) {
      this.messageId = messageId;
    }

    public String getPublishTime() {
      return publishTime;
    }

    public void setPublishTime(String publishTime) {
      this.publishTime = publishTime;
    }

    @Override
    public String toString() {
      return "PubSubMessage{" +
          "data='" + data + '\'' +
          ", attributes=" + attributes +
          ", messageId='" + messageId + '\'' +
          ", publishTime='" + publishTime + '\'' +
          '}';
    }
  }
}
// [END secretmanager_consume_event_notification]
