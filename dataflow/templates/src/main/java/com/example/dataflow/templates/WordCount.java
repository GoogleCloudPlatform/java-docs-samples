// Copyright 2018 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.dataflow.templates;

import java.util.Arrays;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.ValueProvider;
import org.apache.beam.sdk.options.ValueProvider.NestedValueProvider;
import org.apache.beam.sdk.options.ValueProvider.StaticValueProvider;
import org.apache.beam.sdk.transforms.Count;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.Filter;
import org.apache.beam.sdk.transforms.FlatMapElements;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.TypeDescriptors;


public class WordCount {
  // [START word_count_options]
  public interface WordCountOptions extends PipelineOptions {
    // Optional argument with a default value.
    @Description("Google Cloud Storage file pattern glob of the file(s) to read from")
    @Default.String("gs://apache-beam-samples/shakespeare/kinglear.txt")
    ValueProvider<String> getInputFile();

    void setInputFile(ValueProvider<String> value);

    // Required argument (made required via the metadata file).
    @Description("Google Cloud Storage bucket to store the outputs")
    ValueProvider<String> getOutputBucket();

    void setOutputBucket(ValueProvider<String> value);

    // Optional argument.
    @Description("Filter only words containing the specified substring")
    @Default.String("")
    ValueProvider<String> getWithSubstring();

    void setWithSubstring(ValueProvider<String> value);

    // Template option available only at template creation.
    @Description("Whether to make it case sensitive or not")
    @Default.Boolean(true)
    Boolean getIsCaseSensitive();

    void setIsCaseSensitive(Boolean value);
  }
  // [END word_count_options]

  // [START static_value_provider]
  static class FilterWithSubstring extends DoFn<String, String> {
    ValueProvider<String> substring;
    Boolean isCaseSensitive;

    FilterWithSubstring(ValueProvider<String> substring, Boolean isCaseSensitive) {
      this.substring = substring;
      this.isCaseSensitive = isCaseSensitive;
    }

    FilterWithSubstring(String substring, Boolean isCaseSensitive) {
      // This gives a static value to the ValueProvider.
      // It creates a more flexible interface for the DoFn.
      this(StaticValueProvider.of(substring), isCaseSensitive);
    }

    @ProcessElement
    public void processElement(ProcessContext c) {
      String word = c.element();
      String substring = this.substring.get();
      if (isCaseSensitive) {
        word = word.toLowerCase();
        substring = substring.toLowerCase();
      }
      if (word.contains(substring)) {
        c.output(word);
      }
    }
  }
  // [END static_value_provider]

  // [START value_provider]
  public static void main(String[] args) {
    WordCountOptions options = PipelineOptionsFactory.fromArgs(args)
        .withValidation().as(WordCountOptions.class);

    Pipeline pipeline = Pipeline.create(options);
    pipeline
        .apply("Read lines", TextIO.read().from(options.getInputFile()))
        // [END value_provider]
        .apply("Find words", FlatMapElements.into(TypeDescriptors.strings())
            .via((String line) -> Arrays.asList(line.split("[^\\p{L}]+"))))
        .apply("Filter empty words", Filter.by((String word) -> !word.isEmpty()))
        .apply("Filter with substring", ParDo.of(new FilterWithSubstring(
            options.getWithSubstring(), options.getIsCaseSensitive())))
        .apply("Count words", Count.perElement())
        .apply("Format results", MapElements.into(TypeDescriptors.strings())
            .via((KV<String, Long> wordCount) -> wordCount.getKey() + ": " + wordCount.getValue()))
        // [START nested_value_provider]
        .apply("Write results", TextIO.write().to(NestedValueProvider.of(
            options.getOutputBucket(),
            (String bucket) -> String.format("gs://%s/samples/dataflow/wordcount/outputs", bucket)
        )));
    // [END nested_value_provider]
    pipeline.run();
  }
}
