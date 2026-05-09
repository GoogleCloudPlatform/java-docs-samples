/*
 * Copyright 2026 Google LLC
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

package com.example.firebase;

// [START firestore_data_delete_collection]

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import java.util.concurrent.ExecutionException;

public class DeleteCollection {

  /** Delete a collection and all its subcollections. */
  public static void deleteCollection(String projectId, String collectionName)
      throws ExecutionException, InterruptedException {
    FirestoreOptions firestoreOptions =
        FirestoreOptions.getDefaultInstance().toBuilder().setProjectId(projectId).build();
    Firestore db = firestoreOptions.getService();
    CollectionReference collection = db.collection(collectionName);

    ApiFuture<Void> future = db.recursiveDelete(collection);

    future.get();
    System.out.println("Collection and all its subcollections deleted successfully.");
  }

  public static void main(String[] args) throws ExecutionException, InterruptedException {
    String projectId = "your-project-id";
    String collectionName = "your-collection-name";

    deleteCollection(projectId, collectionName);
  }
}
// [END firestore_data_delete_collection]
