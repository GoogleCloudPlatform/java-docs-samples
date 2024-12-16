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
import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.GetRequest;
import com.google.appengine.api.search.GetResponse;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.SearchServiceFactory;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// CHECKSTYLE:OFF
// [START get_document_import]
// [END get_document_import]
// @formatter:on
// CHECKSTYLE:ON

/**
 * Code snippet for getting a document from Index.
 */
@SuppressWarnings("serial")
@WebServlet(
    name = "searchIndex",
    description = "Search: Index a new document",
    urlPatterns = "/search/index"
)
public class IndexServlet extends HttpServlet {

  private static final String INDEX = "testIndex";

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    PrintWriter out = resp.getWriter();
    Document document =
        Document.newBuilder()
            .setId("AZ125")
            .addField(Field.newBuilder().setName("myField").setText("myValue"))
            .build();
    try {
      Utils.indexADocument(INDEX, document);
    } catch (InterruptedException e) {
      out.println("Interrupted");
      return;
    }
    out.println("Indexed a new document.");
    // [START get_document]
    IndexSpec indexSpec = IndexSpec.newBuilder().setName(INDEX).build();
    Index index = SearchServiceFactory.getSearchService().getIndex(indexSpec);

    // Fetch a single document by its  doc_id
    Document doc = index.get("AZ125");

    // Fetch a range of documents by their doc_ids
    GetResponse<Document> docs =
        index.getRange(GetRequest.newBuilder().setStartId("AZ125").setLimit(100).build());
    // [END get_document]
    out.println("myField: " + docs.getResults().get(0).getOnlyField("myField").getText());
  }
}
