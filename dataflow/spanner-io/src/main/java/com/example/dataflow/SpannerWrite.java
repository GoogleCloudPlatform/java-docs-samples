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

package com.example.dataflow;

import com.google.cloud.spanner.Mutation;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.io.gcp.spanner.SpannerIO;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.Validation;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
This sample demonstrates how to write to a Spanner table.

## Prerequisites
* Maven installed
* Set up GCP default credentials, one of the following:
    - export GOOGLE_APPLICATION_CREDENTIALS=path/to/credentials.json
    - gcloud auth application-default login
  [https://developers.google.com/identity/protocols/application-default-credentials]
* Create the Spanner table to write to, you'll need:
    - Instance ID
    - Database ID
    - Singers Table with schema:
       *singerId: INT64 NOT NULL
        firstName: STRING NOT NULL
        lastName: STRING NOT NULL
    - Albums Table with schema:
        singerId: INT64 NOT NULL
       *albumId: INT64 NOT NULL
        albumTitle: STRING NOT NULL
  [https://cloud.google.com/spanner/docs/quickstart-console]

## How to run
cd java-docs-samples/dataflow/spanner-io
mvn clean
mvn compile
mvn exec:java \
    -Dexec.mainClass=com.example.dataflow.SpannerWrite \
    -Dexec.args="--instanceId=my-instance-id \
                 --databaseId=my-database-id
*/

public class SpannerWrite {

  static final String DELIMITER = "\t";

  public interface Options extends PipelineOptions {

    @Description("Singers filename in the format: singer_id\tfirst_name\tlast_name")
    String getSingersFilename();

    void setSingersFilename(String value);

    @Description("Albums filename in the format: singer_id\talbum_id\talbum_title")
    String getAlbumsFilename();

    void setAlbumsFilename(String value);

    @Description("Spanner instance ID to write to")
    @Validation.Required
    String getInstanceId();

    void setInstanceId(String value);

    @Description("Spanner database name to write to")
    @Validation.Required
    String getDatabaseId();

    void setDatabaseId(String value);
  }

  @DefaultCoder(AvroCoder.class)
  static class Singer {
    long singerId;
    String firstName;
    String lastName;

    Singer() {}

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

    Album() {}

    Album(long singerId, long albumId, String albumTitle) {
      this.singerId = singerId;
      this.albumId = albumId;
      this.albumTitle = albumTitle;
    }
  }

  /**
   * Parses each tab-delimited line into a Singer object. The line format is the following:
   *   singer_id\tfirstName\tlastName
   */
  static class ParseSinger extends DoFn<String, Singer> {
    private static final Logger LOG = LoggerFactory.getLogger(ParseSinger.class);

    @ProcessElement
    public void processElement(ProcessContext c) {
      String[] columns = c.element().split(DELIMITER);
      try {
        Long singerId = Long.parseLong(columns[0].trim());
        String firstName = columns[1].trim();
        String lastName = columns[2].trim();
        c.output(new Singer(singerId, firstName, lastName));
      } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
        LOG.info("ParseSinger: parse error on '" + c.element() + "': " + e.getMessage());
      }
    }
  }

  /**
   * Parses each tab-delimited line into an Album object. The line format is the following:
   *   singer_id\talbumId\talbumTitle
   */
  static class ParseAlbum extends DoFn<String, Album> {
    private static final Logger LOG = LoggerFactory.getLogger(ParseAlbum.class);

    @ProcessElement
    public void processElement(ProcessContext c) {
      String[] columns = c.element().split(DELIMITER);
      try {
        Long singerId = Long.parseLong(columns[0].trim());
        Long albumId = Long.parseLong(columns[1].trim());
        String albumTitle = columns[2].trim();
        c.output(new Album(singerId, albumId, albumTitle));
      } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
        LOG.info("ParseAlbum: parse error on '" + c.element() + "': " + e.getMessage());
      }
    }
  }

  public static void main(String[] args) {
    Options options = PipelineOptionsFactory.fromArgs(args).withValidation().as(Options.class);
    Pipeline p = Pipeline.create(options);

    String instanceId = options.getInstanceId();
    String databaseId = options.getDatabaseId();

    // Read singers from a tab-delimited file
    p.apply("ReadSingers", TextIO.read().from(options.getSingersFilename()))
        // Parse the tab-delimited lines into Singer objects
        .apply("ParseSingers", ParDo.of(new ParseSinger()))
        // Spanner expects a Mutation object, so create it using the Singer's data
        .apply("CreateSingerMutation", ParDo.of(new DoFn<Singer, Mutation>() {
          @ProcessElement
          public void processElement(ProcessContext c) {
            Singer singer = c.element();
            c.output(Mutation.newInsertOrUpdateBuilder("singers")
                .set("singerId").to(singer.singerId)
                .set("firstName").to(singer.firstName)
                .set("lastName").to(singer.lastName)
                .build());
          }
        }))
        // Finally write the Mutations to Spanner
        .apply("WriteSingers", SpannerIO.write()
            .withInstanceId(instanceId)
            .withDatabaseId(databaseId));

    // Read albums from a tab-delimited file
    PCollection<Album> albums = p
        .apply("ReadAlbums", TextIO.read().from(options.getAlbumsFilename()))
        // Parse the tab-delimited lines into Album objects
        .apply("ParseAlbums", ParDo.of(new ParseAlbum()));

    // [START spanner_dataflow_write]
    albums
        // Spanner expects a Mutation object, so create it using the Album's data
        .apply("CreateAlbumMutation", ParDo.of(new DoFn<Album, Mutation>() {
          @ProcessElement
          public void processElement(ProcessContext c) {
            Album album = c.element();
            c.output(Mutation.newInsertOrUpdateBuilder("albums")
                .set("singerId").to(album.singerId)
                .set("albumId").to(album.albumId)
                .set("albumTitle").to(album.albumTitle)
                .build());
          }
        }))
        // Write mutations to Spanner
        .apply("WriteAlbums", SpannerIO.write()
            .withInstanceId(instanceId)
            .withDatabaseId(databaseId));
    // [END spanner_dataflow_write]

    p.run().waitUntilFinish();
  }
}
