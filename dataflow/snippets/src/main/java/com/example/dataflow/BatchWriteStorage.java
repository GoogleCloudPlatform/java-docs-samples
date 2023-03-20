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

package com.example.dataflow;

// [START dataflow_batch_write_to_storage]
import java.util.Arrays;
import java.util.List;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.Compression;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Create;

public class BatchWriteStorage {
  public interface Options extends PipelineOptions {
    @Description("The Cloud Storage bucket to write to")
    String getBucketName();

    void setBucketName(String value);
  }

  // Write text data to Cloud Storage
  public static void main(String[] args) {
    final List<String> wordsList = Arrays.asList("1", "2", "3", "4");

    var options = PipelineOptionsFactory.fromArgs(args).withValidation().as(Options.class);
    var pipeline = Pipeline.create(options);
    pipeline
        .apply(Create
            .of(wordsList))
        .apply(TextIO
            .write()
            .to(options.getBucketName())
            .withSuffix(".txt")
            .withCompression(Compression.GZIP)
        );
    pipeline.run().waitUntilFinish();
  }
}
// [END dataflow_batch_write_to_storage]