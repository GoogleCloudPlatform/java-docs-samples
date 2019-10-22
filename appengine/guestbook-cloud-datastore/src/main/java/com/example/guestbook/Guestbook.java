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

package com.example.guestbook;

import static com.example.guestbook.Persistence.getDatastore;
import static com.example.guestbook.Persistence.getKeyFactory;
import static com.google.cloud.datastore.StructuredQuery.OrderBy.desc;
import static com.google.cloud.datastore.StructuredQuery.PropertyFilter.hasAncestor;

import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.EntityQuery;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import java.util.List;
import java.util.Objects;

//[START all]
public class Guestbook {
  private static final KeyFactory keyFactory = getKeyFactory(Guestbook.class);
  private final Key key;

  public final String book;

  public Guestbook(String book) {
    this.book = book == null ? "default" : book;
    key = keyFactory.newKey(
            this.book); // There is a 1:1 mapping between Guestbook names and Guestbook objects
  }

  public Key getKey() {
    return key;
  }

  public List<Greeting> getGreetings() {
    // This query requires the index defined in index.yaml to work because of the orderBy on date.
    EntityQuery query =
        Query.entityQueryBuilder()
            .kind("Greeting")
            .filter(hasAncestor(key))
            .orderBy(desc("date"))
            .limit(5)
            .build();

    QueryResults<Entity> results = getDatastore().run(query);

    Builder<Greeting> resultListBuilder = ImmutableList.builder();
    while (results.hasNext()) {
      resultListBuilder.add(new Greeting(results.next()));
    }

    return resultListBuilder.build();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Guestbook guestbook = (Guestbook) o;
    return Objects.equals(book, guestbook.book) && Objects.equals(key, guestbook.key);
  }

  @Override
  public int hashCode() {
    return Objects.hash(book, key);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("keyFactory", keyFactory)
        .add("book", book)
        .add("key", key)
        .toString();
  }
}
//[END all]
