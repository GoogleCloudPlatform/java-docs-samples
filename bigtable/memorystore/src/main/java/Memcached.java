/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.google.cloud.bigtable.data.v2.BigtableDataClient;
import com.google.cloud.bigtable.data.v2.models.Row;
import net.spy.memcached.MemcachedClient;
import java.net.InetSocketAddress;

public class Memcached {

  public static void main(String[] args) {
    memcachedBigtable("billy-testing-project", "testing-instance", "mobile-time-series",
        "localhost");
  }


  public static void memcachedBigtable(String projectId, String instanceId, String tableId,
      String hostname) {
    // String projectId = "my-project-id";
    // String instanceId = "my-instance-id";
    // String tableId = "mobile-time-series";
    // String hostname = "localhost";

    try {
      MemcachedClient mcc = new MemcachedClient(new InetSocketAddress(hostname, 11211));
      System.out.println("Connected to Memcached successfully");

      // Get value from cache
      String rowkey = "phone#4c410523#20190501";
      String columnFamily = "stats_summary";
      String column = "os_build";
      String cacheKey = String.format("%s:%s:%s", rowkey, columnFamily, column);
      System.out.println("getting from cache");
      Object value = mcc.get(cacheKey);
      System.out.println("got from cache");
      if (value != null) {
        System.out.println("Value fetched from cache: " + value);
      } else {
        System.out.println("didn't get value from cache");
        // Get data from Bigtable source and add to cache for 10 seconds.
        try (BigtableDataClient dataClient = BigtableDataClient.create(projectId, instanceId)) {
          Row row = dataClient.readRow(tableId, rowkey);
          String cellValue = row.getCells(columnFamily, column).get(0).getValue().toStringUtf8();
          System.out.println("got data from bt "+cellValue);
          // Set data into memcached server.
          mcc.set(cacheKey, 10, cellValue);
          System.out.println("Value fetched from Bigtable: " + cellValue);
        } catch (Exception e) {
          System.out.println("Could not set cache value.");
          e.printStackTrace();
        }
      }
      mcc.shutdown();
    } catch (Exception e) {
      System.out.println("Could not get cache value.");
      e.printStackTrace();
    }
  }
}
