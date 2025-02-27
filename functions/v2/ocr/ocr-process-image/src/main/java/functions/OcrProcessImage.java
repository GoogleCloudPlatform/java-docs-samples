/*
 * Copyright 2022 Google LLC
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

// [START functions_ocr_process]

import com.google.cloud.functions.CloudEventsFunction;
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
import com.google.events.cloud.storage.v1.StorageObjectData;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;
import io.cloudevents.CloudEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

// [END functions_ocr_process]

// [START functions_ocr_setup]
public class OcrProcessImage implements CloudEventsFunction {
  // TODO<developer> set these environment variables
  private static final String PROJECT_ID = System.getenv("GCP_PROJECT");
  private static final String TRANSLATE_TOPIC_NAME = System.getenv("TRANSLATE_TOPIC");
  private static final String[] TO_LANGS = System.getenv("TO_LANG") == null ? new String[] { "es" }
      : System.getenv("TO_LANG").split(",");

  private static final Logger logger = Logger.getLogger(OcrProcessImage.class.getName());
  private static final String LOCATION_NAME = LocationName.of(PROJECT_ID, "global").toString();
  private Publisher publisher;

  public OcrProcessImage() throws IOException {
    publisher = Publisher.newBuilder(ProjectTopicName.of(PROJECT_ID, TRANSLATE_TOPIC_NAME)).build();
  }

  // [END functions_ocr_setup]

  // [START functions_ocr_process]
  @Override
  public void accept(CloudEvent event) throws InvalidProtocolBufferException {
    // Unmarshal data from CloudEvent
    String cloudEventData = new String(event.getData().toBytes(), StandardCharsets.UTF_8);
    StorageObjectData.Builder builder = StorageObjectData.newBuilder();
    
    // If you do not ignore unknown fields, then JsonFormat.Parser returns an
    // error when encountering a new or unknown field. Note that you might lose
    // some event data in the unmarshaling process by ignoring unknown fields.
    JsonFormat.Parser parser = JsonFormat.parser().ignoringUnknownFields();
    parser.merge(cloudEventData, builder);
    StorageObjectData gcsEvent = builder.build();

    String bucket = gcsEvent.getBucket();
    if (bucket.isEmpty()) {
      throw new IllegalArgumentException("Missing bucket parameter");
    }
    String filename = gcsEvent.getName();
    if (filename.isEmpty()) {
      throw new IllegalArgumentException("Missing name parameter");
    }

    detectText(bucket, filename);
  }
  // [END functions_ocr_process]

  // [START functions_ocr_detect]
  private void detectText(String bucket, String filename) {
    logger.info("Looking for text in image " + filename);

    List<AnnotateImageRequest> visionRequests = new ArrayList<>();
    String gcsPath = String.format("gs://%s/%s", bucket, filename);

    ImageSource imgSource = ImageSource.newBuilder().setGcsImageUri(gcsPath).build();
    Image img = Image.newBuilder().setSource(imgSource).build();

    Feature textFeature = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
    AnnotateImageRequest visionRequest = AnnotateImageRequest.newBuilder()
        .addFeatures(textFeature).setImage(img)
        .build();
    visionRequests.add(visionRequest);

    // Detect text in an image using the Cloud Vision API
    AnnotateImageResponse visionResponse;
    try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
      visionResponse = client.batchAnnotateImages(visionRequests).getResponses(0);
      if (visionResponse == null || !visionResponse.hasFullTextAnnotation()) {
        logger.info(String.format("Image %s contains no text", filename));
        return;
      }

      if (visionResponse.hasError()) {
        // Log error
        logger.log(
            Level.SEVERE, "Error in vision API call: " + visionResponse.getError().getMessage());
        return;
      }
    } catch (IOException e) {
      // Log error (since IOException cannot be thrown by a Cloud Function)
      logger.log(Level.SEVERE, "Error detecting text: " + e.getMessage(), e);
      return;
    }

    String text = visionResponse.getFullTextAnnotation().getText();
    logger.info("Extracted text from image: " + text);

    // Detect language using the Cloud Translation API
    DetectLanguageRequest languageRequest = DetectLanguageRequest.newBuilder()
        .setParent(LOCATION_NAME)
        .setMimeType("text/plain")
        .setContent(text)
        .build();
    DetectLanguageResponse languageResponse;
    try (TranslationServiceClient client = TranslationServiceClient.create()) {
      languageResponse = client.detectLanguage(languageRequest);
    } catch (IOException e) {
      // Log error (since IOException cannot be thrown by a function)
      logger.log(Level.SEVERE, "Error detecting language: " + e.getMessage(), e);
      return;
    }

    if (languageResponse.getLanguagesCount() == 0) {
      logger.info("No languages were detected for text: " + text);
      return;
    }

    String languageCode = languageResponse.getLanguages(0).getLanguageCode();
    logger.info(String.format("Detected language %s for file %s", languageCode, filename));

    // Send a Pub/Sub translation request for every language we're going to
    // translate to
    for (String targetLanguage : TO_LANGS) {
      logger.info("Sending translation request for language " + targetLanguage);
      OcrTranslateApiMessage message = new OcrTranslateApiMessage(text, filename, targetLanguage);
      ByteString byteStr = ByteString.copyFrom(message.toPubsubData());
      PubsubMessage pubsubApiMessage = PubsubMessage.newBuilder().setData(byteStr).build();
      try {
        publisher.publish(pubsubApiMessage).get();
      } catch (InterruptedException | ExecutionException e) {
        // Log error
        logger.log(Level.SEVERE, "Error publishing translation request: " + e.getMessage(), e);
        return;
      }
    }
  }
  // [END functions_ocr_detect]

  // [START functions_ocr_process]
  // [START functions_ocr_setup]
}
// [END functions_ocr_setup]
// [END functions_ocr_process]
