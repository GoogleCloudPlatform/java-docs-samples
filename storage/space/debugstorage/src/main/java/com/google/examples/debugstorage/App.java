package com.google.examples.debugstorage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Random;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

public class App {
  
  public static void main(String[] args) throws IOException {
    Storage storage = StorageOptions.getDefaultInstance().getService();
    Random random = new SecureRandom();
    String bucketName = "testbucket" + random.nextInt();
    Bucket bucket = storage.create(BucketInfo.of(bucketName));
    
    System.out.println("Created bucket " + bucketName);
    
    BlobId blobId = BlobId.of(bucketName, "blob name");
    BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("text/plain").build();
    Blob blob = storage.create(blobInfo, "Hello, Cloud Storage!\r\n".getBytes(StandardCharsets.UTF_8));
    
    com.google.api.gax.paging.Page<Blob> blobs = bucket.list();
    for (Blob file : blobs.iterateAll()) {
      System.out.println(file.getName());
      System.out.println(file.getSelfLink());
    }
    
    Path path = Files.createTempFile("test", "txt");
    System.out.println("Downloading to " + path);
    blob.downloadTo(path);
    System.out.println("Downloaded to " + path);

  }
}
