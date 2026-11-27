/*
 * Copyright 2024 Google LLC
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

package com.example.cloudsql;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import org.junit.Test;

/** Covers the doPost error path, which does not need a database. */
public class TestIndexServletPostgresErrorResponse {

  @Test
  public void testFailedVoteIsNotReportedAsSuccess() throws Exception {
    DataSource pool = mock(DataSource.class);
    when(pool.getConnection()).thenThrow(new SQLException("connection refused"));

    ServletContext context = mock(ServletContext.class);
    when(context.getAttribute("my-pool")).thenReturn(pool);

    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getServletContext()).thenReturn(context);
    when(request.getParameter("team")).thenReturn("TABS");

    StringWriter body = new StringWriter();
    PrintWriter writer = new PrintWriter(body);
    AtomicInteger status = new AtomicInteger();
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(response.getWriter()).thenReturn(writer);
    doAnswer(
            invocation -> {
              status.set(invocation.getArgument(0));
              return null;
            })
        .when(response)
        .setStatus(anyInt());

    new IndexServlet().doPost(request, response);
    writer.flush();

    assertThat(status.get()).isEqualTo(500);
    assertThat(body.toString()).contains("Unable to successfully cast vote");
    assertThat(body.toString()).doesNotContain("Vote successfully cast");
  }
}
