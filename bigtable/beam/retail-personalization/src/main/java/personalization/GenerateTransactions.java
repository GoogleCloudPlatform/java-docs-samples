package personalization;

import com.google.cloud.bigtable.beam.CloudBigtableIO;
import com.google.cloud.bigtable.beam.CloudBigtableTableConfiguration;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.GenerateSequence;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PDone;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

public class GenerateTransactions {

  static final String[] COLORS = {"red", "orange", "yellow", "green", "blue", "purple"};
  static final String[] ITEMS = {"blouse", "skirt", "dress", "hat", "shoes", "jacket"};
  // static final int NUM_CUSTOMERS = 25_000;
  static final int NUM_CUSTOMERS = 250;
  // static final int NUM_TRANSACTIONS = NUM_CUSTOMERS * 4;
  static final int NUM_TRANSACTIONS = NUM_CUSTOMERS * 2;
  static Shopper[] shoppers = new Shopper[NUM_CUSTOMERS];
  static SecureRandom rand = new SecureRandom();
  // static String SESSION_TABLE_ID = "SessionHistory";
  // static String SESSION_TABLE_ID = "SessionHistory_only_change_colors";
  // static String SESSION_TABLE_ID = "SessionHistory_buy5";
  // static String SESSION_TABLE_ID = "SessionHistory_seemany";
  static String SESSION_TABLE_ID = "SessionHistory_seemany_buymany";
  static final String ENGAGEMENT_COLUMN_FAMILY = "engagement";
  static final String SALES_COLUMN_FAMILY = "sale";

  public static void main(String[] args) {

    for (int i = 0; i < NUM_CUSTOMERS; i++) {
      int colorI = new Random().nextInt(COLORS.length);
      int itemI = new Random().nextInt(ITEMS.length);

      shoppers[i] = new Shopper(COLORS[colorI], ITEMS[itemI], NUM_CUSTOMERS);
    }
    BigtableOptions options =
        PipelineOptionsFactory.fromArgs(args).withValidation().as(BigtableOptions.class);

    Pipeline p = Pipeline.create(options);

    p
        .apply("generate data", GenerateSequence.from(0).to(NUM_TRANSACTIONS)
            // .withRate(rate, Duration.standardSeconds(1))
        )
        .apply("create mutations",
            ParDo.of(new CreateTransactionMutationFn()))
        .apply("writing data",
            CloudBigtableIO.writeToTable(new CloudBigtableTableConfiguration.Builder()
                .withProjectId(options.getProject())
                .withInstanceId(options.getBigtableInstanceId())
                .withTableId(SESSION_TABLE_ID)
                .build()));

    p.run().waitUntilFinish();
  }

  static class CreateTransactionMutationFn extends DoFn<Long, Mutation> {

    @ProcessElement
    public void processElement(@Element Long transaction, OutputReceiver<Mutation> out) {
      Shopper shopper = shoppers[rand.nextInt(shoppers.length)];

      Put row = new Put(Bytes.toBytes(shopper.getUsername() + "#" + new Date().getTime()));

      // Make list of all items
      // Denote which items user is interested in
      // Look at items

      Set<String> potentialItems = new HashSet<>();
      ArrayList<String> allItems = new ArrayList<>();
      for (String color : COLORS) {
        for (String item : ITEMS) {
          allItems.add(color + item);
        }
      }

      Collections.shuffle(allItems);

      for (String color : COLORS) {
        potentialItems.add(color + shopper.item);
      }

      for (String item : ITEMS) {
        potentialItems.add(shopper.color + item);
      }

      int numItems = rand.nextInt(3) + 1;

      ArrayList<String> purchasedItems = new ArrayList<>();
      int curIndex = 0;

      // Works and is wonderful! commenting to lock in
      // for (String item : allItems) {
      //   row.addColumn(
      //       Bytes.toBytes(ENGAGEMENT_COLUMN_FAMILY),
      //       Bytes.toBytes(item),
      //       Bytes.toBytes("Spotted item"));
      //
      //   if (potentialItems.contains(item)) {
      //     row.addColumn(
      //         Bytes.toBytes(ENGAGEMENT_COLUMN_FAMILY),
      //         Bytes.toBytes(item),
      //         Bytes.toBytes("Item details"));
      //
      //     // Move onto purchase with 30% probability.
      //     if (.3 >= rand.nextDouble()) {
      //       purchasedItems.add(item);
      //     }
      //   }
      // }


      for (String item : allItems) {
        if (rand.nextDouble() < .5) {
          continue;
        }

        row.addColumn(
            Bytes.toBytes(ENGAGEMENT_COLUMN_FAMILY),
            Bytes.toBytes(item),
            Bytes.toBytes("Spotted item"));

        if (potentialItems.contains(item)) {
          row.addColumn(
              Bytes.toBytes(ENGAGEMENT_COLUMN_FAMILY),
              Bytes.toBytes(item),
              Bytes.toBytes("Item details"));

          // Move onto purchase with 30% probability.
          if (.3 >= rand.nextDouble()) {
            purchasedItems.add(item);
          }
        }
      }

      // for (int i = 0; i < numItems; i++) {
      //   // See item on page.
      //   String currentItem = allItems.get(curIndex);
      //   while (!potentialItems.contains(allItems.get(curIndex))) {
      //     row.addColumn(
      //         Bytes.toBytes(ENGAGEMENT_COLUMN_FAMILY),
      //         Bytes.toBytes(currentItem),
      //         Bytes.toBytes("Spotted item"));
      //     curIndex++;
      //     currentItem = allItems.get(curIndex);
      //   }
      //   curIndex++;
      //
      //   // // Move onto details page with 50% probability.
      //   // rand.nextDouble();
      //   // if (.5 <= rand.nextDouble()) {
      //   //   continue;
      //   // }
      //   row.addColumn(
      //       Bytes.toBytes(ENGAGEMENT_COLUMN_FAMILY),
      //       Bytes.toBytes(currentItem),
      //       Bytes.toBytes("Item details"));
      //
      //   // Move onto purchase with 30% probability.
      //   if (.3 <= rand.nextDouble()) {
      //     continue;
      //   }
      //   purchasedItems.add(currentItem);
      //
      // }

      if (!purchasedItems.isEmpty()) {
        row.addColumn(
            Bytes.toBytes(SALES_COLUMN_FAMILY),
            Bytes.toBytes(""),
            Bytes.toBytes(purchasedItems.toString()));
      }
      out.output(row);
    }
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

class Shopper {

  String color;
  String item;
  int id;
  static SecureRandom rand = new SecureRandom();

  Shopper(String color, String item, int numCustomers) {
    this.color = color;
    this.item = item;
    this.id = rand.nextInt(numCustomers);
  }

  public String getUsername() {
    return color + item + id;
  }
}