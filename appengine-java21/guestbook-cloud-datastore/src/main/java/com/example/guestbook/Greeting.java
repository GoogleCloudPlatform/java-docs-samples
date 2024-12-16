/*
 * Copyright 2016 Google Inc.
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

//[START all]

package com.example.guestbook;

import static com.example.guestbook.Persistence.getDatastore;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.FullEntity.Builder;
import com.google.cloud.datastore.IncompleteKey;
import com.google.cloud.datastore.Key;
import com.google.common.base.MoreObjects;
import java.util.Date;
import java.util.Objects;

@SuppressWarnings("JavadocMethod")
public class Greeting {

  private Guestbook book;

  public Key key;
  public String authorEmail;
  public String authorId;
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

  public Greeting(String book, String content, String id, String email) {
    this(book, content);
    authorEmail = email;
    authorId = id;
  }

  public Greeting(Entity entity) {
    key = entity.hasKey() ? entity.getKey() : null;
    authorEmail = entity.contains("authorEmail") ? entity.getString("authorEmail") : null;
    authorId = entity.contains("authorId") ? entity.getString("authorId") : null;

    date = entity.contains("date") ? entity.getTimestamp("date").toSqlTimestamp() : null;
    content = entity.contains("content") ? entity.getString("content") : null;
  }

  public void save() {
    if (key == null) {
      key = getDatastore().allocateId(makeIncompleteKey()); // Give this greeting a unique ID
    }

    Builder<Key> builder = FullEntity.newBuilder(key);

    if (authorEmail != null) {
      builder.set("authorEmail", authorEmail);
    }

    if (authorId != null) {
      builder.set("authorId", authorId);
    }

    builder.set("content", content);
    builder.set("date", Timestamp.of(date));

    getDatastore().put(builder.build());
  }

  private IncompleteKey makeIncompleteKey() {
    // The book is our ancestor key.
    return Key.newBuilder(book.getKey(), "Greeting").build();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Greeting greeting = (Greeting) o;
    return Objects.equals(key, greeting.key)
        && Objects.equals(authorEmail, greeting.authorEmail)
        && Objects.equals(authorId, greeting.authorId)
        && Objects.equals(content, greeting.content)
        && Objects.equals(date, greeting.date);
  }

  @Override
  public int hashCode() {
    return Objects.hash(key, authorEmail, authorId, content, date);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("key", key)
        .add("authorEmail", authorEmail)
        .add("authorId", authorId)
        .add("content", content)
        .add("date", date)
        .add("book", book)
        .toString();
  }
}
//[END all]
