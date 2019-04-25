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

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import java.util.concurrent.atomic.AtomicReference;

/** Create a persistence connection to your Firestore instance. */
public class Persistence {

  private static AtomicReference<Firestore> firestore = new AtomicReference<>();

  @SuppressWarnings("JavadocMethod")
  public static Firestore getFirestore() {
    if (firestore.get() == null) {
      // Authorized Firestore service
      firestore.set(
          FirestoreOptions.newBuilder().setProjectId("YOUR-PROJECT-ID").build().getService());
    }

    return firestore.get();
  }

  public static void setFirestore(Firestore firestore) {
    Persistence.firestore.set(firestore);
  }
}
