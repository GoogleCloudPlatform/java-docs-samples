import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.hbase.HBaseIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.DoFn.ProcessElement;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;

public class HbaseDataflow {
  public static void main(String[] args) {
    System.out.println("hi!");
    DataflowPipelineOptions options =
        PipelineOptionsFactory.fromArgs(args).withValidation().as(DataflowPipelineOptions.class);
    Configuration config = HBaseConfiguration.create();
    // Configuration config = new Configuration(true);
    // config.set("hbase.zookeeper.quorum", "10.240.0.63:2181");
    // config.set("hbase.master", "10.240.0.63:16000");
    Pipeline p = Pipeline.create(options);

    System.out.println(config);

    Scan scan = new Scan();
    scan.setCacheBlocks(false);
    System.out.println("hi22!");
    // p.apply(Create.of("phone#4c410523#20190501", "phone#4c410523#20190502"))
    //     .apply(
    //         ParDo.of(
    //             new DoFn<String, Void>() {
    //               @ProcessElement
    //               public void processElement(@Element String row, OutputReceiver<Void> out) {
    //                 System.out.println("hi3!");
    //                 System.out.println(row);
    //               }
    //             }));


    p.apply("read", HBaseIO.read().withConfiguration(config).withTableId("my-table").withScan(scan))
        .apply(
            ParDo.of(
                new DoFn<Result, Void>() {
                  @ProcessElement
                  public void processElement(@Element Result row, OutputReceiver<Void> out) {
                    System.out.println("hi3!");
                    printRow(row);
                  }
                }));

    p.run();
    System.exit(0);
  }

  private static void printRow(Result row) {
    System.out.printf("Reading data for %s%n", Bytes.toString(row.getRow()));
    String colFamily = "";
    for (Cell cell : row.rawCells()) {
      String currentFamily = Bytes.toString(CellUtil.cloneFamily(cell));
      if (!currentFamily.equals(colFamily)) {
        colFamily = currentFamily;
        System.out.printf("Column Family %s%n", colFamily);
      }
      System.out.printf(
          "\t%s: %s @%s%n",
          Bytes.toString(CellUtil.cloneQualifier(cell)),
          Bytes.toString(CellUtil.cloneValue(cell)),
          cell.getTimestamp());
    }
    System.out.println();
  }
}
