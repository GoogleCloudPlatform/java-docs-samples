/**
 * This code snippet is part of a tutorial on how to use Memorystore for Redis.
 *
 * <p>See https://cloud.google.com/memorystore/docs/valkey/create-instances before running the code
 * snippet.
 *
 * <p>Prerequisites: 1. A running Memorystore for Redis instance in Google Cloud.
 *
 * <p>Replace "INSTANCE_ID" with the private IP of your Memorystore instance. Replace "ITEM_ID" and
 * "ITEM_VALUE" with the key and value to be cached.
 */
import redis.clients.jedis.Jedis;

public class MemorystoreWriteItem {

  public static void main(String[] args) {
    /** Connect to the Memorystore Redis instance */
    Jedis jedis = new Jedis("127.0.0.1", 6379);

    /** Replace with the item ID and value to cache */
    String itemId = "foo";
    String itemValue = "bar";

    /** Write the item to the cache */
    jedis.set(itemId, itemValue);

    /** Print out the cached result */
    System.out.println("Item cached with ID: " + itemId + " and value: " + itemValue);

    /** Close the connection */
    jedis.close();
  }
}
