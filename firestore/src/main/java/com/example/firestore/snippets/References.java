/*
 * Copyright 2017 Google Inc.
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

package com.example.firestore.snippets;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;

/** Examples of references to a collection, document in a collection and subcollection. */
public class References {

  private final Firestore db;

  public References(Firestore db) {
    this.db = db;
  }

  /**
   * Return a reference to collection.
   *
   * @return collection reference
   */
  public CollectionReference getACollectionRef() {
    // [START fs_collection_ref]
    // Reference to the collection "users"
    CollectionReference collection = db.collection("users");
    // [END fs_collection_ref]
    return collection;
  }

  /**
   * Return a reference to a document.
   *
   * @return document reference
   */
  public DocumentReference getADocumentRef() {
    // [START fs_document_ref]
    // Reference to a document with id "alovelace" in the collection "users"
    DocumentReference document = db.collection("users").document("alovelace");
    // [END fs_document_ref]
    return document;
  }

  /**
   * Return a reference to a document using path.
   *
   * @return document reference
   */
  public DocumentReference getADocumentRefUsingPath() {
    // [START fs_document_path_ref]
    // Reference to a document with id "alovelace" in the collection "users"
    DocumentReference document = db.document("users/alovelace");
    // [END fs_document_path_ref]
    return document;
  }

  /**
   * Return a reference to a document in a sub-collection.
   *
   * @return document reference in a subcollection
   */
  public DocumentReference getASubCollectionDocumentRef() {
    // [START fs_subcollection_ref]
    // Reference to a document in subcollection "messages"
    DocumentReference document =
        db.collection("rooms").document("roomA").collection("messages").document("message1");
    // [END fs_subcollection_ref]
    return document;
  }
}
