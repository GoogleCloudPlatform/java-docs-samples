package personalization;

import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableRow;
import com.google.api.services.bigquery.model.TableSchema;
import com.google.cloud.bigtable.beam.CloudBigtableIO;
import com.google.cloud.bigtable.beam.CloudBigtableScanConfiguration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.Read;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.CreateDisposition;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.WriteDisposition;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;

public class ExportTransactionsToBigQuery {

  static final String[] COLORS = {"red", "orange", "yellow", "green", "blue", "purple"};
  static final String[] ITEMS = {"blouse", "skirt", "dress", "hat", "shoes", "jacket"};

  // static String SESSION_TABLE_ID = "SessionHistory";
  // static String SESSION_TABLE_ID = "SessionHistory_seemany";
  // static String SESSION_TABLE_ID = "SessionHistory_only_change_colors";
  static String SESSION_TABLE_ID = "SessionHistory_seemany_buymany";
  static final String ENGAGEMENT_COLUMN_FAMILY = "engagement";
  static final String SALES_COLUMN_FAMILY = "sale";

  static TableSchema TABLE_SCHEMA = new TableSchema()
      .setFields(
          Arrays.asList(
              new TableFieldSchema()
                  .setName("user_id")
                  .setType("STRING")
                  .setMode("REQUIRED"),
              new TableFieldSchema()
                  .setName("item_id")
                  .setType("STRING")
                  .setMode("REQUIRED"),
              new TableFieldSchema()
                  .setName("progress")
                  .setType("INT64")
                  .setMode("REQUIRED")));

  public static void main(String[] args) {
    BigtableOptions options =
        PipelineOptionsFactory.fromArgs(args).withValidation().as(BigtableOptions.class);

    String project = "billy-testing-project";
    String instanceId = "personalization";
    String dataset = "my_dataset";
    String table = SESSION_TABLE_ID;
    Pipeline p = Pipeline.create(options);

    Scan scan = new Scan();
    // scan.setLimit(5);

    CloudBigtableScanConfiguration config =
        new CloudBigtableScanConfiguration.Builder()
            .withProjectId(project)
            .withInstanceId(instanceId)
            .withTableId(SESSION_TABLE_ID)
            .withScan(scan)
            .build();

    // [START bigtable_beam_helloworld_read_transforms]
    p.apply(Read.from(CloudBigtableIO.read(config)))
        .apply(
            ParDo.of(
                new DoFn<Result, TableRow>() {
                  @ProcessElement
                  public void processElement(@Element Result row, OutputReceiver<TableRow> out) {
                    // System.out.println(Bytes.toString(row.getRow()));
                    String userId = Bytes.toString(row.getRow()).split("#")[0];
                    for (Cell cell : row.rawCells()) {
                      String colFamily = Bytes.toString(CellUtil.cloneFamily(cell));

                      if (colFamily.equals(ENGAGEMENT_COLUMN_FAMILY)) {
                        String progressMessage = Bytes.toString(CellUtil.cloneValue(cell));
                        int progress = 1;
                        if (progressMessage.equals("Item details")) {
                          progress = 3;
                        }

                        out.output(new TableRow()
                            .set("user_id", userId)
                            .set("item_id", Bytes.toString(CellUtil.cloneQualifier(cell)))
                            .set("progress", progress));
                      } else {
                        String rawSales = Bytes.toString(CellUtil.cloneValue(cell));
                        rawSales = rawSales.replaceAll("\\[", "").replaceAll("\\]", "");
                        String[] sales = rawSales.split(",");

                        for (String sale : sales) {
                          out.output(new TableRow()
                              .set("user_id", userId)
                              .set("item_id", sale.trim())
                              .set("progress", 5));
                        }
                      }
                    }
                  }
                }
            ))
        .apply(
            "Write to BigQuery",
            BigQueryIO.writeTableRows()
                .to(String.format("%s:%s.%s", project, dataset, table))
                .withSchema(TABLE_SCHEMA)
                .withCreateDisposition(CreateDisposition.CREATE_IF_NEEDED)
                .withWriteDisposition(WriteDisposition.WRITE_TRUNCATE));

    List<TableRow> singleProductPeople = new ArrayList<>();

    for (String color : COLORS) {
      for (String item : ITEMS) {
        String productName = color + item;
        TableRow tableRow = new TableRow()
            .set("user_id", "productPredictor" + productName)
            .set("item_id", productName)
            .set("progress", 5);
        singleProductPeople.add(tableRow);
      }
    }

    p.apply(Create.of((singleProductPeople)))
        .apply(
            "Write to BigQuery",
            BigQueryIO.writeTableRows()
                .to(String.format("%s:%s.%s", project, dataset, table))
                .withSchema(TABLE_SCHEMA)
                .withCreateDisposition(CreateDisposition.CREATE_IF_NEEDED)
                .withWriteDisposition(WriteDisposition.WRITE_APPEND));

    p.run().waitUntilFinish();

  }

  public interface BigtableOptions extends DataflowPipelineOptions {

    @Description("The Bigtable instance ID")
    @Default.String("bigtable-instance")
    String getBigtableInstanceId();

    void setBigtableInstanceId(String bigtableInstanceId);

    @Description("The number of terabytes to set your Bigtable instance to have.")
    @Default.Double(1.5)
    Double getBigtableSize();

    void setBigtableSize(Double bigtableSize);
  }

}
