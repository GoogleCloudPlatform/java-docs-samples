/*
 * Copyright 2020 Google LLC
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

package functions;

// [START functions_pubsub_publish]
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PublishMessage implements HttpFunction {
  // TODO<developer> set this environment variable
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");

  private static final Logger logger = Logger.getLogger(PublishMessage.class.getName());

  @Override
  public void service(HttpRequest request, HttpResponse response) throws IOException {
    Optional<String> maybeTopicName = request.getFirstQueryParameter("topic");
    Optional<String> maybeMessage = request.getFirstQueryParameter("message");

    BufferedWriter responseWriter = response.getWriter();

    if (maybeTopicName.isEmpty() || maybeMessage.isEmpty()) {
      response.setStatusCode(HttpURLConnection.HTTP_BAD_REQUEST);

      responseWriter.write("Missing 'topic' and/or 'message' parameter(s).");
      return;
    }

    String topicName = maybeTopicName.get();
    logger.info("Publishing message to topic: " + topicName);

    // Create the PubsubMessage object
    // (This is different than the PubSubMessage POJO used in Pub/Sub-triggered functions)
    ByteString byteStr = ByteString.copyFrom(maybeMessage.get(), StandardCharsets.UTF_8);
    PubsubMessage pubsubApiMessage = PubsubMessage.newBuilder().setData(byteStr).build();

    Publisher publisher = Publisher.newBuilder(
        ProjectTopicName.of(PROJECT_ID, topicName)).build();

    // Attempt to publish the message
    String responseMessage;
    try {
      publisher.publish(pubsubApiMessage).get();
      responseMessage = "Message published.";
    } catch (InterruptedException | ExecutionException e) {
      logger.log(Level.SEVERE, "Error publishing Pub/Sub message: " + e.getMessage(), e);
      responseMessage = "Error publishing Pub/Sub message; see logs for more info.";
    }

    responseWriter.write(responseMessage);
  }
}
// [END functions_pubsub_publish]
