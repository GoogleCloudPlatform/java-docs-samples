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

// [START spanner_spring_data_repository_timeout_sample]
import com.google.cloud.spanner.SpannerOptions;
import com.google.cloud.spanner.SpannerOptions.SpannerCallContextTimeoutConfigurator;
import io.grpc.Context;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.threeten.bp.Duration;

@Component
public class SpannerRepositoryTimeoutSample {

  @Autowired
  SingerRepository singerRepository;

  public void runRepositoryTimeoutExample() {
    // Run multiple statements (both queries and updates), all with a timeout of 10 seconds.
    // NOTE: Spring Data Spanner uses mutations for updating data. Mutations are applied using the
    // Commit RPC. This example therefore sets a timeout value for the Commit RPC.
    // See also https://cloud.google.com/spanner/docs/dml-versus-mutations
    Context context = Context.current().withValue(SpannerOptions.CALL_CONTEXT_CONFIGURATOR_KEY,
        SpannerCallContextTimeoutConfigurator.create()
            // This will ensure that all queries that are run in this context will have a 10
            // seconds timeout.
            .withExecuteQueryTimeout(Duration.ofSeconds(10L))
            // This will ensure that all updates that are run in this context will have a 10
            // seconds timeout.
            .withCommitTimeout(Duration.ofSeconds(10L)));
    context.run(() -> {
      List<Singer> lastNameSingers = this.singerRepository.findByLastName("a last name");

      int fistNameCount = this.singerRepository.countByFirstName("a first name");

      int deletedLastNameCount = this.singerRepository.deleteByLastName("a last name");
    });
  }

}
// [END spanner_spring_data_repository_timeout_sample]
