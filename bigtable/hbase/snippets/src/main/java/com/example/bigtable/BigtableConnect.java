/*
 * Copyright 2018 Google LLC
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

import com.google.cloud.bigtable.hbase.BigtableConfiguration;
import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;

public class BigtableConnect {

  public static String projectId;
  public static String instanceId;
  public static String appProfileId = "default";
  public static Connection connection = null;

  public static void main(String... args) {
    projectId = args[0]; // my-gcp-project-id
    instanceId = args[1]; // my-bigtable-instance-id

    if (args.length > 2) {
      appProfileId = args[2]; // my-bigtable-app-profile-id or default if not provided.
    }
  }

  // [START bigtable_connect]
  public static void connect() throws IOException {
    connection = BigtableConfiguration.connect(projectId, instanceId);
  }
  // [END bigtable_connect]

  // [START bigtable_connect_app_profile]
  public static void connectWithAppProfile() throws IOException {
    connection = BigtableConfiguration.connect(projectId, instanceId, appProfileId);
  }
  // [END bigtable_connect_app_profile]

  // [START bigtable_connect_with_configuration]
  public static void connectWithConfiguration() throws IOException {
    // Define the HBase configuration with the projectID, instanceID, and optional appProfileID
    // from resources/hbase_site.xml
    Configuration config = HBaseConfiguration.create();
    connection = ConnectionFactory.createConnection(config);
  }
  // [END bigtable_connect_with_configuration]

  protected void closeConnection() throws IOException {
    connection.close();
  }
}
