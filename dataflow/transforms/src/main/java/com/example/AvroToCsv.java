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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.AvroIO;
import org.apache.beam.sdk.io.FileSystems;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.commons.csv.CSVFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AvroToCsv {

  private static final Logger LOG = LoggerFactory.getLogger(AvroToCsv.class);
  private static final List<String> acceptedTypes = Arrays.asList(
      new String[]{"string", "boolean", "int", "long", "float", "double"});

  private static String getSchema(String schemaPath) throws IOException {
    ReadableByteChannel channel = FileSystems.open(FileSystems.matchNewResource(
        schemaPath, false));

    try (InputStream stream = Channels.newInputStream(channel)) {
      BufferedReader streamReader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
      StringBuilder dataBuilder = new StringBuilder();

      String line;
      while ((line = streamReader.readLine()) != null) {
        dataBuilder.append(line);
      }

      return dataBuilder.toString();
    }
  }

  public static void checkFieldTypes(Schema schema) throws IllegalArgumentException {
    for (Schema.Field field : schema.getFields()) {
      String fieldType = field.schema().getType().getName().toLowerCase();
      if (!acceptedTypes.contains(fieldType)) {
        LOG.error("Data transformation doesn't support: " + fieldType);
        throw new IllegalArgumentException("Field type " + fieldType + " is not supported.");
      }
    }
  }

  public static class ConvertAvroToCsv extends DoFn<GenericRecord, String> {

    private CSVFormat csvFormat;
    private String schemaJson;

    public ConvertAvroToCsv(String schemaJson, CSVFormat csvFormat) {
      this.schemaJson = schemaJson;
      this.csvFormat = csvFormat;
    }

    @ProcessElement
    public void processElement(ProcessContext ctx) throws IOException {
      GenericRecord genericRecord = ctx.element();
      Schema schema = new Schema.Parser().parse(schemaJson);

      StringBuilder sb = new StringBuilder();
      List<Object> values = schema.getFields().stream().map(
              f -> genericRecord.get(f.name())).collect(Collectors.toList());
      csvFormat.printRecord(sb,values.toArray());
      ctx.output(sb.toString());
    }
  }

  public static void runAvroToCsv(SampleOptions options)
      throws IOException, IllegalArgumentException {
    FileSystems.setDefaultPipelineOptions(options);

    // Get Avro Schema
    String schemaJson = getSchema(options.getAvroSchema());
    Schema schema = new Schema.Parser().parse(schemaJson);

    // Check schema field types before starting the Dataflow job
    checkFieldTypes(schema);

    CSVFormat csvFormat = CSVFormatUtils.getCsvFormat(options.getCsvFormat());

    // Create the Pipeline object with the options we defined above.
    Pipeline pipeline = Pipeline.create(options);

    // Convert Avro To CSV
    pipeline.apply("Read Avro files",
        AvroIO.readGenericRecords(schemaJson).from(options.getInputFile()))
        .apply("Convert Avro to CSV formatted data",
            ParDo.of(new ConvertAvroToCsv(schemaJson, csvFormat)))
        .apply("Write CSV formatted data",
            // we do not need a delimiter as it will be added by the previous transform
            TextIO.write().withDelimiter(new char[0]).to(options.getOutput()).withSuffix(".csv"));

    // Run the pipeline.
    pipeline.run().waitUntilFinish();
  }

  public static void main(String[] args) throws IOException, IllegalArgumentException {
    // Create and set your PipelineOptions.
    PipelineOptionsFactory.register(SampleOptions.class);
    SampleOptions options = PipelineOptionsFactory.fromArgs(args).withValidation()
        .as(SampleOptions.class);

    runAvroToCsv(options);
  }
}
