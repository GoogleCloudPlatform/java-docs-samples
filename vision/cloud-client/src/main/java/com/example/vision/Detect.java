/**
 * Copyright 2017, Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.vision;


import com.google.cloud.vision.spi.v1.ImageAnnotatorClient;
import com.google.cloud.vision.spi.v1.ImageAnnotatorSettings;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageSource;
import com.google.cloud.vision.v1.LocationInfo;
import com.google.cloud.vision.v1.SafeSearchAnnotation;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.ColorInfo;
import com.google.cloud.vision.v1.DominantColorsAnnotation;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.FaceAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.AnnotateImageResponse;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.joda.time.Duration;


public class Detect{

  /**
   * Detects entities,sentiment and syntax in a document using the Natural Language API.
   */
  public static void main(String[] args) throws IOException{
    argsHelper(args, System.out);
  }
  
  /**
   * Helper that handles the input passed to the program.
   */
  public static void argsHelper(String[] args, PrintStream out) throws IOException{
    if (args.length < 1) {
      out.println("Usage:");
      out.printf(
          "\tjava %s \"<command>\" \"<path-to-image>\"\n" +
          "Commands:\n\tall-local | faces | labels | landmarks | logos | text | safe-search | properties\n" +
          "Path:\n\tA file path (ex: ./resources/wakeupcat.jpg) or a URI for a Cloud Storage resource (gs://...)\n",
          Detect.class.getCanonicalName());
      return;
    }
    String command = args[0];
    String path = args.length > 1 ? args[1] : "";

    Detect app = new Detect(ImageAnnotatorClient.create());

    if (command.equals("all-local")) {
      detectFaces("resources/face_no_surprise.jpg", System.out);
      detectLabels("resources/wakeupcat.jpg", System.out);
      detectLandmarks("resources/landmark.jpg", System.out);
      detectLogos("resources/logos.png", System.out);
      detectText("resources/text.jpg", System.out);
      detectProperties("resources/landmark.jpg", System.out);
      detectSafeSearch("resources/wakeupcat.jpg", System.out);
    } else if (command.equals("faces")) {
      if (path.startsWith("gs://")) {
        // TODO: See https://goo.gl/uWgYhQ
      } else {
        detectFaces(path, System.out);
      }
    } else if (command.equals("labels")) {
      if (path.startsWith("gs://")) {
        // TODO: See https://goo.gl/uWgYhQ
      } else {
        detectLabels(path, System.out);
      }
    } else if (command.equals("landmarks")) {
      if (path.startsWith("gs://")) {
        // TODO: See https://goo.gl/uWgYhQ
      } else {
        detectLandmarks(path, System.out);
      }
    } else if (command.equals("logos")) {
      if (path.startsWith("gs://")) {
        // TODO: See https://goo.gl/uWgYhQ
      } else {
        detectLogos(path, System.out);
      }
    } else if (command.equals("text")) {
      if (path.startsWith("gs://")) {
        // TODO: See https://goo.gl/uWgYhQ
      } else {
        detectText(path, System.out);
      }
    } else if (command.equals("properties")) {
      if (path.startsWith("gs://")) {
        // TODO: See https://goo.gl/uWgYhQ
      } else {
        detectProperties(path, System.out);
      }
    } else if (command.equals("safe-search")) {
      if (path.startsWith("gs://")) {
        // TODO: See https://goo.gl/uWgYhQ
      } else {
        detectSafeSearch(path, System.out);
      }
    }
  }

  private static ImageAnnotatorClient visionApi;

  /**
   * Constructs a {@link Detect} which connects to the Cloud Vision API.
   */
  public Detect(ImageAnnotatorClient client) {
    visionApi = client;
  }

  /**
   * Helper for getting byte array given input stream.
   * @param is Input stream to get bytes for.
   * @return Byte array for the input stream contents.
   * @throws IOException
   */
  static public byte[] toByteArray(InputStream is) throws IOException {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    int nRead;
    byte[] data = new byte[16384];

    while ((nRead = is.read(data, 0, data.length)) != -1) {
      buffer.write(data, 0, nRead);
    }

    buffer.flush();

    return buffer.toByteArray();
  }
  
  /**
   * Detects faces in the specified image.
   * @param filePath The path to the file to perform face detection on.
   * @param out A {@link PrintStream} to write detected features to.
   * @throws IOException 
   */
  static public void detectFaces(String filePath, PrintStream out) throws IOException {
    List<AnnotateImageRequest> requests = new ArrayList<>();

    FileInputStream file = new FileInputStream(filePath);
    ByteString imgBytes = ByteString.copyFrom(toByteArray(file));
    
    Image img = Image.newBuilder().setContent(imgBytes).build();    
    Feature feat = Feature.newBuilder().setType(Type.FACE_DETECTION).build();
    AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
        .addFeatures(feat)
        .setImage(img)
        .build();
    requests.add(request);

    BatchAnnotateImagesResponse response = visionApi.batchAnnotateImages(requests);
    List<AnnotateImageResponse> responses = response.getResponsesList();

    for (AnnotateImageResponse res : responses) {      
      if (res.hasError()) out.printf("Error: %s\n", res.getError().getMessage());

      for (FaceAnnotation annotation : res.getFaceAnnotationsList()) {
        out.printf("anger: %s\njoy: %s\nsurprise: %s\n",
            annotation.getAngerLikelihood(),
            annotation.getJoyLikelihood(),
            annotation.getSurpriseLikelihood());
      }
    }
  }
  
  /**
   * Detects labels in the specified image.
   * @param filePath The path to the file to perform label detection on.
   * @param out A {@link PrintStream} to write detected labels to.
   * @throws IOException 
   */
  static public void detectLabels(String filePath, PrintStream out) throws IOException {
    List<AnnotateImageRequest> requests = new ArrayList<>();

    FileInputStream file = new FileInputStream(filePath);
    ByteString imgBytes = ByteString.copyFrom(toByteArray(file));
    
    Image img = Image.newBuilder().setContent(imgBytes).build();    
    Feature feat = Feature.newBuilder().setType(Type.LABEL_DETECTION).build();
    AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
        .addFeatures(feat)
        .setImage(img)
        .build();
    requests.add(request);

    BatchAnnotateImagesResponse response = visionApi.batchAnnotateImages(requests);
    List<AnnotateImageResponse> responses = response.getResponsesList();

    for (AnnotateImageResponse res : responses) {
      if (res.hasError()) out.printf("Error: %s\n", res.getError().getMessage());

      for (EntityAnnotation annotation : res.getLabelAnnotationsList()) {
        Map<FieldDescriptor, Object> fields = annotation.getAllFields();
        Iterator<Entry<FieldDescriptor, Object>> iter = fields.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<FieldDescriptor, Object> entry = iter.next();
            out.append(entry.getKey().getJsonName());
            out.append(" : ").append('"');
            out.append(entry.getValue().toString());
            out.append("\"\n");
        }
      }
    }
  }
  
  /**
   * Detects landmarks in the specified image.
   * @param filePath The path to the file to perform landmark detection on.
   * @param out A {@link PrintStream} to write detected landmarks to.
   * @throws IOException 
   */
  static public void detectLandmarks(String filePath, PrintStream out) throws IOException {
    List<AnnotateImageRequest> requests = new ArrayList<>();

    FileInputStream file = new FileInputStream(filePath);
    ByteString imgBytes = ByteString.copyFrom(toByteArray(file));
    
    Image img = Image.newBuilder().setContent(imgBytes).build();    
    Feature feat = Feature.newBuilder().setType(Type.LANDMARK_DETECTION).build();
    AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
        .addFeatures(feat)
        .setImage(img)
        .build();
    requests.add(request);

    BatchAnnotateImagesResponse response = visionApi.batchAnnotateImages(requests);
    List<AnnotateImageResponse> responses = response.getResponsesList();

    for (AnnotateImageResponse res : responses) {      
      if (res.hasError()) out.printf("Error: %s\n", res.getError().getMessage());

      for (EntityAnnotation annotation : res.getLandmarkAnnotationsList()) {
        LocationInfo info = annotation.getLocationsList().listIterator().next();
        out.printf("Landmark: %s\n %s\n", annotation.getDescription(), info.getLatLng());
      }
    }
  }
  
  /**
   * Detects logos in the specified image.
   * @param filePath The path to the file to perform logo detection on.
   * @param out A {@link PrintStream} to write detected logos to.
   * @throws IOException 
   */
  static public void detectLogos(String filePath, PrintStream out) throws IOException {
    List<AnnotateImageRequest> requests = new ArrayList<>();

    FileInputStream file = new FileInputStream(filePath);
    ByteString imgBytes = ByteString.copyFrom(toByteArray(file));
    
    Image img = Image.newBuilder().setContent(imgBytes).build();    
    Feature feat = Feature.newBuilder().setType(Type.LOGO_DETECTION).build();
    AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
        .addFeatures(feat)
        .setImage(img)
        .build();
    requests.add(request);

    BatchAnnotateImagesResponse response = visionApi.batchAnnotateImages(requests);
    List<AnnotateImageResponse> responses = response.getResponsesList();

    for (AnnotateImageResponse res : responses) {      
      if (res.hasError()) out.printf("Error: %s\n", res.getError().getMessage());

      for (EntityAnnotation annotation : res.getLogoAnnotationsList()) {
        out.println(annotation.getDescription());
      }
    }
  }
  
  /**
   * Detects text in the specified image.
   * @param filePath The path to the file to detect text in.
   * @param out A {@link PrintStream} to write the detected text to.
   * @throws IOException 
   */
  static public void detectText(String filePath, PrintStream out) throws IOException {
    List<AnnotateImageRequest> requests = new ArrayList<>();

    FileInputStream file = new FileInputStream(filePath);
    ByteString imgBytes = ByteString.copyFrom(toByteArray(file));
    
    Image img = Image.newBuilder().setContent(imgBytes).build();    
    Feature feat = Feature.newBuilder().setType(Type.TEXT_DETECTION).build();
    AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
        .addFeatures(feat)
        .setImage(img)
        .build();
    requests.add(request);

    BatchAnnotateImagesResponse response = visionApi.batchAnnotateImages(requests);
    List<AnnotateImageResponse> responses = response.getResponsesList();

    for (AnnotateImageResponse res : responses) {
      if (res.hasError()) out.printf("Error: %s\n", res.getError().getMessage());

      for (EntityAnnotation annotation : res.getTextAnnotationsList()) {
        out.printf("Text: %s\n", annotation.getDescription());
      }
    }
  }
  
  /**
   * Detects image properties such as color frequency from the specified image.
   * @param filePath The path to the file to detect properties.
   * @param out A {@link PrintStream} to write 
   * @throws IOException 
   */
  static public void detectProperties(String filePath, PrintStream out) throws IOException {
    List<AnnotateImageRequest> requests = new ArrayList<>();

    FileInputStream file = new FileInputStream(filePath);
    ByteString imgBytes = ByteString.copyFrom(toByteArray(file));
    
    Image img = Image.newBuilder().setContent(imgBytes).build();    
    Feature feat = Feature.newBuilder().setType(Type.IMAGE_PROPERTIES).build();
    AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
        .addFeatures(feat)
        .setImage(img)
        .build();
    requests.add(request);

    BatchAnnotateImagesResponse response = visionApi.batchAnnotateImages(requests);
    List<AnnotateImageResponse> responses = response.getResponsesList();

    for (AnnotateImageResponse res : responses) {      
      if (res.hasError()) out.printf("Error: %s\n", res.getError().getMessage());

      DominantColorsAnnotation colors = res.getImagePropertiesAnnotation().getDominantColors();
      for (ColorInfo color : colors.getColorsList()) {
        out.printf("fraction: %f\nr: %f, g: %f, b: %f\n", 
            color.getPixelFraction(),
            color.getColor().getRed(), 
            color.getColor().getRed(), 
            color.getColor().getRed());
      }
    }
  }
  
  /**
   * Detects whether the specified image has features you would want to moderate.
   * @param filePath The path to the file used for safe search detection.
   * @param out A {@link PrintStream} to write the results to.
   * @throws IOException 
   */
  static public void detectSafeSearch(String filePath, PrintStream out) throws IOException {
    List<AnnotateImageRequest> requests = new ArrayList<>();

    FileInputStream file = new FileInputStream(filePath);
    ByteString imgBytes = ByteString.copyFrom(toByteArray(file));
    
    Image img = Image.newBuilder().setContent(imgBytes).build();    
    Feature feat = Feature.newBuilder().setType(Type.SAFE_SEARCH_DETECTION).build();
    AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
        .addFeatures(feat)
        .setImage(img)
        .build();
    requests.add(request);

    BatchAnnotateImagesResponse response = visionApi.batchAnnotateImages(requests);
    List<AnnotateImageResponse> responses = response.getResponsesList();

    for (AnnotateImageResponse res : responses) {      
      if (res.hasError()) out.printf("Error: %s\n", res.getError().getMessage());

      SafeSearchAnnotation annotation = res.getSafeSearchAnnotation();
      out.printf("adult: %s\nmedical: %s\nspoofed: %s\nviolence: %s\n",
          annotation.getAdult(),
          annotation.getMedical(),
          annotation.getSpoof(),
          annotation.getViolence());
    }
  }
}
