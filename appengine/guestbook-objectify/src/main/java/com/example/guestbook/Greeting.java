/*
 * Copyright 2015 Google Inc.
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

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;

import java.lang.String;
import java.util.Date;

/**
 * The @Entity tells Objectify about our entity.  We also register it in {@link OfyHelper}
 * Our primary key @Id is set automatically by the Google Datastore for us.
 *
 * We add a @Parent to tell the object about its ancestor. We are doing this to support many
 * guestbooks.  Objectify, unlike the AppEngine library requires that you specify the fields you
 * want to index using @Index.  Only indexing the fields you need can lead to substantial gains in
 * performance -- though if not indexing your data from the start will require indexing it later.
 *
 * NOTE - all the properties are PUBLIC so that can keep the code simple.
 **/
@Entity
public class Greeting {
  @Parent Key<Guestbook> theBook;
  @Id public Long id;

  public String authorEmail;
  public String authorId;
  public String content;
  @Index public Date date;

  /**
   * Simple constructor just sets the date.
   **/
  public Greeting() {
    date = new Date();
  }

  /**
   * A convenience constructor.
   **/
  public Greeting(String book, String content) {
    this();
    if (book != null) {
      theBook = Key.create(Guestbook.class, book);  // Creating the Ancestor key
    } else {
      theBook = Key.create(Guestbook.class, "default");
    }
    this.content = content;
  }

  /**
   * Takes all important fields.
   **/
  public Greeting(String book, String content, String id, String email) {
    this(book, content);
    authorEmail = email;
    authorId = id;
  }

}
//[END all]
