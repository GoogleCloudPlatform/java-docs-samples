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

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.RewriteResponse;
import java.io.IOException;
import java.io.InputStream;

/**
 * Demonstrates the use of GCS's CSEK features via the Java API client library
 *
 *  This program demonstrates some quick, basic examples of using GCS's CSEK functionality.
 *
 *  <p>When run, it begins by uploading an object named "encrypted_file.txt" to the specified bucket
 *  that will be protected with a provided CSEK.</p>
 *
 *  <p>Next, it will fetch that object by providing that same CSEK to GCS.</p>
 *
 *  <p>Finally, it will rotate that key to a new value.</p>
 **/
class CustomerSuppliedEncryptionKeysSamples {

  // You can (and should) generate your own CSEK Key! Try running this from the command line:
  //    python -c 'import base64; import os; print(base64.encodestring(os.urandom(32)))'
  // Also, these encryption keys are included here for simplicity, but please remember that
  // private keys should not be stored in source code.
  private static final String CSEK_KEY = "4RzDI0TeWa9M/nAvYH05qbCskPaSU/CFV5HeCxk0IUA=";

  // You can use openssl to quickly calculate the hash of your key. Try running this:
  //    openssl base64 -d <<< YOUR_KEY_FROM_ABOVE | openssl dgst -sha256 -binary | openssl base64
  private static final String CSEK_KEY_HASH = "aanjNC2nwso8e2FqcWILC3/Tt1YumvIwEj34kr6PRpI=";

  // Used for the key rotation example
  private static final String ANOTHER_CESK_KEY = "oevtavYZC+TfGtV86kJBKTeytXAm1s2r3xIqam+QPKM=";
  private static final String ANOTHER_CSEK_KEY_HASH =
      "/gd0N3k3MK0SEDxnUiaswl0FFv6+5PHpo+5KD5SBCeA=";

  private static final String OBJECT_NAME = "encrypted_file.txt";

  /**
   * Downloads a CSEK-protected object from GCS. The download may continue in the background after
   * this method returns. The caller of this method is responsible for closing the input stream.
   *
   * @param storage A Storage object, ready for use
   * @param bucketName The name of the destination bucket
   * @param objectName The name of the destination object
   * @param base64CseKey An AES256 key, encoded as a base64 string.
   * @param base64CseKeyHash The SHA-256 hash of the above key, also encoded as a base64 string.
   *
   * @return An InputStream that contains the decrypted contents of the object.
   *
   * @throws IOException if there was some error download from GCS.
   */
  public static InputStream downloadObject(
      Storage storage,
      String bucketName,
      String objectName,
      String base64CseKey,
      String base64CseKeyHash)
      throws Exception {

    // Set the CSEK headers
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("x-goog-encryption-algorithm", "AES256");
    httpHeaders.set("x-goog-encryption-key", base64CseKey);
    httpHeaders.set("x-goog-encryption-key-sha256", base64CseKeyHash);

    Storage.Objects.Get getObject = storage.objects().get(bucketName, objectName);

    // If you're using AppEngine, turn off setDirectDownloadEnabled:
    //      getObject.getMediaHttpDownloader().setDirectDownloadEnabled(false);

    getObject.setRequestHeaders(httpHeaders);

    try {
      return getObject.executeMediaAsInputStream();
    } catch (GoogleJsonResponseException e) {
      System.out.println("Error downloading: " + e.getContent());
      System.exit(1);
      return null;
    }
  }

  /**
   * Uploads an object to GCS, to be stored with a customer-supplied key (CSEK). The upload may
   * continue in the background after this method returns. The caller of this method is responsible
   * for closing the input stream.
   *
   * @param storage A Storage object, ready for use
   * @param bucketName The name of the destination bucket
   * @param objectName The name of the destination object
   * @param data An InputStream containing the contents of the object to upload
   * @param base64CseKey An AES256 key, encoded as a base64 string.
   * @param base64CseKeyHash The SHA-256 hash of the above key, also encoded as a base64 string.
   * @throws IOException if there was some error uploading to GCS.
   */
  public static void uploadObject(
      Storage storage,
      String bucketName,
      String objectName,
      InputStream data,
      String base64CseKey,
      String base64CseKeyHash)
      throws IOException {
    InputStreamContent mediaContent = new InputStreamContent("text/plain", data);
    Storage.Objects.Insert insertObject =
        storage.objects().insert(bucketName, null, mediaContent).setName(objectName);
    // The client library's default gzip setting may cause objects to be stored with gzip encoding,
    // which can be desirable in some circumstances but has some disadvantages as well, such as
    // making it difficult to read only a certain range of the original object.
    insertObject.getMediaHttpUploader().setDisableGZipContent(true);

    // Now set the CSEK headers
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("x-goog-encryption-algorithm", "AES256");
    httpHeaders.set("x-goog-encryption-key", base64CseKey);
    httpHeaders.set("x-goog-encryption-key-sha256", base64CseKeyHash);

    insertObject.setRequestHeaders(httpHeaders);

    try {
      insertObject.execute();
    } catch (GoogleJsonResponseException e) {
      System.out.println("Error uploading: " + e.getContent());
      System.exit(1);
    }
  }

