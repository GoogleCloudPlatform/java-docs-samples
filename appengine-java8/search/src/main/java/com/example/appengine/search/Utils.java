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

package com.example.appengine.search;

// [START index_import]

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.PutException;
import com.google.appengine.api.search.SearchServiceFactory;
import com.google.appengine.api.search.StatusCode;
// [END index_import]

/**
 * A utility class for the search API sample.
 */
public class Utils {

  /**
   * Put a given document into an index with the given indexName.
   *
   * @param indexName The name of the index.
   * @param document A document to add.
   * @throws InterruptedException When Thread.sleep is interrupted.
   */
  // [START putting_document_with_retry]
  public static void indexADocument(String indexName, Document document)
      throws InterruptedException {
    IndexSpec indexSpec = IndexSpec.newBuilder().setName(indexName).build();
    Index index = SearchServiceFactory.getSearchService().getIndex(indexSpec);

    final int maxRetry = 3;
    int attempts = 0;
    int delay = 2;
    while (true) {
      try {
        index.put(document);
      } catch (PutException e) {
        if (StatusCode.TRANSIENT_ERROR.equals(e.getOperationResult().getCode())
            && ++attempts < maxRetry) { // retrying
          Thread.sleep(delay * 1000);
          delay *= 2; // easy exponential backoff
          continue;
        } else {
          throw e; // otherwise throw
        }
      }
      break;
    }
  }
  // [END putting_document_with_retry]
}
