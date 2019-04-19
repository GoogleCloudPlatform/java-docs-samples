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

import com.google.cloud.Timestamp;
import com.google.common.base.MoreObjects;
import java.util.Date;
import java.util.Objects;

import com.google.cloud.firestore.DocumentReference;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.Query.Direction;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("JavadocMethod")
public class Greeting {

  private Guestbook book;

  public String id;
  public String authorName;
  public String content;
  public Date date;

  public Greeting() {
    date = new Date();
  }

  public Greeting(String book, String content) {
    this();
    this.book = new Guestbook(book);
    this.content = content;
  }

  public Greeting(String book, String content, String name) {
    this(book, content);
    authorName = name;
  }

  // public Greeting(DocumentReference greetingRef) {
  //   ApiFuture<DocumentSnapshot> query = greetingRef.get();
  //   DocumentSnapshot greetingSnapshot = query.get();
  //
  //   id = greetingSnapshot.getId();
  //   authorName = greetingSnapshot.getString("authorName");
  //   date = greetingSnapshot.getString("date"); //.toSqlTimestamp()
  //   content = greetingSnapshot.getString("content");
  // }

  public void save() {
    Map<String, Object> greetingData = new HashMap<>();
    greetingData.put("date", date);
    greetingData.put("content", content);
    greetingData.put("authorName", authorName);

    book.getBookRef().collection("Greetings").add(greetingData);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    Greeting greeting = (Greeting) obj;
    return Objects.equals(id, greeting.id)
        && Objects.equals(authorName, greeting.authorName)
        && Objects.equals(content, greeting.content)
        && Objects.equals(date, greeting.date);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, authorName, content, date);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", id)
        .add("authorName", authorName)
        .add("content", content)
        .add("date", date)
        .add("book", book)
        .toString();
  }
}
