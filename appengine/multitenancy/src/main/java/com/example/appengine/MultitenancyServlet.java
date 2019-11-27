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

package com.example.appengine;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.SearchService;
import com.google.appengine.api.search.SearchServiceConfig;
import com.google.appengine.api.search.SearchServiceFactory;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// [START example]
@SuppressWarnings("serial")
public class MultitenancyServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String namespace;

    PrintWriter out = resp.getWriter();
    out.println("Code Snippets -- not yet fully runnable as an app");

    // [START temp_namespace]
    // Set the namepace temporarily to "abc"
    String oldNamespace = NamespaceManager.get();
    NamespaceManager.set("abc");
    try {
    //    ... perform operation using current namespace ...
    } finally {
      NamespaceManager.set(oldNamespace);
    }
    // [END temp_namespace]

    // [START per_user_namespace]
    if (com.google.appengine.api.NamespaceManager.get() == null) {
      // Assuming there is a logged in user.
      namespace = UserServiceFactory.getUserService().getCurrentUser().getUserId();
      NamespaceManager.set(namespace);
    }
    // [END per_user_namespace]
    String value = "something here";

    // [START ns_memcache]
    // Create a MemcacheService that uses the current namespace by
    // calling NamespaceManager.get() for every access.
    MemcacheService current = MemcacheServiceFactory.getMemcacheService();

    // stores value in namespace "abc"
    oldNamespace = NamespaceManager.get();
    NamespaceManager.set("abc");
    try {
      current.put("key", value);  // stores value in namespace “abc”
    } finally {
      NamespaceManager.set(oldNamespace);
    }
    // [END ns_memcache]

    // [START specific_memcache]
    // Create a MemcacheService that uses the namespace "abc".
    MemcacheService explicit = MemcacheServiceFactory.getMemcacheService("abc");
    explicit.put("key", value);  // stores value in namespace "abc"
    // [END specific_memcache]

    //[START searchns]
    // Set the current namespace to "aSpace"
    NamespaceManager.set("aSpace");
    // Create a SearchService with the namespace "aSpace"
    SearchService searchService = SearchServiceFactory.getSearchService();
    // Create an IndexSpec
    IndexSpec indexSpec = IndexSpec.newBuilder().setName("myIndex").build();
    // Create an Index with the namespace "aSpace"
    Index index = searchService.getIndex(indexSpec);
    // [END searchns]

    // [START searchns_2]
    // Create a SearchServiceConfig, specifying the namespace "anotherSpace"
    SearchServiceConfig config =  SearchServiceConfig.newBuilder()
        .setNamespace("anotherSpace").build();
    // Create a SearchService with the namespace "anotherSpace"
    searchService = SearchServiceFactory.getSearchService(config);
    // Create an IndexSpec
    indexSpec = IndexSpec.newBuilder().setName("myindex").build();
    // Create an Index with the namespace "anotherSpace"
    index = searchService.getIndex(indexSpec);
    // [END searchns_2]

  }

}
// [END example]
