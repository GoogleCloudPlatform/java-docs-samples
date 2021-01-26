/*
 * Copyright 2021 Google LLC
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

package com.example.bigtable;

// [START bigtable_configure_connection_pool]


import static com.google.cloud.bigtable.hbase.BigtableOptionsFactory.BIGTABLE_DATA_CHANNEL_COUNT_KEY;

import com.google.cloud.bigtable.hbase.BigtableConfiguration;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Connection;

public class ConfigureConnectionPool {

  public static void configureConnectionPool(String projectId, String instanceId) {
    // String projectId = "my-project-id";
    // String instanceId = "my-instance-id";
    Configuration config = BigtableConfiguration.configure(projectId, instanceId);
    config.setInt(BIGTABLE_DATA_CHANNEL_COUNT_KEY, 250);
    try (Connection connection = BigtableConfiguration.connect(config)) {
      int poolSize = connection.getConfiguration().getInt(BIGTABLE_DATA_CHANNEL_COUNT_KEY, 0);

      System.out.println(String.format("Connected with pool size of %d", poolSize));
    } catch (Exception e) {
      System.out.println("Error during ConfigureConnectionPool: \n" + e.toString());
    }
  }
}

// [END bigtable_configure_connection_pool]
