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

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.Query;
import com.google.appengine.api.search.QueryOptions;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.SearchException;
import com.google.appengine.api.search.SearchServiceFactory;
import com.google.appengine.api.search.SortExpression;
import com.google.appengine.api.search.SortOptions;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// CHECKSTYLE:OFF
// @formatter:off
// [START search_option_import]
// [END search_option_import]
// @formatter:on
// CHECKSTYLE:ON


/**
 * Code snippet for searching with query options.
 */
@SuppressWarnings("serial")
public class SearchOptionServlet extends HttpServlet {
  private static final Logger LOG = Logger.getLogger(SearchOptionServlet.class.getSimpleName());

  private static final String SEARCH_INDEX = "searchOptionIndex";

  private Index getIndex() {
    IndexSpec indexSpec = IndexSpec.newBuilder().setName(SEARCH_INDEX).build();
    Index index = SearchServiceFactory.getSearchService().getIndex(indexSpec);
    return index;
  }

  private Results<ScoredDocument> doSearch() {
    String indexName = SEARCH_INDEX;
    // [START search_with_options]
    try {
      // Build the SortOptions with 2 sort keys
      SortOptions sortOptions = SortOptions.newBuilder()
          .addSortExpression(SortExpression.newBuilder()
              .setExpression("price")
              .setDirection(SortExpression.SortDirection.DESCENDING)
              .setDefaultValueNumeric(0))
          .addSortExpression(SortExpression.newBuilder()
              .setExpression("brand")
              .setDirection(SortExpression.SortDirection.DESCENDING)
              .setDefaultValue(""))
          .setLimit(1000)
          .build();

      // Build the QueryOptions
      QueryOptions options = QueryOptions.newBuilder()
          .setLimit(25)
          .setFieldsToReturn("model", "price", "description")
          .setSortOptions(sortOptions)
          .build();

      // A query string
      String queryString = "product: coffee roaster AND price < 500";

      //  Build the Query and run the search
      Query query = Query.newBuilder().setOptions(options).build(queryString);
      IndexSpec indexSpec = IndexSpec.newBuilder().setName(indexName).build();
      Index index = SearchServiceFactory.getSearchService().getIndex(indexSpec);
      Results<ScoredDocument> result = index.search(query);
      return result;
    } catch (SearchException e) {
      // handle exception...
    }
    // [END search_with_options]
    return null;
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    // Put one document to avoid an error
    Document document = Document.newBuilder()
        .setId("theOnlyCoffeeRoaster")
        .addField(Field.newBuilder().setName("price").setNumber(200))
        .addField(Field.newBuilder().setName("model").setText("TZ4000"))
        .addField(Field.newBuilder().setName("brand").setText("MyBrand"))
        .addField(Field.newBuilder().setName("product").setText("coffee roaster"))
        .addField(Field.newBuilder()
            .setName("description").setText("A coffee bean roaster at home"))
        .build();
    try {
      Utils.indexADocument(SEARCH_INDEX, document);
    } catch (InterruptedException e) {
      // ignore
    }
    PrintWriter out = resp.getWriter();
    Results<ScoredDocument> result = doSearch();
    for (ScoredDocument doc : result.getResults()) {
      out.println(doc.toString());
    }
  }
}
