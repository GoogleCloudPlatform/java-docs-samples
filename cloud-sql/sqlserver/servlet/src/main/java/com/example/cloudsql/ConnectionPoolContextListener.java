/*
 * Copyright 2020 Google LLC
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

import com.zaxxer.hikari.HikariDataSource;
import java.sql.SQLException;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.sql.DataSource;

@WebListener("Creates a connection pool that is stored in the Servlet's context for later use.")
public class ConnectionPoolContextListener implements ServletContextListener {

  private static final Logger LOGGER = Logger.getLogger(IndexServlet.class.getName());

  // Saving credentials in environment variables is convenient, but not secure - consider a more
  // secure solution such as https://cloud.google.com/kms/ to help keep secrets safe.
  private static final String INSTANCE_CONNECTION_NAME = System.getenv(
      "INSTANCE_CONNECTION_NAME");
  private static final String DB_USER = System.getenv("DB_USER");
  private static final String DB_PASS = System.getenv("DB_PASS");
  private static final String DB_NAME = System.getenv("DB_NAME");

  @Override
  public void contextDestroyed(ServletContextEvent event) {
    // This function is called when the Servlet is destroyed.
    HikariDataSource pool = (HikariDataSource) event.getServletContext().getAttribute("my-pool");
    if (pool != null) {
      pool.close();
    }
  }

  @Override
  public void contextInitialized(ServletContextEvent event) {
    // This function is called when the application starts and will safely create a connection pool
    // that can be used to connect to.
    DataSource pool = (DataSource) event.getServletContext().getAttribute("my-pool");
    if (pool == null) {
      if (System.getenv("INSTANCE_HOST") != null) {
        pool = TcpConnectionPoolFactory.createConnectionPool();
      } else {
        pool = ConnectorConnectionPoolFactory.createConnectionPool();
      }
      event.getServletContext().setAttribute("my-pool", pool);
    }
    try {
      // from src/main/java/com/example/cloudsql/Utils.java
      Utils.createTable(pool);
    } catch (SQLException ex) {
      throw new RuntimeException("Unable to verify table schema. Please double check the steps"
          + "in the README and try again.", ex);
    }
  }
}
