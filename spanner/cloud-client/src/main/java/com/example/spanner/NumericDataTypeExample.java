/*
 * Copyright 2020 Google Inc.
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

package com.example.spanner;

// [START spanner_numeric_datatype]
import com.google.cloud.spanner.DatabaseAdminClient;
import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.DatabaseId;
import com.google.cloud.spanner.Mutation;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerExceptionFactory;
import com.google.cloud.spanner.SpannerOptions;
import com.google.cloud.spanner.Statement;
import com.google.cloud.spanner.TransactionContext;
import com.google.cloud.spanner.TransactionRunner.TransactionCallable;
import com.google.common.collect.ImmutableList;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class NumericDataTypeExample {

  /** Class to contain singer revenues sample data. */
  static class SingerRevenue {

    final long singerId;
    final long year;
    final BigDecimal revenues;

    SingerRevenue(long singerId, long year, BigDecimal revenues) {
      this.singerId = singerId;
      this.year = year;
      this.revenues = revenues;
    }
  }

  static final List<SingerRevenue> SINGER_REVENUES =
      Arrays.asList(
          new SingerRevenue(1, 2020, new BigDecimal("148143.18")),
          new SingerRevenue(2, 2020, new BigDecimal("87003.81")),
          new SingerRevenue(3, 2020, new BigDecimal("101002.00")),
          new SingerRevenue(4, 2020, new BigDecimal("68599.11")),
          new SingerRevenue(5, 2020, new BigDecimal("7500")),
          new SingerRevenue(1, 2019, new BigDecimal("20300")),
          new SingerRevenue(2, 2019, new BigDecimal("0.00")),
          new SingerRevenue(3, 2019, new BigDecimal("98762.44")),
          new SingerRevenue(4, 2019, new BigDecimal("71009.30")),
          new SingerRevenue(5, 2019, new BigDecimal("12611.75")));

  static void numericDataType() {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project";
    String instanceId = "my-instance";
    String databaseId = "my-database";

    try (Spanner spanner =
        SpannerOptions.newBuilder().setProjectId(projectId).build().getService()) {
      numericDataType(spanner, DatabaseId.of(projectId, instanceId, databaseId));
    }
  }

  static void numericDataType(Spanner spanner, DatabaseId databaseId) {
    DatabaseAdminClient dbAdminClient = spanner.getDatabaseAdminClient();
    DatabaseClient dbClient = spanner.getDatabaseClient(databaseId);

    // Create a table with a column with type NUMERIC.
    try {
      dbAdminClient
          .updateDatabaseDdl(
              databaseId.getInstanceId().getInstance(),
              databaseId.getDatabase(),
              ImmutableList.of(
                  "CREATE TABLE SingerRevenues (\n"
                      + "SingerId INT64 NOT NULL,\n"
                      + "Year INT64 NOT NULL,\n"
                      + "Revenues NUMERIC\n"
                      + ") PRIMARY KEY (SingerId, Year)"),
              null)
          .get();
      System.out.println("Created SingerRevenues table.");
    } catch (ExecutionException e) {
      throw SpannerExceptionFactory.newSpannerException(e.getCause());
    } catch (InterruptedException e) {
      throw SpannerExceptionFactory.propagateInterrupt(e);
    }

    // Fill SingerRevenues table.
    dbClient
        .readWriteTransaction()
        .run(
            new TransactionCallable<Void>() {
              @Override
              public Void run(TransactionContext transaction) throws Exception {
                for (SingerRevenue singerRevenue : SINGER_REVENUES) {
                  transaction.buffer(
                      Mutation.newInsertBuilder("SingerRevenues")
                          .set("SingerId")
                          .to(singerRevenue.singerId)
                          .set("Year")
                          .to(singerRevenue.year)
                          .set("Revenues")
                          .to(singerRevenue.revenues)
                          .build());
                }
                return null;
              }
            });

    // Get Singers with Revenues > 100,000 in 2020.
    BigDecimal revenues = new BigDecimal(100000);
    long year = 2020;
    try (ResultSet rs =
        dbClient
            .singleUse()
            .executeQuery(
                Statement.newBuilder(
                        "SELECT *\n"
                            + "From SingerRevenues\n"
                            + "WHERE Revenues > @revenues AND Year=@year\n"
                            + "ORDER BY Revenues DESC")
                    .bind("revenues")
                    .to(revenues)
                    .bind("year")
                    .to(year)
                    .build())) {
      while (rs.next()) {
        System.out.printf(
            "%s %s %s%n", rs.getLong("SingerId"), rs.getLong("Year"), rs.getBigDecimal("Revenues"));
      }
    }

    // Get the total revenues per singer for all years.
    try (ResultSet rs =
        dbClient
            .singleUse()
            .executeQuery(
                Statement.of(
                    "SELECT SingerId, SUM(Revenues) AS TotalRevenues\n"
                        + "FROM SingerRevenues\n"
                        + "GROUP BY SingerId"))) {
      while (rs.next()) {
        System.out.printf(
            "Total revenues: %s %s%n", rs.getLong("SingerId"), rs.getBigDecimal("TotalRevenues"));
      }
    }
  }
}
// [END spanner_numeric_datatype]
