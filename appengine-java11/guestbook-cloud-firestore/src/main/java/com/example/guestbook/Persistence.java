/*
 * Copyright 2019 Google LLC
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

package com.example.guestbook;

// [START gae_java11_firestore_dependencies]
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
// [END gae_java11_firestore_dependencies]

/** Create a persistence connection to your Firestore instance. */
public class Persistence {

  private static Firestore firestore;

  @SuppressWarnings("JavadocMethod")
  public static Firestore getFirestore() {
    if (firestore == null) {
      // Authorized Firestore service
      // [START gae_java11_firestore]
      Firestore db =
          FirestoreOptions.newBuilder().setProjectId("YOUR-PROJECT-ID").build().getService();
      // [END gae_java11_firestore]
      firestore = db;
    }

    return firestore;
  }

  public static void setFirestore(Firestore firestore) {
    Persistence.firestore = firestore;
  }
}
