package functions;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.IncompleteKey;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.StringValue;
import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import com.google.common.util.concurrent.Uninterruptibles;
import functions.HelloWorld.PubSubMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;
import org.apache.commons.lang3.RandomStringUtils;

public class HelloWorld implements BackgroundFunction<PubSubMessage> {
  private static final Logger logger = Logger.getLogger(HelloWorld.class.getName());
  private static final int NANOS_PER_MICRO = 1_000;
  private static final int MICROS_PER_SECOND = 1_000_000;
  private static final int RANDOM_ID_BOUND = 10000;
  private static final int DURATION_MINUTES = 5;
  private static final int TARGET_QPS = 200;
  private static final int THREAD_COUNT = 25;

  private final Datastore datastore;
  private final String kind;
  // Create a Key factory to construct keys associated with this project.
  private final KeyFactory keyFactory;
  Random rand = new Random();

  public HelloWorld() {
    DatastoreOptions.Builder builder = DatastoreOptions.newBuilder();
    datastore = builder.build().getService();
    logger.info(datastore.toString());
    kind = "BillyFnGradient";
    keyFactory = datastore.newKeyFactory().setKind(kind);
  }

  public void addEntity() {
    int numRows = 10;
    int rowHeight = (int) (RANDOM_ID_BOUND / numRows);
    int id = randomId(rand);
    int rowNumber = id / rowHeight;

    float p = (float) rowNumber / numRows;
    if (Math.random() <= p) {

      FullEntity<Key> fullEntity =
          Entity.newBuilder(keyFactory.newKey(id))
              .set("description", getRandomStringValue())
              .build();
      // logger.info("writing entity" + fullEntity.getKey());

      Entity entity = datastore.put(fullEntity);
    }
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

  @Override
  public void accept(PubSubMessage message, Context context) {
    long periodMillis = 1_000 / TARGET_QPS * THREAD_COUNT;
    List<ScheduledFuture<?>> scheduledFutures = new ArrayList<>();
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(THREAD_COUNT);

    for (int i = 0; i < THREAD_COUNT; i++) {
      ScheduledFuture<?> writeFuture =
          executor.scheduleAtFixedRate(
              () -> {
                Random random = new Random();
                try {
                  addEntity();
                } catch (Exception e) {
                  // catch all exceptions to avoid stopping traffic generator
                  logger.info("Error when sending message" + e.getMessage());
                }
              },
              0,
              periodMillis,
              MILLISECONDS);
      scheduledFutures.add(writeFuture);
    }
    Uninterruptibles.sleepUninterruptibly(DURATION_MINUTES, MINUTES);
    scheduledFutures.forEach(future -> future.cancel(false));
    executor.shutdown();
    try {
      executor.awaitTermination(60, SECONDS);
    } catch (InterruptedException e) {
      throw new RuntimeException("Waiting termination when unexpectedly got interrupted", e);
    }
  }

  /** A message that is published by publishers and consumed by subscribers. */
  public static class PubSubMessage {
    String data;
    Map<String, String> attributes;
    String messageId;
    String publishTime;
  }
}
