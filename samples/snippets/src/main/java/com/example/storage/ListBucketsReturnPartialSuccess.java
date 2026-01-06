package com.example.storage;

import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.util.Iterator;

public class ListBucketsReturnPartialSuccess {

  public static void listBucketsReturnPartialSuccess() {
    // [START storage_buckets_list_with_return_partial_success]
    // The ID of your project
    String projectId = "your-project-id";

    Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
    Iterator<Bucket> buckets = storage.listBuckets(Storage.BucketListOption.returnPartialSuccess()).iterateAll();

    buckets.forEachRemaining(
        bucket -> {
          System.out.println(bucket.getName() + " isUnreachable: " + bucket.isUnreachable());
        });
    // [END storage_buckets_list_with_return_partial_success]
  }
}