import com.google.cloud.bigtable.data.v2.BigtableDataClient;
import com.google.cloud.bigtable.data.v2.models.Row;
import net.spy.memcached.MemcachedClient;
import java.net.InetSocketAddress;

public class Memcached {

  private static String rowString;

  public static void main(String[] args) {
    memcachedBigtable("billy-testing-project", "testing-instance", "mobile-time-series",
        "localhost");
  }


  public static void memcachedBigtable(String projectId, String instanceId, String tableId,
      String hostname) {

    // String projectId = "my-project-id";
    // String instanceId = "my-instance-id";
    // String tableId = "mobile-time-series";
    // String hostname = "0.0.0.0";

    // Connecting to Memcached server on localhost
    try {
      // hostname = "10.99.208.3"
      MemcachedClient mcc = new MemcachedClient(new
          InetSocketAddress(hostname, 11211));
      System.out.println("Connection to server sucessfully");

      // Get value from cache
      String rowkey = "phone#4c410523#20190501";
      Object value = mcc.get(rowkey);

      if (value != null) {
        System.out.println("Value from cache: " + value);
      } else {
        try (BigtableDataClient dataClient = BigtableDataClient.create(projectId, instanceId)) {
          Row row = dataClient.readRow(tableId, rowkey);
          // Set data into memcached server.
          String rowString = row.toString();
          System.out.println("set status:" + mcc.set(rowkey, 10, rowString));
          System.out.println("Value from Bigtable is: " + rowString);
        } catch (Exception e) {
          System.out.println("Could not set cache value.");
          e.printStackTrace();
        }
      }
      mcc.shutdown();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
