/*
 * Copyright 2019 Google Inc.
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

package com.example.vision;

import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gcp.vision.CloudVisionTemplate;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Code sample demonstrating Cloud Vision usage within the context of Spring Framework using Spring
 * Cloud GCP libraries. The sample is written as a Spring Boot application to demonstrate a
 * practical application of this usage.
 */
@RestController
public class VisionController {

  @Autowired private ResourceLoader resourceLoader;

  // [START vision_spring_autowire]
  @Autowired private CloudVisionTemplate cloudVisionTemplate;
  // [END vision_spring_autowire]

  private static final String ALLOWED_IMAGE_HOST = "storage.googleapis.com";

  /**
   * Restrict user-supplied image URLs to HTTPS URLs on {@link #ALLOWED_IMAGE_HOST}. Passing an
   * arbitrary URI to {@link ResourceLoader#getResource(String)} allows callers to make the server
   * open local files ({@code file:}) or issue SSRF requests to internal endpoints.
   */
  static void requireAllowedImageUrl(String imageUrl) {
    if (imageUrl == null || imageUrl.trim().isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "imageUrl is required");
    }
    URI uri;
    try {
      uri = new URI(imageUrl);
    } catch (URISyntaxException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid imageUrl", e);
    }
    String host = uri.getHost();
    String rawPath = uri.getRawPath();
    String path = uri.getPath();
    int port = uri.getPort();
    if (!"https".equalsIgnoreCase(uri.getScheme())
        || host == null
        || !host.chars().allMatch(c -> c < 0x80)
        || !ALLOWED_IMAGE_HOST.equalsIgnoreCase(host)
        || (port != -1 && port != 443)
        || uri.getUserInfo() != null
        || rawPath == null
        || rawPath.length() <= 1
        || !rawPath.startsWith("/")
        || rawPath.contains("..")
        || path == null
        || path.length() <= 1
        || !path.startsWith("/")
        || path.contains("..")) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "imageUrl must be an https://" + ALLOWED_IMAGE_HOST + "/... URL");
    }
  }

  /**
   * This method downloads an image from a URL and sends its contents to the Vision API for label
   * detection.
   *
   * @param imageUrl the URL of the image
   * @param map the model map to use
   * @return a string with the list of labels and percentage of certainty
   */
  @GetMapping("/extractLabels")
  public ModelAndView extractLabels(String imageUrl, ModelMap map) {
    requireAllowedImageUrl(imageUrl);
    // [START vision_spring_image_labelling]
    AnnotateImageResponse response =
        this.cloudVisionTemplate.analyzeImage(
            this.resourceLoader.getResource(imageUrl), Type.LABEL_DETECTION);

    Map<String, Float> imageLabels =
        response.getLabelAnnotationsList().stream()
            .collect(
                Collectors.toMap(
                    EntityAnnotation::getDescription,
                    EntityAnnotation::getScore,
                    (u, v) -> {
                      throw new IllegalStateException(String.format("Duplicate key %s", u));
                    },
                    LinkedHashMap::new));
    // [END vision_spring_image_labelling]

    map.addAttribute("annotations", imageLabels);
    map.addAttribute("imageUrl", imageUrl);

    return new ModelAndView("result", map);
  }

  @GetMapping("/extractText")
  public String extractText(String imageUrl) {
    requireAllowedImageUrl(imageUrl);
    // [START vision_spring_text_extraction]
    String textFromImage =
        this.cloudVisionTemplate.extractTextFromImage(this.resourceLoader.getResource(imageUrl));
    return "Text from image: " + textFromImage;
    // [END vision_spring_text_extraction]
  }
}
