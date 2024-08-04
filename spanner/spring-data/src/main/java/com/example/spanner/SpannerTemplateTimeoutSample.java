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

package com.example.spanner;

// [START spanner_spring_data_template_timeout_sample]
import com.google.cloud.spanner.SpannerOptions;
import com.google.cloud.spanner.SpannerOptions.SpannerCallContextTimeoutConfigurator;
import com.google.cloud.spanner.Statement;
import com.google.cloud.spring.data.spanner.core.SpannerQueryOptions;
import com.google.cloud.spring.data.spanner.core.SpannerTemplate;
import io.grpc.Context;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.threeten.bp.Duration;

@Component
public class SpannerTemplateTimeoutSample {

  @Autowired
  SpannerTemplate spannerTemplate;

  public void runTemplateTimeoutExample(Singer singer) {
    // Query all Singers using a custom timeout value for the query.
    Context context = Context.current().withValue(SpannerOptions.CALL_CONTEXT_CONFIGURATOR_KEY,
        SpannerCallContextTimeoutConfigurator.create()
            .withExecuteQueryTimeout(Duration.ofSeconds(10L)));
    context.run(() -> {
      List<Singer> allSingers = this.spannerTemplate.query(Singer.class,
          Statement.of("SELECT * FROM Singers"),
          new SpannerQueryOptions().setAllowPartialRead(true));
    });

    // Delete all singers using a DML statement with a timeout.
    Context.current().withValue(SpannerOptions.CALL_CONTEXT_CONFIGURATOR_KEY,
        SpannerCallContextTimeoutConfigurator.create()
            .withExecuteUpdateTimeout(Duration.ofSeconds(10L))).run(() -> {
              this.spannerTemplate
                .executeDmlStatement(Statement.of("delete from Singers where true"));
            });

    // Insert a new Singer record using a mutation (Commit) with a timeout.
    // NOTE: SpannerTemplate uses mutations for executing data modifications. Mutations are applied
    // to the database using the Commit RPC. This example therefore sets a timeout for the Commit
    // RPC.
    Context.current().withValue(SpannerOptions.CALL_CONTEXT_CONFIGURATOR_KEY,
        SpannerCallContextTimeoutConfigurator.create()
            .withCommitTimeout(Duration.ofSeconds(10L))).run(() -> {
              this.spannerTemplate.insert(singer);
            });
  }

}
// [END spanner_spring_data_template_timeout_sample]
