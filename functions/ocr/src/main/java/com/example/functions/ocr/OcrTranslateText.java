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

package com.example.functions.ocr;

// [START functions_ocr_translate]

import com.example.functions.ocr.eventpojos.PubSubMessage;
import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.translate.v3.LocationName;
import com.google.cloud.translate.v3.TranslateTextRequest;
import com.google.cloud.translate.v3.TranslateTextResponse;
import com.google.cloud.translate.v3.TranslationServiceClient;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OcrTranslateText implements BackgroundFunction<PubSubMessage> {
  // TODO<developer> set these environment variables
  private static final String PROJECT_ID = System.getenv("GCP_PROJECT");
  private static final String RESULTS_TOPIC_NAME = System.getenv("RESULT_TOPIC");
  private static final Logger LOGGER = Logger.getLogger(OcrTranslateText.class.getName());
  private static final String LOCATION_NAME = LocationName.of(PROJECT_ID, "global").toString();

  private Publisher publisher;

  public OcrTranslateText() throws IOException {
    publisher = Publisher.newBuilder(
        ProjectTopicName.of(PROJECT_ID, RESULTS_TOPIC_NAME)).build();
  }

  @Override
  public void accept(PubSubMessage pubSubMessage, Context context) {
    OcrTranslateApiMessage ocrMessage = OcrTranslateApiMessage.fromPubsubData(
        pubSubMessage.getData().getBytes(StandardCharsets.UTF_8));

    String targetLang = ocrMessage.getLang();
    LOGGER.info("Translating text into " + targetLang);

    // Translate text to target language
    String text = ocrMessage.getText();
    TranslateTextRequest request =
        TranslateTextRequest.newBuilder()
            .setParent(LOCATION_NAME)
            .setMimeType("text/plain")
            .setTargetLanguageCode(targetLang)
            .addContents(text)
            .build();

    TranslateTextResponse response;
    try (TranslationServiceClient client = TranslationServiceClient.create()) {
      response = client.translateText(request);
    } catch (IOException e) {
      // Log error (since IOException cannot be thrown by a function)
      LOGGER.log(Level.SEVERE, "Error translating text: " + e.getMessage(), e);
      return;
    }
    if (response.getTranslationsCount() == 0) {
      return;
    }

    String translatedText = response.getTranslations(0).getTranslatedText();
    LOGGER.info("Translated text: " + translatedText);

    // Send translated text to (subsequent) Pub/Sub topic
    String filename = ocrMessage.getFilename();
    OcrTranslateApiMessage translateMessage = new OcrTranslateApiMessage(
        translatedText, filename, targetLang);
    try {
      ByteString byteStr = ByteString.copyFrom(translateMessage.toPubsubData());
      PubsubMessage pubsubApiMessage = PubsubMessage.newBuilder().setData(byteStr).build();

      publisher.publish(pubsubApiMessage).get();
      LOGGER.info("Text translated to " + targetLang);
    } catch (InterruptedException | ExecutionException e) {
      // Log error (since these exception types cannot be thrown by a function)
      LOGGER.log(Level.SEVERE, "Error publishing translation save request: " + e.getMessage(), e);
    }
  }
}
// [END functions_ocr_translate]
