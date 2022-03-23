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

package com.example.cloud.bigtable.scheduledbackups;

import com.example.cloud.bigtable.scheduledbackups.resource.CreateBackupMessage;
import com.example.cloud.bigtable.scheduledbackups.resource.PubSubMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.bigtable.admin.v2.BigtableTableAdminClient;
import com.google.cloud.bigtable.admin.v2.BigtableTableAdminSettings;
import com.google.cloud.bigtable.admin.v2.models.Backup;
import com.google.cloud.bigtable.admin.v2.models.CreateBackupRequest;
import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.threeten.bp.Instant;

public class CreateBackup implements BackgroundFunction<PubSubMessage> {
  private static final Logger logger = Logger.getLogger(CreateBackup.class.getName());
  private static ObjectMapper mapper = new ObjectMapper();

  @Override
  public void accept(PubSubMessage message, Context context) {
    if (message != null && message.getData() != null) {
      logger.info("Trigger event:" + message.getData());

      try {
        String payload = new String(
            Base64.getDecoder().decode(message.getData().getBytes(StandardCharsets.UTF_8)),
            StandardCharsets.UTF_8);

        logger.info("Decoded payload:" + payload);
        CreateBackupMessage cbMessage = mapper.readValue(payload, CreateBackupMessage.class);
        logger.info("CreateBackup message:" + cbMessage.toString());
        logger.info("Submitting the create backup request");

        // Create an admin client
        BigtableTableAdminSettings adminSettings =
            BigtableTableAdminSettings.newBuilder()
            .setProjectId(cbMessage.getProjectId())
            .setInstanceId(cbMessage.getInstanceId()).build();
        try (BigtableTableAdminClient adminClient =
            BigtableTableAdminClient.create(adminSettings)) {
          CreateBackupRequest request =
              CreateBackupRequest.of(cbMessage.getClusterId(),
                  buildBackupId(cbMessage.getTableId()))
              .setSourceTableId(cbMessage.getTableId())
              .setExpireTime(buildExpireTime(cbMessage.getExpireHours()));
          Backup backupDetails = adminClient.createBackup(request);

          logger.info("Submitted backup request :" + backupDetails.getId()
                          + ": that will expire at:" + backupDetails.getExpireTime());
        } catch (IOException e) {
          logger.log(Level.SEVERE, "Caught Exception creating backup:"
              + e.toString(), e);
        }
      } catch (Exception e) {
        logger.log(Level.SEVERE, "Caught Exception running the create backup function:"
            + e.toString(), e);
      }
      return;
    }
  }

  private String buildBackupId(String tableName) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmm");
    return tableName + "-backup-" + sdf.format(new Date());
  }

  private Instant buildExpireTime(int expireHours) {
    return Instant.now().plusSeconds(expireHours * 3600);
  }
}
