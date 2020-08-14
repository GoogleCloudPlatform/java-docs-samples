import net.spy.memcached.MemcachedClient;
import java.net.InetSocketAddress;

public class Memcached {

  public static void main(String[] args) {
    // Connecting to Memcached server on localhost
    try {
      MemcachedClient mcc = new MemcachedClient(new
          InetSocketAddress("10.99.208.3", 11211));
      System.out.println("Connection to server sucessfully");

      //not set data into memcached server
      System.out.println("set status:" + mcc.set("tutorialspoint", 900, "memcached"));

      //Get value from cache
      System.out.println("Get from Cache:" + mcc.get("tutorialspoint"));

      mcc.shutdown();
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

}
