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
import java.util.ArrayList;
import java.util.List;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.io.TextIO;
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
public class CsvToAvroTest implements Serializable {

  @Rule
  public final transient TestPipeline pipeline = TestPipeline.create();

  @BeforeClass
  public static void setUp() {
    PipelineOptionsFactory.register(SampleOptions.class);
  }

  @Test
  public void testCsvToAvro() throws Exception {
    SampleOptions options = TestPipeline.testingPipelineOptions().as(SampleOptions.class);
    options.setAvroSchema("gs://cloud-samples-data/storage/transformations/user.avsc");
    options.setInputFile("gs://cloud-samples-data/storage/transformations/input.csv");

    String schemaJson = getSchema(options.getAvroSchema());
    Schema schema = new Schema.Parser().parse(schemaJson);

    final List<GenericRecord> expectedResult = new ArrayList<>();
    GenericRecord genericRecordOne = new GenericData.Record(schema);
    genericRecordOne.put("first_name", "frank");
    genericRecordOne.put("last_name", "natividad");
    genericRecordOne.put("age", 1);
    expectedResult.add(genericRecordOne);
    GenericRecord genericRecordTwo = new GenericData.Record(schema);
    genericRecordTwo.put("first_name", "Karthi");
    genericRecordTwo.put("last_name", "thyagarajan");
    genericRecordTwo.put("age", 3);
    expectedResult.add(genericRecordTwo);

    final PCollection<GenericRecord> avroDataCollection = pipeline.apply("Read CSV files",
        TextIO.read().from(options.getInputFile()))
        .apply("Convert CSV to Avro formatted data", ParDo.of(
            new CsvToAvro.ConvertCsvToAvro(schemaJson, options.getCsvDelimiter())))
        .setCoder(AvroCoder.of(GenericRecord.class, schema));

    PAssert.that(avroDataCollection).containsInAnyOrder(expectedResult);

    pipeline.run().waitUntilFinish();
  }
}
