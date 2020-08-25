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

// [START spring_data_spanner_template_sample]
import com.google.cloud.spanner.KeySet;
import com.google.cloud.spanner.Statement;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gcp.data.spanner.core.SpannerTemplate;
import org.springframework.stereotype.Component;

/**
 * A quick start code for Spring Data Cloud Spanner. It demonstrates how to use SpannerTemplate to
 * execute DML and SQL queries, save POJOs, and read entities.
 */
@Component
public class SpannerTemplateSample {

  @Autowired
  SpannerTemplate spannerTemplate;

  public void runTemplateExample(Singer singer) {
    // Delete all of the rows in the Singer table.
    this.spannerTemplate.delete(Singer.class, KeySet.all());

    // Insert a singer into the Singers table.
    this.spannerTemplate.insert(singer);

    // Read all of the singers in the Singers table.
    List<Singer> allSingers = this.spannerTemplate
        .query(Singer.class, Statement.of("SELECT * FROM Singers"), null);
  }

}
// [END spring_data_spanner_template_sample]
