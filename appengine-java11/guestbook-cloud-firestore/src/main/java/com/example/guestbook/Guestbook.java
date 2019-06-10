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

import static com.example.guestbook.Persistence.getFirestore;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Query.Direction;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Represents a guestbook: book name, book document reference. */
@SuppressWarnings("JavadocMethod")
public class Guestbook {

  private static final long TIMEOUT_SECONDS = 5;

  private final DocumentReference bookRef;

  public final String book;

  public Guestbook(String book) {
    this.book = book == null ? "default" : book;
    // Construct the Guestbook data.
    Map<String, Object> bookData = new HashMap<>();
    bookData.put("name", this.book);
    // [START gae_java11_firestore_book]
    // The Guestbook document reference in the collection.
    bookRef = getFirestore().collection("Guestbooks").document(this.book);
    // Add the Guestbook to collection.
    bookRef.set(bookData);
    // [END gae_java11_firestore_book]
  }

  /** Get the Firestore reference to the Guestbook document **/
  public DocumentReference getBookRef() {
    return bookRef;
  }

  // [START gae_java11_firestore_get_greetings]
  /** Query Firstore for Guestbook greetings */
  public List<Greeting> getGreetings() {
    // Initialize a List for Greetings.
    ImmutableList.Builder<Greeting> greetings = new ImmutableList.Builder<Greeting>();
    // Construct query.
    ApiFuture<QuerySnapshot> query =
        bookRef.collection("Greetings").orderBy("date", Direction.DESCENDING).get();

    try {
      // Get query documents.
      QuerySnapshot querySnapshot = query.get();
      for (QueryDocumentSnapshot greeting : querySnapshot.getDocuments()) {
        greetings.add(greeting.toObject(Greeting.class));
      }
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }

    return greetings.build();
  }
  // [END gae_java11_firestore_get_greetings]

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Guestbook)) {
      return false;
    }
    Guestbook guestbook = (Guestbook) obj;
    return Objects.equals(book, guestbook.book) && Objects.equals(bookRef, guestbook.bookRef);
  }

  @Override
  public int hashCode() {
    return Objects.hash(book, bookRef);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("book", book).add("bookRef", bookRef).toString();
  }
}
