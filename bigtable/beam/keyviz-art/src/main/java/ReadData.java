import com.google.cloud.bigtable.beam.AbstractCloudBigtableTableDoFn;
import com.google.cloud.bigtable.beam.CloudBigtableConfiguration;
import com.google.cloud.bigtable.beam.CloudBigtableTableConfiguration;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.FileSystems;
import org.apache.beam.sdk.io.GenerateSequence;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;
import org.apache.hadoop.hbase.filter.MultiRowRangeFilter;
import org.apache.hadoop.hbase.filter.MultiRowRangeFilter.RowRange;
import org.apache.hadoop.hbase.util.Bytes;
import org.joda.time.Duration;

public class ReadData {

  static final long KEY_VIZ_WINDOW_MINUTES = 15;
  static final long MAX_INPUT = 8000;
  static final String COLUMN_FAMILY = "cf1";

  public static void main(String[] args) {
    System.out.println("running main");
    BigtableOptions options =
        PipelineOptionsFactory.fromArgs(args).withValidation().as(BigtableOptions.class);
    Pipeline p = Pipeline.create(options);
    CloudBigtableTableConfiguration bigtableTableConfig =
        new CloudBigtableTableConfiguration.Builder()
            .withProjectId(options.getBigtableProjectId())
            .withInstanceId(options.getBigtableInstanceId())
            .withTableId(options.getBigtableTableId())
            .build();

    PCollection<Void> sequence =
        p.apply(GenerateSequence.from(0).withRate(1, new Duration(1000)))
            .apply(
                ParDo.of(
                    new DoFn<Long, Integer>() {
                      @ProcessElement
                      public void processElement(
                          @Element Long rowkey, OutputReceiver<Integer> out) {
                        Put row = new Put(Bytes.toBytes("" + rowkey));
                        long timestamp = System.currentTimeMillis();
                        long minutes = (timestamp / 1000) / 60;
                        out.output(Math.toIntExact(minutes / KEY_VIZ_WINDOW_MINUTES));
                      }
                    }))
            .apply(ParDo.of(new ReadFromTableFn(bigtableTableConfig)));
    p.run().waitUntilFinish();
  }

  static class ReadFromTableFn extends AbstractCloudBigtableTableDoFn<Integer, Void> {

    List<List<Float>> rows = new ArrayList<List<Float>>();
    String[] keys = new String[Math.toIntExact(MAX_INPUT + 1)];

    public ReadFromTableFn(CloudBigtableConfiguration config) {
      super(config);
      try {
        ReadableByteChannel chan =
            FileSystems.open(
                FileSystems.matchNewResource(
                    "gs://public-examples-testing-bucket/mona2.txt", false /* is_directory */));
        InputStream is = Channels.newInputStream(chan);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String line = null;

        while ((line = br.readLine()) != null) {
          //          System.out.println("Line entered : " + line.split(","));
          rows.add(
              Arrays.asList(line.split(",")).stream()
                  .map(x -> Float.valueOf(x))
                  .collect(Collectors.toList()));
        }

        int maxLength = ("" + MAX_INPUT).length();
        for (int i = 0; i < MAX_INPUT + 1; i++) {
          String paddedRowkey = String.format("%0" + maxLength + "d", i);
          String reversedRowkey = new StringBuilder(paddedRowkey).reverse().toString();
          keys[i] = "" + reversedRowkey;
        }
        Arrays.sort(keys);
        System.out.println(keys.length);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    @ProcessElement
    public void processElement(
        @Element Integer timeOffsetIndex, OutputReceiver<Void> out, PipelineOptions po) {
      //      System.out.println("timeOffsetIndex is " + timeOffsetIndex);
      BigtableOptions options = po.as(BigtableOptions.class);
      long count = 0;

      List<RowRange> ranges = new ArrayList<>();

      int numRows = rows.size();
      int numCols = rows.get(0).size();

      long rowHeight = MAX_INPUT / numRows;
      int columnIndex = timeOffsetIndex % numCols;
      for (int i = 0; i < rows.size(); i++) {
        System.out.println("rows.get(i) = " + rows.get(i));
        if (Math.random() <= rows.get(i).get(columnIndex)) {
          long startKeyI = MAX_INPUT - (i + 1) * rowHeight;
          long endKeyI = MAX_INPUT - i * rowHeight;

          String startKey = keys[Math.toIntExact(startKeyI)];
          String endKey = keys[Math.toIntExact(endKeyI)];
          System.out.println("startKeyI = " + startKeyI);
          System.out.println("endKeyI = " + endKeyI);
          System.out.println("startKey = " + startKey);
          System.out.println("endKey = " + endKey);
          ranges.add(
              new RowRange(
                  Bytes.toBytes("" + startKey), true,
                  Bytes.toBytes("" + endKey), false));
        }
      }

      if (ranges.size() == 0) {
        return;
      }

      try {
        Filter rangeFilters = new MultiRowRangeFilter(ranges);

        Scan scan =
            new Scan()
                .addFamily(Bytes.toBytes(COLUMN_FAMILY))
                .setFilter(new FilterList(new FirstKeyOnlyFilter(), rangeFilters));

        Table table = getConnection().getTable(TableName.valueOf(options.getBigtableTableId()));
        ResultScanner rows = table.getScanner(scan);

        for (Result row : rows) {
          count++;
          //          System.out.printf(
          //              "Reading data for %s%n",
          // Bytes.toString(row.rawCells()[0].getRowArray()));
        }
      } catch (Exception e) {
        System.out.println("error reading");
        e.printStackTrace();
      }
      System.out.printf("got %d rows\n", count);
    }
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