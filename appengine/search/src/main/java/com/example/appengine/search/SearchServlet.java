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

// @formatter:off
// [START search_document_import]

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.SearchException;
import com.google.appengine.api.search.SearchServiceFactory;
import com.google.appengine.api.search.StatusCode;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// [END search_document_import]
// CHECKSTYLE:OFF
// @formatter:on
// CHECKSTYLE:ON



@SuppressWarnings("serial")
public class SearchServlet extends HttpServlet {

  private static final String SEARCH_INDEX = "searchIndex";

  private Index getIndex() {
    IndexSpec indexSpec = IndexSpec.newBuilder().setName(SEARCH_INDEX).build();
    Index index = SearchServiceFactory.getSearchService().getIndex(indexSpec);
    return index;
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    PrintWriter out = resp.getWriter();
    Document doc = Document.newBuilder()
        .setId("theOnlyPiano")
        .addField(Field.newBuilder().setName("product").setText("piano"))
        .addField(Field.newBuilder().setName("maker").setText("Yamaha"))
        .addField(Field.newBuilder().setName("price").setNumber(4000))
        .build();
    try {
      Utils.indexADocument(SEARCH_INDEX, doc);
    } catch (InterruptedException e) {
      // ignore
    }
    // [START search_document]
    final int maxRetry = 3;
    int attempts = 0;
    int delay = 2;
    while (true) {
      try {
        String queryString = "product = piano AND price < 5000";
        Results<ScoredDocument> results = getIndex().search(queryString);

        // Iterate over the documents in the results
        for (ScoredDocument document : results) {
          // handle results
          out.print("maker: " + document.getOnlyField("maker").getText());
          out.println(", price: " + document.getOnlyField("price").getNumber());
        }
      } catch (SearchException e) {
        if (StatusCode.TRANSIENT_ERROR.equals(e.getOperationResult().getCode())
            && ++attempts < maxRetry) {
          // retry
          try {
            Thread.sleep(delay * 1000);
          } catch (InterruptedException e1) {
            // ignore
          }
          delay *= 2; // easy exponential backoff
          continue;
        } else {
          throw e;
        }
      }
      break;
    }
    // [END search_document]
    // We don't test the search result below, but we're fine if it runs without errors.
    out.println("Search performed");
    Index index = getIndex();
    // [START simple_search_1]
    index.search("rose water");
    // [END simple_search_1]
    // [START simple_search_2]
    index.search("1776-07-04");
    // [END simple_search_2]
    // [START simple_search_3]
    // search for documents with pianos that cost less than $5000
    index.search("product = piano AND price < 5000");
    // [END simple_search_3]
  }
}
