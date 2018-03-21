/*
 * Copyright 2018 Google Inc.
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

package com.example.dataflow;

import com.google.cloud.Timestamp;
import com.google.cloud.spanner.Mutation;
import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.io.gcp.spanner.MutationGroup;
import org.apache.beam.sdk.io.gcp.spanner.SpannerIO;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.Validation;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.values.PCollection;

/**
 * This sample demonstrates how to group together mutations when writing to the Cloud Spanner
 * database.
 */
public class SpannerGroupWrite {
  public interface Options extends PipelineOptions {

    @Description("Spanner instance ID to write to")
    @Validation.Required
    String getInstanceId();

    void setInstanceId(String value);

    @Description("Spanner database name to write to")
    @Validation.Required
    String getDatabaseId();

    void setDatabaseId(String value);

    @Description("Singers output filename in the format: singer_id\tfirst_name\tlast_name")
    @Validation.Required
    String getSuspiciousUsersFile();

    void setSuspiciousUsersFile(String value);

  }

  public static void main(String[] args) {
    Options options = PipelineOptionsFactory.fromArgs(args).withValidation().as(Options.class);
    Pipeline p = Pipeline.create(options);

    String instanceId = options.getInstanceId();
    String databaseId = options.getDatabaseId();

    String usersIdFile = options.getSuspiciousUsersFile();

    PCollection<String> suspiciousUserIds = p.apply(TextIO.read().from(usersIdFile));

    final Timestamp timestamp = Timestamp.now();

    // [START spanner_dataflow_writegroup]
    PCollection<MutationGroup> mutations = suspiciousUserIds
        .apply(MapElements.via(new SimpleFunction<String, MutationGroup>() {

          @Override
          public MutationGroup apply(String userId) {
            // Immediately block the user.
            Mutation userMutation = Mutation.newUpdateBuilder("Users")
                .set("id").to(userId)
                .set("state").to("BLOCKED")
                .build();
            long generatedId = Hashing.sha1().newHasher()
                .putString(userId, Charsets.UTF_8)
                .putLong(timestamp.getSeconds())
                .putLong(timestamp.getNanos())
                .hash()
                .asLong();

            // Add an entry to pending review requests.
            Mutation pendingReview = Mutation.newInsertOrUpdateBuilder("PendingReviews")
                .set("id").to(generatedId)  // Must be deterministically generated.
                .set("userId").to(userId)
                .set("action").to("REVIEW ACCOUNT")
                .set("note").to("Suspicious activity detected.")
                .build();

            return MutationGroup.create(userMutation, pendingReview);
          }
        }));

    mutations.apply(SpannerIO.write()
        .withInstanceId(instanceId)
        .withDatabaseId(databaseId)
        .grouped());
    // [END spanner_dataflow_writegroup]

    p.run().waitUntilFinish();

  }

}
