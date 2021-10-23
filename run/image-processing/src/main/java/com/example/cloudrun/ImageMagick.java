/*
 * Copyright 2019 Google LLC
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

package com.example.cloudrun;

// [START cloudrun_imageproc_handler_setup]
// [START run_imageproc_handler_setup]
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
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageSource;
import com.google.cloud.vision.v1.SafeSearchAnnotation;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ImageMagick {

  private static final String BLURRED_BUCKET_NAME = System.getenv("BLURRED_BUCKET_NAME");
  private static Storage storage = StorageOptions.getDefaultInstance().getService();
  // [END run_imageproc_handler_setup]
  // [END cloudrun_imageproc_handler_setup]

  // [START cloudrun_imageproc_handler_analyze]
  // [START run_imageproc_handler_analyze]
  // Blurs uploaded images that are flagged as Adult or Violence.
  public static void blurOffensiveImages(JsonObject data) {
    String fileName = data.get("name").getAsString();
    String bucketName = data.get("bucket").getAsString();
    BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName).build();
    // Construct URI to GCS bucket and file.
    String gcsPath = String.format("gs://%s/%s", bucketName, fileName);
    System.out.println(String.format("Analyzing %s", fileName));

    // Construct request.
    List<AnnotateImageRequest> requests = new ArrayList<>();
    ImageSource imgSource = ImageSource.newBuilder().setImageUri(gcsPath).build();
    Image img = Image.newBuilder().setSource(imgSource).build();
    Feature feature = Feature.newBuilder().setType(Type.SAFE_SEARCH_DETECTION).build();
    AnnotateImageRequest request =
        AnnotateImageRequest.newBuilder().addFeatures(feature).setImage(img).build();
    requests.add(request);

    // Send request to the Vision API.
    try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
      BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
      List<AnnotateImageResponse> responses = response.getResponsesList();
      for (AnnotateImageResponse res : responses) {
        if (res.hasError()) {
          System.out.println(String.format("Error: %s\n", res.getError().getMessage()));
          return;
        }
        // Get Safe Search Annotations
        SafeSearchAnnotation annotation = res.getSafeSearchAnnotation();
        if (annotation.getAdultValue() == 5 || annotation.getViolenceValue() == 5) {
          System.out.println(String.format("Detected %s as inappropriate.", fileName));
          blur(blobInfo);
        } else {
          System.out.println(String.format("Detected %s as OK.", fileName));
        }
      }
    } catch (Exception e) {
      System.out.println(String.format("Error with Vision API: %s", e.getMessage()));
    }
  }
  // [END run_imageproc_handler_analyze]
  // [END cloudrun_imageproc_handler_analyze]

  // [START cloudrun_imageproc_handler_blur]
  // [START run_imageproc_handler_blur]
  // Blurs the file described by blobInfo using ImageMagick,
  // and uploads it to the blurred bucket.
  public static void blur(BlobInfo blobInfo) throws IOException {
    String bucketName = blobInfo.getBucket();
    String fileName = blobInfo.getName();
    // Download image
    Blob blob = storage.get(BlobId.of(bucketName, fileName));
    Path download = Paths.get("/tmp/", fileName);
    blob.downloadTo(download);

    // Construct the command.
    List<String> args = new ArrayList<String>();
    args.add("convert");
    args.add(download.toString());
    args.add("-blur");
    args.add("0x8");
    Path upload = Paths.get("/tmp/", "blurred-" + fileName);
    args.add(upload.toString());
    try {
      ProcessBuilder pb = new ProcessBuilder(args);
      Process process = pb.start();
      process.waitFor();
    } catch (Exception e) {
      System.out.println(String.format("Error: %s", e.getMessage()));
    }

    // Upload image to blurred bucket.
    BlobId blurredBlobId = BlobId.of(BLURRED_BUCKET_NAME, fileName);
    BlobInfo blurredBlobInfo =
        BlobInfo.newBuilder(blurredBlobId).setContentType(blob.getContentType()).build();
    try {
      byte[] blurredFile = Files.readAllBytes(upload);
      Blob blurredBlob = storage.create(blurredBlobInfo, blurredFile);
      System.out.println(
          String.format("Blurred image uploaded to: gs://%s/%s", BLURRED_BUCKET_NAME, fileName));
    } catch (Exception e) {
      System.out.println(String.format("Error in upload: %s", e.getMessage()));
    }

    // Remove images from fileSystem
    Files.delete(download);
    Files.delete(upload);
  }
}
// [END run_imageproc_handler_blur]
// [END cloudrun_imageproc_handler_blur]
