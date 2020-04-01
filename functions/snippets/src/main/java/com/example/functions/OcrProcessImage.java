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

package com.example.functions;

// [START functions_ocr_process]

import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.translate.v3.DetectLanguageRequest;
import com.google.cloud.translate.v3.DetectLanguageResponse;
import com.google.cloud.translate.v3.LocationName;
import com.google.cloud.translate.v3.TranslationServiceClient;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageSource;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

public class OcrProcessImage implements BackgroundFunction<GcsEvent> {
  // TODO<developer> set these environment variables
  private static final String PROJECT_ID = System.getenv("GCP_PROJECT");
  private static final String TRANSLATE_TOPIC_NAME = System.getenv("TRANSLATE_TOPIC");
  private static final String[] TO_LANGS = System.getenv("TO_LANG").split(",");

  private static final Logger LOGGER = Logger.getLogger(OcrProcessImage.class.getName());
  private static final String LOCATION_NAME = LocationName.of(PROJECT_ID, "global").toString();
  private static Publisher publisher;
  private static final Gson gson = new Gson();

  public OcrProcessImage() throws IOException {
    publisher = Publisher.newBuilder(
        ProjectTopicName.of(PROJECT_ID, TRANSLATE_TOPIC_NAME)).build();
  }

  @Override
  public void accept(GcsEvent gcsEvent, Context context) throws RuntimeException {

    // Validate parameters
    String bucket = gcsEvent.getBucket();
    if (bucket == null) {
      throw new IllegalArgumentException("Missing bucket parameter");
    }
    String filename = gcsEvent.getName();
    if (filename == null) {
      throw new IllegalArgumentException("Missing name parameter");
    }

    detectText(bucket, filename);
  }
  // [END functions_ocr_process]

  // [START functions_ocr_detect]
  private void detectText(String bucket, String filename) throws RuntimeException {
    LOGGER.info("Looking for text in image " + filename);

    List<AnnotateImageRequest> visionRequests = new ArrayList<>();
    String gcsPath = String.format("gs://%s/%s", bucket, filename);

    ImageSource imgSource = ImageSource.newBuilder().setGcsImageUri(gcsPath).build();
    Image img = Image.newBuilder().setSource(imgSource).build();

    Feature textFeature = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
    AnnotateImageRequest visionRequest =
        AnnotateImageRequest.newBuilder().addFeatures(textFeature).setImage(img).build();
    visionRequests.add(visionRequest);

    // Detect text in an image using the Cloud Vision API
    AnnotateImageResponse visionResponse;
    try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
      visionResponse = client.batchAnnotateImages(visionRequests).getResponses(0);
      if (visionResponse.hasError()) {
        LOGGER.severe("Error detecting text: " + visionResponse.getError().getMessage());
        return;
      }
    } catch (IOException e) {
      // Cast to RuntimeException
      throw new RuntimeException(e);
    }

    if (visionResponse == null || !visionResponse.hasFullTextAnnotation()) {
      return;
    }

    String text = visionResponse.getFullTextAnnotation().getText();
    LOGGER.info("Extracted text from image: " + text);

    // Detect language using the Cloud Translation API
    DetectLanguageRequest languageRequest =
        DetectLanguageRequest.newBuilder()
            .setParent(LOCATION_NAME.toString())
            .setMimeType("text/plain")
            .setContent(text)
            .build();
    DetectLanguageResponse languageResponse;
    try (TranslationServiceClient client = TranslationServiceClient.create()) {
      languageResponse = client.detectLanguage(languageRequest);
    } catch (IOException e) {
      // Cast to RuntimeException
      throw new RuntimeException(e);
    }

    if (languageResponse == null || languageResponse.getLanguagesCount() == 0) {
      return;
    }

    String languageCode = languageResponse.getLanguages(0).getLanguageCode();
    LOGGER.info(String.format("Detected language %s for file %s", languageCode, filename));

    // Send a Pub/Sub translation request for every language we're going to translate to
    for (String targetLanguage : TO_LANGS) {
      LOGGER.info("Sending translation request for language " + targetLanguage);
      OcrTranslateApiMessage message = new OcrTranslateApiMessage(text, filename, targetLanguage);
      ByteString byteStr = ByteString.copyFrom(message.toPubsubData());
      PubsubMessage pubsubApiMessage = PubsubMessage.newBuilder().setData(byteStr).build();
      try {
        publisher.publish(pubsubApiMessage).get();
      } catch (InterruptedException | ExecutionException e) {
        // Cast to RuntimeException
        throw new RuntimeException(e);
      }
    }
  }
  // [END functions_ocr_detect]

  // [START functions_ocr_process]
}
// [END functions_ocr_process]
