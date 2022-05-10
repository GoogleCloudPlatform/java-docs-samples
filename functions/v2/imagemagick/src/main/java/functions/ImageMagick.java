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

// [START functions_imagemagick_setup]


import com.google.cloud.functions.CloudEventsFunction;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.gson.Gson;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageSource;
import com.google.cloud.vision.v1.SafeSearchAnnotation;
import functions.eventpojos.GcsEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.cloudevents.CloudEvent;

public class ImageMagick implements CloudEventsFunction {

  private static Storage storage = StorageOptions.getDefaultInstance().getService();
  private static final String BLURRED_BUCKET_NAME = System.getenv("BLURRED_BUCKET_NAME");
  private static final Logger logger = Logger.getLogger(ImageMagick.class.getName());
  // [END functions_imagemagick_setup]

  // [START functions_imagemagick_analyze]
  @Override
  // Blurs uploaded images that are flagged as Adult or Violence.
  public void accept(CloudEvent event) {
    // Extract the GCS Event data from the CloudEvent's data payload.
    GcsEvent data = getEventData(event);
    // Validate parameters
    if (data.getBucket() == null || data.getName() == null) {
      logger.severe("Error: Malformed GCS event.");
      return;
    }

    BlobInfo blobInfo = BlobInfo.newBuilder(data.getBucket(), data.getName()).build();

    // Construct URI to GCS bucket and file.
    String gcsPath = String.format("gs://%s/%s", data.getBucket(), data.getName());
    logger.info(String.format("Analyzing %s", data.getName()));

    // Construct request.
    ImageSource imgSource = ImageSource.newBuilder().setImageUri(gcsPath).build();
    Image img = Image.newBuilder().setSource(imgSource).build();
    Feature feature = Feature.newBuilder().setType(Type.SAFE_SEARCH_DETECTION).build();
    AnnotateImageRequest request =
        AnnotateImageRequest.newBuilder().addFeatures(feature).setImage(img).build();
    List<AnnotateImageRequest> requests = List.of(request);

    // Send request to the Vision API.
    try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
      BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
      List<AnnotateImageResponse> responses = response.getResponsesList();
      for (AnnotateImageResponse res : responses) {
        if (res.hasError()) {
          logger.info(String.format("Error: %s", res.getError().getMessage()));
          return;
        }
        // Get Safe Search Annotations
        SafeSearchAnnotation annotation = res.getSafeSearchAnnotation();
        if (annotation.getAdultValue() == 5 || annotation.getViolenceValue() == 5) {
          logger.info(String.format("Detected %s as inappropriate.", data.getName()));
          blur(blobInfo);
        } else {
          logger.info(String.format("Detected %s as OK.", data.getName()));
        }
      }
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error with Vision API: " + e.getMessage(), e);
    }
  }
  // [END functions_imagemagick_analyze]

  // [START functions_imagemagick_blur]
  // Blurs the file described by blobInfo using ImageMagick,
  // and uploads it to the blurred bucket.
  private static void blur(BlobInfo blobInfo) throws IOException {
    String bucketName = blobInfo.getBucket();
    String fileName = blobInfo.getName();

    // Download image
    Blob blob = storage.get(BlobId.of(bucketName, fileName));
    Path download = Paths.get("/tmp/", fileName);
    blob.downloadTo(download);

    // Construct the command.
    Path upload = Paths.get("/tmp/", "blurred-" + fileName);
    List<String> args = List.of("convert", download.toString(), "-blur", "0x8", upload.toString());
    try {
      ProcessBuilder pb = new ProcessBuilder(args);
      Process process = pb.start();
      process.waitFor();
    } catch (Exception e) {
      logger.info(String.format("Error: %s", e.getMessage()));
    }

    // Upload image to blurred bucket.
    BlobId blurredBlobId = BlobId.of(BLURRED_BUCKET_NAME, fileName);
    BlobInfo blurredBlobInfo =
        BlobInfo.newBuilder(blurredBlobId).setContentType(blob.getContentType()).build();

    byte[] blurredFile = Files.readAllBytes(upload);
    storage.create(blurredBlobInfo, blurredFile);
    logger.info(
        String.format("Blurred image uploaded to: gs://%s/%s", BLURRED_BUCKET_NAME, fileName));

    // Remove images from fileSystem
    Files.delete(download);
    Files.delete(upload);
  }
  // [END functions_imagemagick_blur]

  // Converts CloudEvent data payload to a GcsEvent
  private static GcsEvent getEventData(CloudEvent event) {
    if (event.getData() != null) {
      // Extract Cloud Event data and convert to GcsEvent
      String cloudEventData = new String(event.getData().toBytes(), StandardCharsets.UTF_8);
      Gson gson = new Gson();
      return gson.fromJson(cloudEventData, GcsEvent.class);
    }
    return new GcsEvent();
  }
  // [START functions_imagemagick_setup]
}
// [END functions_imagemagick_setup]
