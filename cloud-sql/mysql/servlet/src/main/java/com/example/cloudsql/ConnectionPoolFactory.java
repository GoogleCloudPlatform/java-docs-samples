/*
 * Copyright 2022 Google LLC
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

import com.zaxxer.hikari.HikariConfig;

public class ConnectionPoolFactory {

  public static HikariConfig configureConnectionPool(HikariConfig config) {
    // [START cloud_sql_mysql_servlet_limit]
    // maximumPoolSize limits the total number of concurrent connections this pool will keep. Ideal
    // values for this setting are highly variable on app design, infrastructure, and database.
    config.setMaximumPoolSize(5);
    // minimumIdle is the minimum number of idle connections Hikari maintains in the pool.
    // Additional connections will be established to meet this value unless the pool is full.
    config.setMinimumIdle(5);
    // [END cloud_sql_mysql_servlet_limit]

    // [START cloud_sql_mysql_servlet_timeout]
    // setConnectionTimeout is the maximum number of milliseconds to wait for a connection checkout.
    // Any attempt to retrieve a connection from this pool that exceeds the set limit will throw an
    // SQLException.
    config.setConnectionTimeout(10000); // 10 seconds
    // idleTimeout is the maximum amount of time a connection can sit in the pool. Connections that
    // sit idle for this many milliseconds are retried if minimumIdle is exceeded.
    config.setIdleTimeout(600000); // 10 minutes
    // [END cloud_sql_mysql_servlet_timeout]

    // [START cloud_sql_mysql_servlet_backoff]
    // Hikari automatically delays between failed connection attempts, eventually reaching a
    // maximum delay of `connectionTimeout / 2` between attempts.
    // [END cloud_sql_mysql_servlet_backoff]

    // [START cloud_sql_mysql_servlet_lifetime]
    // maxLifetime is the maximum possible lifetime of a connection in the pool. Connections that
    // live longer than this many milliseconds will be closed and reestablished between uses. This
    // value should be several minutes shorter than the database's timeout value to avoid unexpected
    // terminations.
    config.setMaxLifetime(1800000); // 30 minutes
    // [END cloud_sql_mysql_servlet_lifetime]
    return config;
  }
}
