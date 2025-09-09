/*
 * Copyright 2015 Google LLC
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

package com.example.appengine.images;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.api.images.Transform;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

// [START gae_java21_images_example]
@SuppressWarnings("serial")
// With @WebServlet annotation the webapp/WEB-INF/web.xml is no longer required.
@WebServlet(
    name = "images",
    description = "Images: Write an image to a bucket and display it in various sizes",
    urlPatterns = "/images")
public class ImagesServlet extends HttpServlet {
  final String bucket = "YOUR-BUCKETNAME-HERE";

  // [START gae_java21_images_gcs]
  private final GcsService gcsService =
      GcsServiceFactory.createGcsService(
          new RetryParams.Builder()
              .initialRetryDelayMillis(10)
              .retryMaxAttempts(10)
              .totalRetryPeriodMillis(15000)
              .build());
  // [END gae_java21_images_gcs]

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

    // [START gae_java21_images_original_image]
    // Read the image.jpg resource into a ByteBuffer.
    FileInputStream fileInputStream = new FileInputStream(new File("WEB-INF/image.jpg"));
    FileChannel fileChannel = fileInputStream.getChannel();
    ByteBuffer byteBuffer = ByteBuffer.allocate((int) fileChannel.size());
    fileChannel.read(byteBuffer);

    byte[] imageBytes = byteBuffer.array();

    // Write the original image to Cloud Storage
    gcsService.createOrReplace(
        new GcsFilename(bucket, "image.jpeg"),
        new GcsFileOptions.Builder().mimeType("image/jpeg").build(),
        ByteBuffer.wrap(imageBytes));
    // [END gae_java21_images_original_image]

    // [START gae_java21_images_resize]
    // Get an instance of the imagesService we can use to transform images.
    ImagesService imagesService = ImagesServiceFactory.getImagesService();

    // Make an image directly from a byte array, and transform it.
    Image image = ImagesServiceFactory.makeImage(imageBytes);
    Transform resize = ImagesServiceFactory.makeResize(100, 50);
    Image resizedImage = imagesService.applyTransform(resize, image);

    // Write the transformed image back to a Cloud Storage object.
    gcsService.createOrReplace(
        new GcsFilename(bucket, "resizedImage.jpeg"),
        new GcsFileOptions.Builder().mimeType("image/jpeg").build(),
        ByteBuffer.wrap(resizedImage.getImageData()));
    // [END gae_java21_images_resize]

    // [START gae_java21_images_rotate]
    // Make an image from a Cloud Storage object, and transform it.
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    BlobKey blobKey = blobstoreService.createGsBlobKey("/gs/" + bucket + "/image.jpeg");
    Image blobImage = ImagesServiceFactory.makeImageFromBlob(blobKey);
    Transform rotate = ImagesServiceFactory.makeRotate(90);
    Image rotatedImage = imagesService.applyTransform(rotate, blobImage);

    // Write the transformed image back to a Cloud Storage object.
    gcsService.createOrReplace(
        new GcsFilename(bucket, "rotatedImage.jpeg"),
        new GcsFileOptions.Builder().mimeType("image/jpeg").build(),
        ByteBuffer.wrap(rotatedImage.getImageData()));
    // [END gae_java21_images_rotate]

    // [START gae_java21_images_servingUrl]
    // Create a fixed dedicated URL that points to the GCS hosted file
    ServingUrlOptions options =
        ServingUrlOptions.Builder.withGoogleStorageFileName("/gs/" + bucket + "/image.jpeg")
            .imageSize(150)
            .crop(true)
            .secureUrl(true);
    String url = imagesService.getServingUrl(options);
    // [END gae_java21_images_servingUrl]

    // Output some simple HTML to display the images we wrote to Cloud Storage
    // in the browser.
    PrintWriter out = resp.getWriter();
    out.println("<html><body>\n");
    out.println(
        "<img src='//storage.cloud.google.com/" + bucket + "/image.jpeg' alt='AppEngine logo' />");
    out.println(
        "<img src='//storage.cloud.google.com/"
            + bucket
            + "/resizedImage.jpeg' alt='AppEngine logo resized' />");
    out.println(
        "<img src='//storage.cloud.google.com/"
            + bucket
            + "/rotatedImage.jpeg' alt='AppEngine logo rotated' />");
    out.println("<img src='" + url + "' alt='Hosted logo' />");
    out.println("</body></html>\n");
  }
}
// [END gae_java21_images_example]
