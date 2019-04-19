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

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.List;
import java.util.Objects;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
// import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.Query.Direction;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
// import com.google.cloud.firestore.WriteResult;

@SuppressWarnings("JavadocMethod")
public class Guestbook {

  // private static final KeyFactory keyFactory = getKeyFactory(Guestbook.class);
  private final DocumentReference bookRef;

  public final String book;

  public Guestbook(String book) {
    this.book = book == null ? "default" : book;
    // Create the Guestbook document.
    bookRef = getFirestore().collection("Guestbooks").document(this.book);
    // key = keyFactory.newKey(this.book);
  }

  public DocumentReference getBookRef() {
    return bookRef;
  }

  public List<Greeting> getGreetings() {
    ImmutableList.Builder<Greeting> greetingList = new ImmutableList.Builder<Greeting>();
    ApiFuture<QuerySnapshot> query = bookRef.collection("Greetings").orderBy("date", Direction.DESCENDING).get();
    System.out.println("Query");
    try {
      QuerySnapshot querySnapshot = query.get();
      List<QueryDocumentSnapshot> greetings = querySnapshot.getDocuments();
      for (QueryDocumentSnapshot greeting : greetings) {
        System.out.println(greeting.getId());
        Greeting newGreeting = greeting.toObject(Greeting.class);
        System.out.println(newGreeting);
        greetingList.add(greeting.toObject(Greeting.class));
      }
    }
    catch(Exception InterruptedException) {
      System.out.println("Nothing");
    }

    return greetingList.build();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Guestbook)) {
      return false;
    }
    Guestbook guestbook = (Guestbook) obj;
    return Objects.equals(book, guestbook.book)
        && Objects.equals(bookRef, guestbook.bookRef);
  }

  @Override
  public int hashCode() {
    return Objects.hash(book, bookRef);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("book", book)
        .add("bookRef", bookRef)
        .toString();
  }
}
