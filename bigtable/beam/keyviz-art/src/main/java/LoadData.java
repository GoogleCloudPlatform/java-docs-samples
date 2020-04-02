import com.google.cloud.bigtable.beam.CloudBigtableIO;
import com.google.cloud.bigtable.beam.CloudBigtableTableConfiguration;
import java.util.Random;
import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.GenerateSequence;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

public class LoadData {

  public static void main(String[] args) {

    BigtableOptions options =
        PipelineOptionsFactory.fromArgs(args).withValidation().as(BigtableOptions.class);
    Pipeline p = Pipeline.create(options);
    CloudBigtableTableConfiguration bigtableTableConfig =
        new CloudBigtableTableConfiguration.Builder()
            .withProjectId(options.getBigtableProjectId())
            .withInstanceId(options.getBigtableInstanceId())
            .withTableId(options.getBigtableTableId())
            .build();

    // final int max = 1000000;
    final long ONE_MB = 1000 * 1000;
    final long ONE_GB = 1000 * ONE_MB;
    final long FORTY_GIGS = 40 * ONE_GB;
    final long ROW_SIZE = 5 * ONE_MB;
    final long max = FORTY_GIGS / ROW_SIZE;
    // final int max = 30 * 1000;
    p.apply(GenerateSequence.from(0).to(max))
        // p.apply(GenerateSequence.from(0).to(10))
        .apply(
            ParDo.of(
                new DoFn<Long, Mutation>() {
                  @ProcessElement
                  public void processElement(@Element Long rowkey, OutputReceiver<Mutation> out) {
                    int maxLength = ("" + max).length();
                    // Make each number the same length by padding with 0s.
                    String paddedRowkey = String.format("%0" + maxLength + "d", rowkey);
                    String reversedRowkey = new StringBuilder(paddedRowkey).reverse().toString();
                    System.out.println("reversedRowkey = " + reversedRowkey);

                    Put row = new Put(Bytes.toBytes(reversedRowkey));

                    // Generate 5 random bytes
                    // byte[] b = new byte[(int) ROW_SIZE];
                    byte[] b = new byte[1];
                    new Random().nextBytes(b);

                    long timestamp = System.currentTimeMillis();

                    row.addColumn(Bytes.toBytes("cf1"), Bytes.toBytes("C"), timestamp, b);
                    out.output(row);
                  }
                }))
        .apply(CloudBigtableIO.writeToTable(bigtableTableConfig));

    p.run().waitUntilFinish();
  }

  public interface BigtableOptions extends DataflowPipelineOptions {

    @Description("The Bigtable project ID, this can be different than your Dataflow project")
    @Default.String("bigtable-project")
    String getBigtableProjectId();

    void setBigtableProjectId(String bigtableProjectId);

    @Description("The Bigtable instance ID")
    @Default.String("bigtable-instance")
    String getBigtableInstanceId();

    void setBigtableInstanceId(String bigtableInstanceId);

    @Description("The Bigtable table ID in the instance.")
    @Default.String("bigtable-table")
    String getBigtableTableId();

    void setBigtableTableId(String bigtableTableId);
  }
}