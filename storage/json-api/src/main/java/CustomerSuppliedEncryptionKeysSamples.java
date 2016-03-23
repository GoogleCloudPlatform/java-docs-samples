import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.StorageScopes;
import com.google.api.services.storage.model.RewriteResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;

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

  private static final java.io.File DATA_STORE_DIR =
      new java.io.File(System.getProperty("user.home"), ".store/storage_sample");

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
   * @param base64CSEKey An AES256 key, encoded as a base64 string.
   * @param base64CSEKeyHash The SHA-256 hash of the above key, also encoded as a base64 string.
   * @throws IOException if there was some error download from GCS.
   *
   * @return An InputStream that contains the decrypted contents of the object.
   */
  public static InputStream downloadObject(
      Storage storage,
      String bucketName,
      String objectName,
      String base64CSEKey,
      String base64CSEKeyHash)
      throws Exception {
    Storage.Objects.Get getObject = storage.objects().get(bucketName, objectName);

    // If you're using AppEngine, turn off setDirectDownloadEnabled:
    //      getObject.getMediaHttpDownloader().setDirectDownloadEnabled(false);

    // Now set the CSEK headers
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("x-goog-encryption-algorithm", "AES256");
    httpHeaders.set("x-goog-encryption-key", base64CSEKey);
    httpHeaders.set("x-goog-encryption-key-sha256", base64CSEKeyHash);

    // Since our request includes our private key as a header, it is a good idea to instruct caches
    // and proxies not to store this request.
    httpHeaders.setCacheControl("no-store");

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
   * @param base64CSEKey An AES256 key, encoded as a base64 string.
   * @param base64CSEKeyHash The SHA-256 hash of the above key, also encoded as a base64 string.
   * @throws IOException if there was some error uploading to GCS.
   */
  public static void uploadObject(
      Storage storage,
      String bucketName,
      String objectName,
      InputStream data,
      String base64CSEKey,
      String base64CSEKeyHash)
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
    httpHeaders.set("x-goog-encryption-key", base64CSEKey);
    httpHeaders.set("x-goog-encryption-key-sha256", base64CSEKeyHash);
    
    // Since our request includes our private key as a header, it is a good idea to instruct caches
    // and proxies not to store this request.
    httpHeaders.setCacheControl("no-store");
    
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
    Storage.Objects.Rewrite rewriteObject =
        storage.objects().rewrite(bucketName, objectName, bucketName, objectName, null);

    // Now set the CSEK headers
    final HttpHeaders httpHeaders = new HttpHeaders();

    // Specify the exiting object's current CSEK.
    httpHeaders.set("x-goog-copy-source-encryption-algorithm", "AES256");
    httpHeaders.set("x-goog-copy-source-encryption-key", originalBase64Key);
    httpHeaders.set("x-goog-copy-source-encryption-key-sha256", originalBase64KeyHash);

    // Specify the new CSEK that we would like to apply.
    httpHeaders.set("x-goog-encryption-algorithm", "AES256");
    httpHeaders.set("x-goog-encryption-key", newBase64Key);
    httpHeaders.set("x-goog-encryption-key-sha256", newBase64KeyHash);
    
    // Since our request includes our private key as a header, it is a good idea to instruct caches
    // and proxies not to store this request.
    httpHeaders.setCacheControl("no-store");
    
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
    // CSEK, like the JSON API, may be used only via HTTPS.
    HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    DataStoreFactory dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    Credential credential = authorize(jsonFactory, httpTransport, dataStoreFactory);
    Storage storage =
        new Storage.Builder(httpTransport, jsonFactory, credential)
            .setApplicationName("JavaCSEKApiSample")
            .build();

    InputStream dataToUpload = new ArbitrarilyLargeInputStream(10000000);

    System.out.format("Uploading object gs://%s/%s using CSEK.\n", bucketName, OBJECT_NAME);
    uploadObject(storage, bucketName, OBJECT_NAME, dataToUpload, CSEK_KEY, CSEK_KEY_HASH);
    System.out.format("Downloading object gs://%s/%s using CSEK.\n", bucketName, OBJECT_NAME);
    InputStream objectData =
        downloadObject(storage, bucketName, OBJECT_NAME, CSEK_KEY, CSEK_KEY_HASH);
    readStream(objectData);
    System.out.println("Rotating object to use a different CSEK.");
    rotateKey(storage, bucketName, OBJECT_NAME, CSEK_KEY, CSEK_KEY_HASH,
        ANOTHER_CESK_KEY, ANOTHER_CSEK_KEY_HASH);

    System.out.println();
  }

  private static Credential authorize(
      JsonFactory jsonFactory, HttpTransport httpTransport, DataStoreFactory dataStoreFactory)
      throws Exception {

    InputStream clientSecretStream =
        CustomerSuppliedEncryptionKeysSamples.class
            .getResourceAsStream("client_secrets.json");
    if (clientSecretStream == null) {
      throw new RuntimeException("Could not load secrets");
    }

    // Load client secrets
    GoogleClientSecrets clientSecrets =
        GoogleClientSecrets.load(jsonFactory, new InputStreamReader(clientSecretStream));

    // Set up authorization code flow
    GoogleAuthorizationCodeFlow flow =
        new GoogleAuthorizationCodeFlow.Builder(
                httpTransport,
                jsonFactory,
                clientSecrets,
                Collections.singleton(StorageScopes.DEVSTORAGE_FULL_CONTROL))
            .setDataStoreFactory(dataStoreFactory)
            .build();

    // Authorize
    Credential credential =
        new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");

    return credential;
  }

  /**
   * Reads the contents of an InputStream and does nothing with it.
   */
  private static void readStream(InputStream is) throws IOException {
    byte inputBuffer[] = new byte[256];
    while (is.read(inputBuffer) != -1) {}
    // The caller is responsible for closing this InputStream.
    is.close();
  }

  /**
   * A helper class to provide input streams of any size.
   * The input streams will be full of null bytes.
   */
  static class ArbitrarilyLargeInputStream extends InputStream {

    private long bytesRead;
    private final long streamSize;

    public ArbitrarilyLargeInputStream(long streamSizeInBytes) {
      bytesRead = 0;
      this.streamSize = streamSizeInBytes;
    }

    @Override
    public int read() throws IOException {
      if (bytesRead >= streamSize) {
        return -1;
      }
      bytesRead++;
      return 0;
    }
  }

}
