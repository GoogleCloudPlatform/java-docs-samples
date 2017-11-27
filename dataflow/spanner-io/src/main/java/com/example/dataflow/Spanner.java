package com.example.dataflow;

import com.google.cloud.spanner.Struct;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.io.gcp.spanner.SpannerIO;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.Validation;
import org.apache.beam.sdk.transforms.Count;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.Sum;
import org.apache.beam.sdk.transforms.ToString;
import org.apache.beam.sdk.values.PCollection;

/**
 * Created by dcavazos on 10/18/17.
 */
public class Spanner {

  public interface Options extends PipelineOptions {
    @Description("Spanner instance ID to query from")
    @Validation.Required
    String getInstanceId();
    void setInstanceId(String value);

    @Description("Spanner database name to query from")
    @Validation.Required
    String getDatabaseName();
    void setDatabaseName(String value);

    @Description("Spanner table name to query from")
    @Validation.Required
    String getTableName();
    void setTableName(String value);

    @Description("Output filename for records count")
    @Validation.Required
    String getOutputCount();
    void setOutputCount(String value);

    @Description("Output filename for records size")
    @Validation.Required
    String getOutputSize();
    void setOutputSize(String value);
  }

  private static class EstimateStructSizeFn extends DoFn<Struct, Long> {
    @ProcessElement public void processElement(ProcessContext c) throws Exception {
      Struct struct = c.element();
      long result = 0;
      for (int i = 0; i < struct.getColumnCount(); i++) {
        if (struct.isNull(i)) {
          continue;
        }

        switch (struct.getColumnType(i).getCode()) {
          case BOOL:
            result += 1;
            break;
          case INT64:
          case FLOAT64:
            result += 8;
            break;
          case BYTES:
            result += struct.getBytes(i).length();
            break;
          case STRING:
            result += struct.getString(i).length();
            break;
          case TIMESTAMP:
          case DATE:
            result += 12;
            break;
          case ARRAY:
            throw new IllegalArgumentException("Arrays are not supported :(");
          case STRUCT:
            throw new IllegalArgumentException("Structs are not supported :(");
        }
      }
      c.output(result);
    }
  }

  public static void main(String[] args) {
    Options options = PipelineOptionsFactory.fromArgs(args).withValidation().as(Options.class);

    Pipeline p = Pipeline.create(options);

    PCollection<Struct> records = p
        .apply(SpannerIO.read()
            .withInstanceId(options.getInstanceId())
            .withDatabaseId(options.getDatabaseName())
            .withQuery("SELECT * FROM " + options.getTableName())
        );

    records
        .apply(Count.globally())
        .apply(ToString.elements())
        .apply(TextIO.write().to(options.getOutputCount()));

    records
        .apply(ParDo.of(new EstimateStructSizeFn()))
        .apply(Sum.longsGlobally())
        .apply(ToString.elements())
        .apply(TextIO.write().to(options.getOutputSize()));

    p.run().waitUntilFinish();
  }
}
