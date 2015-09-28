//[START all]
/*
 * Copyright (c) 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.StorageScopes;
import com.google.api.services.storage.model.Bucket;
import com.google.api.services.storage.model.ObjectAccessControl;
import com.google.api.services.storage.model.Objects;
import com.google.api.services.storage.model.StorageObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Main class for the Cloud Storage JSON API sample.
 *
 * Demonstrates how to make an authenticated API call using the Google Cloud Storage API client
 * library for java, with Application Default Credentials.
 */
public class StorageSample {

  /**
   * Be sure to specify the name of your application. If the application name is {@code null} or
   * blank, the application will log a warning. Suggested format is "MyCompany-ProductName/1.0".
   */
  private static final String APPLICATION_NAME = "[[INSERT_YOUR_APP_NAME_HERE]]";

  /** Global instance of the JSON factory. */
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  private static final String TEST_FILENAME = "json-test.txt";

  // [START get_service]
  private static Storage storageService;

  /**
   * Returns an authenticated Storage object used to make service calls to Cloud Storage.
   */
  private static Storage getService() throws IOException, GeneralSecurityException {
    if (null == storageService) {
      GoogleCredential credential = GoogleCredential.getApplicationDefault();
      // Depending on the environment that provides the default credentials (e.g. Compute Engine,
      // App Engine), the credentials may require us to specify the scopes we need explicitly.
      // Check for this case, and inject the Cloud Storage scope if required.
      if (credential.createScopedRequired()) {
        credential = credential.createScoped(StorageScopes.all());
      }
      HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
      storageService = new Storage.Builder(httpTransport, JSON_FACTORY, credential)
          .setApplicationName(APPLICATION_NAME).build();
    }
    return storageService;
  }
  // [END get_service]

  // [START list_bucket]
  /**
   * Fetch a list of the objects within the given bucket.
   *
   * @param bucketName the name of the bucket to list.
   * @return a list of the contents of the specified bucket.
   */
  public static List<StorageObject> listBucket(String bucketName)
      throws IOException, GeneralSecurityException {
    Storage client = getService();
    Storage.Objects.List listRequest = client.objects().list(bucketName);

    List<StorageObject> results = new ArrayList<StorageObject>();
    Objects objects;

    // Iterate through each page of results, and add them to our results list.
    do {
      objects = listRequest.execute();
      // Add the items in this page of results to the list we'll return.
      results.addAll(objects.getItems());

      // Get the next page, in the next iteration of this loop.
      listRequest.setPageToken(objects.getNextPageToken());
    } while (null != objects.getNextPageToken());

    return results;
  }
  // [END list_bucket]

  // [START get_bucket]
  /**
   * Fetches the metadata for the given bucket.
   *
   * @param bucketName the name of the bucket to get metadata about.
   * @return a Bucket containing the bucket's metadata.
   */
  public static Bucket getBucket(String bucketName) throws IOException, GeneralSecurityException {
    Storage client = getService();

    Storage.Buckets.Get bucketRequest = client.buckets().get(bucketName);
    // Fetch the full set of the bucket's properties (e.g. include the ACLs in the response)
    bucketRequest.setProjection("full");
    return bucketRequest.execute();
  }
  // [END get_bucket]

  // [START upload_stream]
  /**
   * Uploads data to an object in a bucket.
   *
   * @param name the name of the destination object.
   * @param contentType the MIME type of the data.
   * @param stream the data - for instance, you can use a FileInputStream to upload a file.
   * @param bucketName the name of the bucket to create the object in.
   */
  public static void uploadStream(
      String name, String contentType, InputStream stream, String bucketName)
      throws IOException, GeneralSecurityException {
    InputStreamContent contentStream = new InputStreamContent(contentType, stream);
    StorageObject objectMetadata = new StorageObject()
        // Set the destination object name
        .setName(name)
        // Set the access control list to publicly read-only
        .setAcl(Arrays.asList(
            new ObjectAccessControl().setEntity("allUsers").setRole("READER")));

    // Do the insert
    Storage client = getService();
    Storage.Objects.Insert insertRequest = client.objects().insert(
        bucketName, objectMetadata, contentStream);

    insertRequest.execute();
  }
  // [END upload_stream]

  // [START delete_object]
  /**
   * Deletes an object in a bucket.
   *
   * @param path the path to the object to delete.
   * @param bucketName the bucket the object is contained in.
   */
  public static void deleteObject(String path, String bucketName)
      throws IOException, GeneralSecurityException {
    Storage client = getService();
    client.objects().delete(bucketName, path).execute();
  }
  // [END delete_object]

  /**
   * Exercises the class's functions - gets and lists a bucket, uploads and deletes an object.
   *
   * @param args the command-line arguments. The first argument should be the bucket name.
   */
  public static void main(String[] args) {
    if (args.length < 1) {
      System.out.println("Usage: StorageSample <bucket-name>");
      System.exit(1);
    }

    String bucketName = args[0];

    try {
      // Get metadata about the specified bucket.
      Bucket bucket = getBucket(bucketName);
      System.out.println("name: " + bucketName);
      System.out.println("location: " + bucket.getLocation());
      System.out.println("timeCreated: " + bucket.getTimeCreated());
      System.out.println("owner: " + bucket.getOwner());


      // List the contents of the bucket.
      List<StorageObject> bucketContents = listBucket(bucketName);
      if (null == bucketContents) {
        System.out.println(
            "There were no objects in the given bucket; try adding some and re-running.");
      }
      for (StorageObject object : bucketContents) {
        System.out.println(object.getName() + " (" + object.getSize() + " bytes)");
      }


      // Upload a stream to the bucket. This could very well be a file.
      uploadStream(
          TEST_FILENAME, "text/plain",
          new ByteArrayInputStream("Test of json storage sample".getBytes()),
          bucketName);

      // Now delete the file
      deleteObject(TEST_FILENAME, bucketName);

    } catch (IOException e) {
      System.err.println(e.getMessage());
      System.exit(1);
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }
}
//[END all]
