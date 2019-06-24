/*
 * Copyright 2019 Google Inc.
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

//[START spring_data_spanner_schema_sample]
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gcp.data.spanner.core.admin.SpannerDatabaseAdminTemplate;
import org.springframework.cloud.gcp.data.spanner.core.admin.SpannerSchemaUtils;
import org.springframework.stereotype.Component;

/**
 * This sample demonstrates how to generate schemas for interleaved tables from POJOs and how to
 * execute DDL.
 */
@Component
public class SpannerSchemaToolsSample {

  @Autowired
  SpannerDatabaseAdminTemplate spannerDatabaseAdminTemplate;

  @Autowired
  SpannerSchemaUtils spannerSchemaUtils;

  /**
   * Creates the Singers table. Also creates the Albums table, because Albums is interleaved with
   * Singers.
   */
  public void createTableIfNotExists() {
    if (!this.spannerDatabaseAdminTemplate.tableExists("Singers")) {
      this.spannerDatabaseAdminTemplate.executeDdlStrings(
          this.spannerSchemaUtils
              .getCreateTableDdlStringsForInterleavedHierarchy(Singer.class),
          true);
    }
  }

  /**
   * Drops both the Singers and Albums tables using just a reference to the Singer entity type ,
   * because they are interleaved.
   */
  public void dropTables() {
    if (this.spannerDatabaseAdminTemplate.tableExists("Singers")) {
      this.spannerDatabaseAdminTemplate.executeDdlStrings(
          this.spannerSchemaUtils.getDropTableDdlStringsForInterleavedHierarchy(Singer.class),
          false);
    }
  }
}
//[END spring_data_spanner_schema_sample]
