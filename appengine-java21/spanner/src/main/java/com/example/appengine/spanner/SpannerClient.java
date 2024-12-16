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

package com.example.appengine.spanner;

import com.google.cloud.spanner.DatabaseAdminClient;
import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.DatabaseId;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerOptions;
import java.io.IOException;
import java.util.UUID;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

// With @WebListener annotation the webapp/WEB-INF/web.xml is no longer required.
@WebListener
public class SpannerClient implements ServletContextListener {

  private static String PROJECT_ID;
  private static String INSTANCE_ID;
  private static String DATABASE_ID;

  // The initial connection can be an expensive operation -- We cache this Connection
  // to speed things up.  For this sample, keeping them here is a good idea, for
  // your application, you may wish to keep this somewhere else.
  private static Spanner spanner = null;
  private static DatabaseAdminClient databaseAdminClient = null;
  private static DatabaseClient databaseClient = null;

  private static ServletContext sc;

  private static void connect() throws IOException {
    if (INSTANCE_ID == null) {
      if (sc != null) {
        sc.log("environment variable SPANNER_INSTANCE need to be defined.");
      }
      return;
    }
    SpannerOptions options = SpannerOptions.newBuilder().build();
    PROJECT_ID = options.getProjectId();
    spanner = options.getService();
    databaseAdminClient = spanner.getDatabaseAdminClient();
  }

  static DatabaseAdminClient getDatabaseAdminClient() {
    if (databaseAdminClient == null) {
      try {
        connect();
      } catch (IOException e) {
        if (sc != null) {
          sc.log("getDatabaseAdminClient ", e);
        }
      }
    }
    if (databaseAdminClient == null) {
      if (sc != null) {
        sc.log("Spanner : Unable to connect");
      }
    }
    return databaseAdminClient;
  }

  static DatabaseClient getDatabaseClient() {
    if (databaseClient == null) {
      databaseClient =
          spanner.getDatabaseClient(DatabaseId.of(PROJECT_ID, INSTANCE_ID, DATABASE_ID));
    }
    return databaseClient;
  }

  @Override
  public void contextInitialized(ServletContextEvent event) {
    if (event != null) {
      sc = event.getServletContext();
      if (INSTANCE_ID == null) {
        INSTANCE_ID = sc.getInitParameter("SPANNER_INSTANCE");
      }
    }
    //try system properties
    if (INSTANCE_ID == null) {
      INSTANCE_ID = System.getProperty("SPANNER_INSTANCE");
    }

    if (DATABASE_ID == null) {
      DATABASE_ID = "db-" + UUID.randomUUID().toString().substring(0, 25);
    }

    try {
      connect();
    } catch (IOException e) {
      if (sc != null) {
        sc.log("SpannerConnection - connect ", e);
      }
    }
    if (databaseAdminClient == null) {
      if (sc != null) {
        sc.log("SpannerConnection - No Connection");
      }
    }
    if (sc != null) {
      sc.log("ctx Initialized: " + INSTANCE_ID + " " + DATABASE_ID);
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    // App Engine does not currently invoke this method.
    databaseAdminClient = null;
  }

  static String getInstanceId() {
    return INSTANCE_ID;
  }

  static String getDatabaseId() {
    return DATABASE_ID;
  }
}
