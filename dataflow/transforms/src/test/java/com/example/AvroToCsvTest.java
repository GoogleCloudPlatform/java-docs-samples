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

package com.example;

import static com.example.CsvToAvro.getSchema;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import org.apache.beam.sdk.io.AvroIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class AvroToCsvTest implements Serializable {

  @Rule
  public final transient TestPipeline pipeline = TestPipeline.create();

  @BeforeClass
  public static void setUp() {
    PipelineOptionsFactory.register(SampleOptions.class);
  }

  @Test
  public void testAvroToCsv() throws Exception {
    final List<String> expectedList = Arrays.asList("frank,natividad,1", "Karthi,thyagarajan,3");

    SampleOptions options = TestPipeline.testingPipelineOptions().as(SampleOptions.class);

    // Set test options
    options.setAvroSchema("gs://cloud-samples-data/storage/transformations/user.avsc");
    options.setInputFile("gs://cloud-samples-data/storage/transformations/input.avro");

    String schemaJson = getSchema(options.getAvroSchema());

    final PCollection<String> csvDataCollection = pipeline
        .apply("Read input", AvroIO.readGenericRecords(schemaJson).from(options.getInputFile()))
        .apply("Convert Avro to CSV formatted data",
            ParDo.of(new AvroToCsv.ConvertAvroToCsv(schemaJson, options.getCsvDelimiter())));

    System.out.println(csvDataCollection);
    PAssert.that(csvDataCollection).containsInAnyOrder(expectedList);

    pipeline.run().waitUntilFinish();
  }
}
