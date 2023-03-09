package com.example.dataflow;

// [START dataflow_bigquery_read_projection_and_filtering]
import com.google.api.services.bigquery.model.TableRow;
import java.util.Arrays;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.TypedRead;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.values.TypeDescriptor;

public class BigQueryReadWithProjectionAndFiltering {
  public static void main(String[] args) {
    PipelineOptionsFactory.register(BigQueryReadOptions.class);
    var options = PipelineOptionsFactory.fromArgs(args)
        .withValidation()
        .as(BigQueryReadOptions.class);

    var pipeline = Pipeline.create(options);
    pipeline
        .apply(BigQueryIO.readTableRows()
            // Read rows from a specified table.
            .from(String.format("%s:%s.%s",
                options.getProjectId(),
                options.getDatasetName(),
                options.getTableName()))
            .withMethod(TypedRead.Method.DIRECT_READ)
            .withSelectedFields(Arrays.asList("user_name", "age"))
            .withRowRestriction("age > 18")
        )
        // The output from the previous step is a PCollection<TableRow>.
        .apply(MapElements
            .into(TypeDescriptor.of(TableRow.class))
            // Use TableRow to access individual fields in the row.
            .via((TableRow row) -> {
              var name = (String) row.get("user_name");
              var age = row.get("age");
              System.out.printf("Name: %s, Age: %s%n", name, age);
              return row;
            }));
    pipeline.run().waitUntilFinish();
  }
}
// [END dataflow_bigquery_read_projection_and_filtering]
