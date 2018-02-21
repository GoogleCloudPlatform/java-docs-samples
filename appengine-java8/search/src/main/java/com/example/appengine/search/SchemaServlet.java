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
import com.google.appengine.api.search.Field.FieldType;
import com.google.appengine.api.search.GetIndexesRequest;
import com.google.appengine.api.search.GetResponse;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.Schema;
import com.google.appengine.api.search.SearchServiceFactory;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// @formatter:off
// CHECKSTYLE:OFF
// [START schema_import]
// [END schema_import]
// @formatter:on
// CHECKSTYLE:ON

@SuppressWarnings("serial")
@WebServlet(
    name = "searchSchema",
    description = "Search: List the schema for a document.",
    urlPatterns = "/search/schema"
)
public class SchemaServlet extends HttpServlet {

  private static final String SEARCH_INDEX = "schemaIndex";

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    PrintWriter out = resp.getWriter();
    Document doc =
        Document.newBuilder()
            .setId("theOnlyCar")
            .addField(Field.newBuilder().setName("maker").setText("Toyota"))
            .addField(Field.newBuilder().setName("price").setNumber(300000))
            .addField(Field.newBuilder().setName("color").setText("lightblue"))
            .addField(Field.newBuilder().setName("model").setText("Prius"))
            .build();
    try {
      Utils.indexADocument(SEARCH_INDEX, doc);
    } catch (InterruptedException e) {
      // ignore
    }
    // [START list_schema]
    GetResponse<Index> response =
        SearchServiceFactory.getSearchService()
            .getIndexes(GetIndexesRequest.newBuilder().setSchemaFetched(true).build());

    // List out elements of each Schema
    for (Index index : response) {
      Schema schema = index.getSchema();
      for (String fieldName : schema.getFieldNames()) {
        List<FieldType> typesForField = schema.getFieldTypes(fieldName);
        // Just printing out the field names and types
        for (FieldType type : typesForField) {
          out.println(index.getName() + ":" + fieldName + ":" + type.name());
        }
      }
    }
    // [END list_schema]
  }
}
