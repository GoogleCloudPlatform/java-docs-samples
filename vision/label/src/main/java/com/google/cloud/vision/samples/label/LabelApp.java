/*
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

package com.google.cloud.vision.samples.label;

// [START import_libraries]
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionScopes;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.List;
// [END import_libraries]

/**
 * A sample application that uses the Vision API to label an image.
 */
@SuppressWarnings("serial")
public class LabelApp {
  /**
   * Be sure to specify the name of your application. If the application name is {@code null} or
   * blank, the application will log a warning. Suggested format is "MyCompany-ProductName/1.0".
   */
  private static final String APPLICATION_NAME = "Google-VisionLabelSample/1.0";

  private static final int MAX_LABELS = 3;

  // [START run_application]
  /**
   * Annotates an image using the Vision API.
   */
  public static void main(String[] args) throws IOException, GeneralSecurityException {
    if (args.length != 1) {
      System.err.println("Missing imagePath argument.");
      System.err.println("Usage:");
      System.err.printf("\tjava %s imagePath\n", LabelApp.class.getCanonicalName());
      System.exit(1);
    }
    Path imagePath = Paths.get(args[0]);

    LabelApp app = new LabelApp(getVisionService());
    printLabels(System.out, imagePath, app.labelImage(imagePath, MAX_LABELS));
  }

  /**
   * Prints the labels received from the Vision API.
   */
  public static void printLabels(PrintStream out, Path imagePath, List<EntityAnnotation> labels) {
    out.printf("Labels for image %s:\n", imagePath);
    for (EntityAnnotation label : labels) {
      out.printf(
          "\t%s (score: %.3f)\n",
          label.getDescription(),
          label.getScore());
    }
    if (labels.isEmpty()) {
      out.println("\tNo labels found.");
    }
  }
  // [END run_application]

  // [START authenticate]
  /**
   * Connects to the Vision API using Application Default Credentials.
   */
  public static Vision getVisionService() throws IOException, GeneralSecurityException {
    GoogleCredential credential =
        GoogleCredential.getApplicationDefault().createScoped(VisionScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    return new Vision.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, credential)
            .setApplicationName(APPLICATION_NAME)
            .build();
  }
  // [END authenticate]

  private final Vision vision;

  /**
   * Constructs a {@link LabelApp} which connects to the Vision API.
   */
  public LabelApp(Vision vision) {
    this.vision = vision;
  }

  /**
   * Gets up to {@code maxResults} labels for an image stored at {@code path}.
   */
  public List<EntityAnnotation> labelImage(Path path, int maxResults) throws IOException {
    // [START construct_request]
    byte[] data = Files.readAllBytes(path);

    AnnotateImageRequest request =
        new AnnotateImageRequest()
            .setImage(new Image().encodeContent(data))
            .setFeatures(ImmutableList.of(
                new Feature()
                    .setType("LABEL_DETECTION")
                    .setMaxResults(maxResults)));
    Vision.Images.Annotate annotate =
        vision.images()
            .annotate(new BatchAnnotateImagesRequest().setRequests(ImmutableList.of(request)));
    // Due to a bug: requests to Vision API containing large images fail when GZipped.
    // annotate.setDisableGZipContent(true);
    // [END construct_request]

    // [START parse_response]
    BatchAnnotateImagesResponse batchResponse = annotate.execute();
    assert batchResponse.getResponses().size() == 1;
    AnnotateImageResponse response = batchResponse.getResponses().get(0);
    if (response.getLabelAnnotations() == null) {
      throw new IOException(
          response.getError() != null
              ? response.getError().getMessage()
              : "Unknown error getting image annotations");
    }
    return response.getLabelAnnotations();
    // [END parse_response]
  }
}
