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

package com.google.cloud.vision.samples.facedetect;

// [START vision_face_detection_tutorial_imports]
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.FaceAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.Vertex;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.ByteString;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javax.imageio.ImageIO;

// [END vision_face_detection_tutorial_imports]

/**
 * A sample application that uses the Vision API to detect faces in an image.
 */
@SuppressWarnings("serial")
public class FaceDetectApp {

  private static final int MAX_RESULTS = 4;

  // [START vision_face_detection_tutorial_run_application]
  /** Annotates an image using the Vision API. */
  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.err.println("Usage:");
      System.err.printf(
          "\tjava %s inputImagePath outputImagePath\n", FaceDetectApp.class.getCanonicalName());
      System.exit(1);
    }
    Path inputPath = Paths.get(args[0]);
    Path outputPath = Paths.get(args[1]);
    if (!outputPath.toString().toLowerCase().endsWith(".jpg")) {
      System.err.println("outputImagePath must have the file extension 'jpg'.");
      System.exit(1);
    }

    try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
      FaceDetectApp app = new FaceDetectApp(client);
      List<FaceAnnotation> faces = app.detectFaces(inputPath, MAX_RESULTS);
      System.out.printf("Found %d face%s\n", faces.size(), faces.size() == 1 ? "" : "s");
      System.out.printf("Writing to file %s\n", outputPath);
      app.writeWithFaces(inputPath, outputPath, faces);
    }
  }
  // [END vision_face_detection_tutorial_run_application]

  // [START vision_face_detection_tutorial_client]
  /** Connects to the Vision API using Application Default Credentials. */
  // [END vision_face_detection_tutorial_client]

  private final ImageAnnotatorClient vision;

  /** Constructs a {@link FaceDetectApp} which connects to the Vision API. */
  public FaceDetectApp(ImageAnnotatorClient vision) {
    this.vision = vision;
  }

  // [START vision_face_detection_tutorial_send_request]
  /** Gets up to {@code maxResults} faces for an image stored at {@code path}. */
  public List<FaceAnnotation> detectFaces(Path path, int maxResults) throws IOException {
    ByteString imgBytes = ByteString.copyFrom(Files.readAllBytes(path));

    Image img = Image.newBuilder().setContent(imgBytes).build();
    Feature feat = Feature.newBuilder()
        .setType(Feature.Type.FACE_DETECTION)
        .setMaxResults(maxResults)
        .build();
    AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
        .setImage(img)
        .addFeatures(feat)
        .build();

    BatchAnnotateImagesResponse response = vision.batchAnnotateImages(ImmutableList.of(request));
    List<AnnotateImageResponse> responses = response.getResponsesList();

    if (responses.isEmpty()) {
      throw new IOException("No response received from the Vision API.");
    }

    AnnotateImageResponse imageResponse = responses.get(0);
    if (imageResponse.hasError()) {
      throw new IOException(
          "Error getting image annotations: " + imageResponse.getError().getMessage());
    }
    return imageResponse.getFaceAnnotationsList();
  }
  // [END vision_face_detection_tutorial_send_request]

  // [START vision_face_detection_tutorial_process_response]
  /**
   * Reads image {@code inputPath} and writes {@code outputPath} with
   * {@code faces} outlined.
   */
  private static void writeWithFaces(Path inputPath, Path outputPath, List<FaceAnnotation> faces)
      throws IOException {
    BufferedImage img = ImageIO.read(inputPath.toFile());
    annotateWithFaces(img, faces);
    ImageIO.write(img, "jpg", outputPath.toFile());
  }

  /**
   * Annotates an image {@code img} with a polygon around each face in
   * {@code faces}.
   */
  public static void annotateWithFaces(BufferedImage img, List<FaceAnnotation> faces) {
    for (FaceAnnotation face : faces) {
      annotateWithFace(img, face);
    }
  }

  /** Annotates an image {@code img} with a polygon defined by {@code face}. */
  private static void annotateWithFace(BufferedImage img, FaceAnnotation face) {
    Graphics2D gfx = img.createGraphics();
    Polygon poly = new Polygon();
    for (Vertex vertex : face.getFdBoundingPoly().getVerticesList()) {
      poly.addPoint(vertex.getX(), vertex.getY());
    }
    gfx.setStroke(new BasicStroke(5));
    gfx.setColor(new Color(0x00ff00));
    gfx.draw(poly);
  }
  // [END vision_face_detection_tutorial_process_response]
}