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
// [START delete_import]

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.GetRequest;
import com.google.appengine.api.search.GetResponse;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.SearchServiceFactory;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// [END delete_import]
// CHECKSTYLE:OFF
// @formatter:on
// CHECKSTYLE:ON

/**
 * Code snippet for deleting documents from an Index.
 */
@SuppressWarnings("serial")
public class DeleteServlet extends HttpServlet {
  private static final Logger LOG = Logger.getLogger(DeleteServlet.class.getSimpleName());

  private static final String SEARCH_INDEX = "searchIndexForDelete";

  private Index getIndex() {
    IndexSpec indexSpec = IndexSpec.newBuilder().setName(SEARCH_INDEX).build();
    Index index = SearchServiceFactory.getSearchService().getIndex(indexSpec);
    return index;
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    // Put one document to avoid an error
    Document document = Document.newBuilder()
        .addField(Field.newBuilder().setName("f").setText("v"))
        .build();
    try {
      Utils.indexADocument(SEARCH_INDEX, document);
    } catch (InterruptedException e) {
      // ignore
    }
    // [START delete_documents]
    try {
      // looping because getRange by default returns up to 100 documents at a time
      while (true) {
        List<String> docIds = new ArrayList<>();
        // Return a set of doc_ids.
        GetRequest request = GetRequest.newBuilder().setReturningIdsOnly(true).build();
        GetResponse<Document> response = getIndex().getRange(request);
        if (response.getResults().isEmpty()) {
          break;
        }
        for (Document doc : response) {
          docIds.add(doc.getId());
        }
        getIndex().delete(docIds);
      }
    } catch (RuntimeException e) {
      LOG.log(Level.SEVERE, "Failed to delete documents", e);
    }
    // [END delete_documents]
    PrintWriter out = resp.getWriter();
    out.println("Deleted documents.");
  }
}
