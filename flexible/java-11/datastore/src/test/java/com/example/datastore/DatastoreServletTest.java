/*
 * Copyright 2023 Google LLC
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

package com.example.datastore;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.IncompleteKey;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class DatastoreServletTest {

  @SuppressWarnings("unchecked")
  @Test
  public void testget() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    when(request.getRemoteAddr()).thenReturn("9.9.9.9");

    Datastore mockdatastore = mock(Datastore.class);
    KeyFactory mockKeyFactory = mock(KeyFactory.class);
    when(mockdatastore.newKeyFactory()).thenReturn(mockKeyFactory);

    IncompleteKey mockKey = mock(IncompleteKey.class);
    when(mockKeyFactory.newKey()).thenReturn(mockKey);
    QueryResults<Entity> results = mock(QueryResults.class);
    when(results.hasNext()).thenReturn(false);
    when(mockdatastore.run(any(Query.class))).thenReturn(results);

    MockedStatic<DatastoreOptions> datastoreOptionsMock =
        Mockito.mockStatic(DatastoreOptions.class, Mockito.RETURNS_DEEP_STUBS);

    datastoreOptionsMock
        .when(() -> DatastoreOptions.getDefaultInstance().getService())
        .thenReturn(mockdatastore);
    DatastoreServlet servlet = new DatastoreServlet();
    servlet.doGet(request, response);
    verify(mockdatastore).add(any(FullEntity.class));
  }
}
