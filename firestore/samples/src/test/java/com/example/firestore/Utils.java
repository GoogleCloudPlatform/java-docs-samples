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

package com.example.firestore;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.WriteResult;
import java.util.HashMap;
import java.util.Map;

public class Utils {
  public static void createCollection(
      String projectId, String collectionName, String documentName, String name) throws Exception {
    // Create collection and document to be deleted
    FirestoreOptions firestoreOptions =
        FirestoreOptions.getDefaultInstance().toBuilder().setProjectId(projectId).build();
    try (Firestore db = firestoreOptions.getService()) {

      Map<String, Object> docData = new HashMap<>();
      docData.put("name", name);
      ApiFuture<WriteResult> future =
          db.collection(collectionName).document(documentName).set(docData);
      future.get();
    }
  }
}