  /**
   * Given an existing, CSEK-protected object, changes the key used to store that object.
   *
   * @param storage A Storage object, ready for use
   * @param bucketName The name of the destination bucket
   * @param objectName The name of the destination object
   * @param originalBase64Key The AES256 key currently associated with this object,
   *     encoded as a base64 string.
   * @param originalBase64KeyHash The SHA-256 hash of the above key,
   *     also encoded as a base64 string.
   * @param newBase64Key An AES256 key which will replace the existing key,
   *     encoded as a base64 string.
   * @param newBase64KeyHash The SHA-256 hash of the above key, also encoded as a base64 string.
   * @throws IOException if there was some error download from GCS.
   */
  public static void rotateKey(
      Storage storage,
      String bucketName,
      String objectName,
      String originalBase64Key,
      String originalBase64KeyHash,
      String newBase64Key,
      String newBase64KeyHash)
      throws Exception {

    // Set the CSEK headers
    final HttpHeaders httpHeaders = new HttpHeaders();

    // Specify the exiting object's current CSEK.
    httpHeaders.set("x-goog-copy-source-encryption-algorithm", "AES256");
    httpHeaders.set("x-goog-copy-source-encryption-key", originalBase64Key);
    httpHeaders.set("x-goog-copy-source-encryption-key-sha256", originalBase64KeyHash);

    // Specify the new CSEK that we would like to apply.
    httpHeaders.set("x-goog-encryption-algorithm", "AES256");
    httpHeaders.set("x-goog-encryption-key", newBase64Key);
    httpHeaders.set("x-goog-encryption-key-sha256", newBase64KeyHash);

    Storage.Objects.Rewrite rewriteObject =
        storage.objects().rewrite(bucketName, objectName, bucketName, objectName, null);

    rewriteObject.setRequestHeaders(httpHeaders);

    try {
      RewriteResponse rewriteResponse = rewriteObject.execute();

      // If an object is very large, you may need to continue making successive calls to
      // rewrite until the operation completes.
      while (!rewriteResponse.getDone()) {
        System.out.println("Rewrite did not complete. Resuming...");
        rewriteObject.setRewriteToken(rewriteResponse.getRewriteToken());
        rewriteResponse = rewriteObject.execute();
      }
    } catch (GoogleJsonResponseException e) {
      System.out.println("Error rotating key: " + e.getContent());
      System.exit(1);
    }
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.out.println("\nPlease run this with one argument: "
          + "the GCS bucket into which this program should upload an object.\n\n"
          + "You can create a bucket using gsutil like this:\n\n\t"
          + "gsutil mb gs://name-of-bucket\n\n");
      System.exit(1);
    }
    String bucketName = args[0];

    Storage storage = StorageFactory.getService();
    InputStream dataToUpload = new StorageUtils.ArbitrarilyLargeInputStream(10000000);

    System.out.format("Uploading object gs://%s/%s using CSEK.\n", bucketName, OBJECT_NAME);
    uploadObject(storage, bucketName, OBJECT_NAME, dataToUpload, CSEK_KEY, CSEK_KEY_HASH);

    System.out.format("Downloading object gs://%s/%s using CSEK.\n", bucketName, OBJECT_NAME);
    InputStream objectData =
        downloadObject(storage, bucketName, OBJECT_NAME, CSEK_KEY, CSEK_KEY_HASH);
    StorageUtils.readStream(objectData);

    System.out.println("Rotating object to use a different CSEK.");
    rotateKey(storage, bucketName, OBJECT_NAME, CSEK_KEY, CSEK_KEY_HASH,
        ANOTHER_CESK_KEY, ANOTHER_CSEK_KEY_HASH);

    System.out.println("Done");
  }

}
