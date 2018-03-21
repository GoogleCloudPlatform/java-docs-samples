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

import com.google.cloud.spanner.Struct;
import com.google.cloud.spanner.TimestampBound;
import com.google.common.base.Joiner;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.io.gcp.spanner.SpannerConfig;
import org.apache.beam.sdk.io.gcp.spanner.SpannerIO;
import org.apache.beam.sdk.io.gcp.spanner.Transaction;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.Validation;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionView;

public class TransactionalRead {

  private static final String DELIMITER = "\t";

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
    String getSingersFilename();

    void setSingersFilename(String value);

    @Description("Albums output filename in the format: singer_id\talbum_id\talbum_title")
    String getAlbumsFilename();

    void setAlbumsFilename(String value);

  }

  @DefaultCoder(AvroCoder.class)
  static class Singer {

    long singerId;
    String firstName;
    String lastName;

    Singer() {
    }

    Singer(long singerId, String firstName, String lastName) {
      this.singerId = singerId;
      this.firstName = firstName;
      this.lastName = lastName;
    }
  }

  @DefaultCoder(AvroCoder.class)
  static class Album {

    long singerId;
    long albumId;
    String albumTitle;

    Album() {
    }

    Album(long singerId, long albumId, String albumTitle) {
      this.singerId = singerId;
      this.albumId = albumId;
      this.albumTitle = albumTitle;
    }
  }

  public static void main(String[] args) {
    Options options = PipelineOptionsFactory.fromArgs(args).withValidation().as(Options.class);
    Pipeline p = Pipeline.create(options);

    String instanceId = options.getInstanceId();
    String databaseId = options.getDatabaseId();

    // [START spanner_dataflow_txread]
    SpannerConfig spannerConfig = SpannerConfig.create()
        .withInstanceId(instanceId)
        .withDatabaseId(databaseId);
    PCollectionView<Transaction> tx = p.apply(
        SpannerIO.createTransaction()
            .withSpannerConfig(spannerConfig)
            .withTimestampBound(TimestampBound.strong()));
    PCollection<Struct> singers = p.apply(SpannerIO.read()
        .withSpannerConfig(spannerConfig)
        .withQuery("SELECT SingerID, FirstName, LastName FROM Singers")
        .withTransaction(tx));
    PCollection<Struct> albums = p.apply(SpannerIO.read().withSpannerConfig(spannerConfig)
        .withQuery("SELECT SingerId, AlbumId, AlbumTitle FROM Albums")
        .withTransaction(tx));
    // [END spanner_dataflow_txread]

    singers.apply(MapElements.via(new SimpleFunction<Struct, String>() {

      @Override
      public String apply(Struct input) {
        return Joiner.on(DELIMITER).join(input.getLong(0), input.getString(1), input.getString(2));
      }
    })).apply(TextIO.write().to(options.getSingersFilename()).withoutSharding());

    albums.apply(MapElements.via(new SimpleFunction<Struct, String>() {

      @Override
      public String apply(Struct input) {
        return Joiner.on(DELIMITER).join(input.getLong(0), input.getLong(1), input.getString(2));
      }
    })).apply(TextIO.write().to(options.getAlbumsFilename()).withoutSharding());

    p.run().waitUntilFinish();

  }

}
