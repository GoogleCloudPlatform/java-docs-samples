import com.google.cloud.bigtable.data.v2.BigtableDataClient;
import com.google.cloud.bigtable.data.v2.models.Row;
import net.spy.memcached.MemcachedClient;
import java.net.InetSocketAddress;

public class Memcached {

  public static void memcachedBigtable(String projectId, String instanceId, String tableId,
      String hostname) {

    // String projectId = "my-project-id";
    // String instanceId = "my-instance-id";
    // String tableId = "mobile-time-series";
    // String hostname = "0.0.0.0";

    // Connecting to Memcached server on localhost
    try {
      String hostname1 = "10.99.208.3";
      MemcachedClient mcc = new MemcachedClient(new
          InetSocketAddress(hostname1, 11211));
      System.out.println("Connection to server sucessfully");

      // Get value from cache
      String rowkey = "phone#4c410523#20190501";
      Object value = mcc.get(rowkey);

      if (value != null) {
        System.out.println("Get from Cache:" + value);
      } else {
        try (BigtableDataClient dataClient = BigtableDataClient.create(projectId, instanceId)) {
          Row row = dataClient.readRow(tableId, rowkey);
          // Set data into memcached server.
          System.out.println("set status:" + mcc.set(rowkey, 900, row));
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
