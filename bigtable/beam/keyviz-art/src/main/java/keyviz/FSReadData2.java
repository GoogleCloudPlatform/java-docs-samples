package keyviz;


import com.google.cloud.bigtable.beam.CloudBigtableConfiguration;
import com.google.cloud.bigtable.hbase.BigtableConfiguration;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.EntityQuery;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StringValue;
import com.google.cloud.datastore.StructuredQuery.CompositeFilter;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import keyviz.LoadData.BigtableOptions;
import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.GenerateSequence;
import org.apache.beam.sdk.io.gcp.datastore.DatastoreIO;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.hadoop.hbase.client.Connection;
import org.joda.time.Duration;

public class FSReadData2 {

  static final long START_TIME = new Date().getTime();
  static final long KEY_VIZ_WINDOW_SECONDS = 10;

  public static void main(String[] args) {
    DataflowPipelineOptions options =
        PipelineOptionsFactory.fromArgs(args).withValidation().as(DataflowPipelineOptions.class);
    System.out.println("options");
    System.out.println(options);

    Pipeline p = Pipeline.create(options);
    // Initiates a new pipeline every second
    p.apply(GenerateSequence.from(0).withRate(1, new Duration(1000)))
        .apply(ParDo.of(new ReadFromTableFn()));

    p.run();
    System.exit(0);
  }

  public static class ReadFromTableFn extends DoFn<Long, Void> {

    List<List<Float>> imageData = new ArrayList<>();
    String[] keys;
    // protected final CloudBigtableConfiguration config;
    protected Connection connection;
    protected Datastore datastore;
    private KeyFactory keyFactory;
    // private final String kind = "BillyDF500reversed1mswithP";
    private final String kind = "BillyDFrw1s500rowsWithFetch";

    private static final int RANDOM_ID_BOUND = 500;
    Random rand = new Random();

    public ReadFromTableFn() {
      super();
      // downloadImageData(readDataOptions.getFilePath());
      // generateRowkeys(getNumRows(readDataOptions));
      // generateRowkeys(1000);
    }

    protected synchronized Datastore getDatastore() {
      if (this.connection == null) {
        DatastoreOptions.Builder builder = DatastoreOptions.newBuilder();
        datastore = builder.setProjectId("datastore-mode-kv-prod").build().getService();
        keyFactory = datastore.newKeyFactory().setKind(kind);
      }

      return datastore;
    }

    @ProcessElement
    public void processElement(@Element Long count, OutputReceiver<Void> out) {
      int numRows = 10;
      int rowHeight = (int) (RANDOM_ID_BOUND / numRows);
      int id = randomId(rand);
      List<FullEntity<com.google.cloud.datastore.Key>> entities = new ArrayList<>();
      System.out.println("processing elements");
      int maxInput = RANDOM_ID_BOUND;
      int maxLength = ("" + maxInput).length();
      // Make each number the same length by padding with 0s
      String numberFormat = "%0" + maxLength + "d";
      Datastore ds = getDatastore();
      int c = 0;

      if (count == 1) {
        for (int i = 0; i < maxInput; i++) {

          String paddedRowkey = String.format(numberFormat, i);
          String reversedRowkey = new StringBuilder(paddedRowkey).reverse().toString();
          int rowNumber = i / rowHeight;

          float p = (float) rowNumber / numRows;
          System.out.printf("index: %d rowNumber %d prob %f count %d", i, rowNumber, p, count);

          FullEntity<com.google.cloud.datastore.Key> fullEntity =
              com.google.cloud.datastore.Entity.newBuilder(keyFactory.newKey(reversedRowkey))
                  .set("description", getRandomStringValue())
                  .build();
          entities.add(fullEntity);
          // logger.info("writing entity" + fullEntity.getKey());
          c++;

          // 500 per write limit
          if (i % 500 == 0) {
            System.out.printf("500 mod 0");
            ds.put(entities.toArray(new FullEntity<?>[0]));
            entities = new ArrayList<>();
          }
        }

        System.out.println(c + " entities written");

        ds.put(entities.toArray(new FullEntity<?>[0]));
      }

      EntityQuery.Builder query = Query.newEntityQueryBuilder()
          .setKind(kind);

      // if ((count / (2 * 60 * 1000)) % 2 == 0) {
      //   query.setFilter(oddRangeFilter);
      // } else {
      //   query.setFilter(evenRangeFilter);
      // }
      QueryResults<Entity> entityQueryResults = ds.run(query.build());
      c = 0;
      // while (entityQueryResults.hasNext()) {
      //   Entity entity = entityQueryResults.next();
      //   System.out.println(entity);
      //   c++;
      // }

      long timestampDiff = System.currentTimeMillis() - START_TIME;
      long seconds = timestampDiff / 1000;
      int timeOffsetIndex = Math.toIntExact(seconds / KEY_VIZ_WINDOW_SECONDS);

      List<com.google.cloud.datastore.Key> keysToFetch = new ArrayList<>();
      for (int i = 0; i < maxInput; i++) {
        if (timeOffsetIndex % 2 == 0) {
          String paddedRowkey = String.format(numberFormat, i);
          String reversedRowkey = new StringBuilder(paddedRowkey).reverse().toString();
          keysToFetch.add(keyFactory.newKey(reversedRowkey));
          c++;
        }
      }
      datastore.fetch();
      System.out.println(c + " entities fetched");

      // String kind = "Billy10ms1krs";
      // int maxInput = 1000;
      // int maxLength = ("" + maxInput).length();
      // // Make each number the same length by padding with 0s
      // String numberFormat = "%0" + maxLength + "d";
      //
      // for (int i = 0; i < maxInput; i++) {
      //   byte[] b = new byte[(int) 1000];
      //   new Random().nextBytes(b);
      //
      //   String paddedRowkey = String.format(numberFormat, i);
      //   String reversedRowkey = new StringBuilder(paddedRowkey).reverse().toString();
      //
      //   Entity entity = Entity.newBuilder()
      //       .setKey(makeKey(kind, "testing" + reversedRowkey))
      //       .putProperties(
      //           "long",
      //           makeValue(b.toString()).setExcludeFromIndexes(true).build()
      //       ).build();
      //
      //   out.output(entity);
      // }
    }

    private StringValue getRandomStringValue() {
      String randomString =
          RandomStringUtils.random(
              1000,
              /* start= */ 0,
              /* end= */ 0,
              /* letters= */ true,
              /* numbers= */ true,
              /* chars= */ null);
      return StringValue.newBuilder(randomString).setExcludeFromIndexes(true).build();
    }

    private int randomId(Random rand) {
      return rand.nextInt(RANDOM_ID_BOUND) + 1;
    }
  }

  public interface ReadDataOptions extends BigtableOptions {

    @Description("The number of gigabytes written using the load data script.")
    @Default.Double(40)
    double getGigabytesWritten();

    void setGigabytesWritten(double gigabytesWritten);

    @Description("The number of megabytes per row written using the load data script.")
    @Default.Long(5)
    long getMegabytesPerRow();

    void setMegabytesPerRow(long megabytesPerRow);

    // See README for more images to use
    @Description("The file containing the pixels to draw.")
    @Default.String("gs://keyviz-art/mona_lisa_8h.txt")
    String getFilePath();

    void setFilePath(String filePath);
  }

}