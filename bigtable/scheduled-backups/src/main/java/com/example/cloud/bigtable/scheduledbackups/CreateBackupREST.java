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
import com.example.cloud.bigtable.scheduledbackups.resource.CreateBackupRequestHTTP;
import com.example.cloud.bigtable.scheduledbackups.resource.PubSubMessage;
import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.map.ObjectMapper;

//CHECKSTYLE OFF: AbbreviationAsWordInName
public class CreateBackupREST implements BackgroundFunction<PubSubMessage> {
  //CHECKSTYLE ON: AbbreviationAsWordInName
  private static final Logger logger = Logger.getLogger(CreateBackupREST.class.getName());

  private static ObjectMapper mapper = new ObjectMapper();

  public static final String BT_ADMIN_BASE_URL = "https://bigtableadmin.googleapis.com/v2";

  @Override
  public void accept(PubSubMessage message, Context context) {

    if (message != null && message.getData() != null) {
      logger.info("Trigger event:" + message.getData());

      HttpURLConnection conn = null;

      try {
        String payload = new String(
            Base64.getDecoder().decode(message.getData().getBytes(StandardCharsets.UTF_8)),
            StandardCharsets.UTF_8);

        logger.info("Decoded payload:" + payload);
        CreateBackupMessage cbMessage = mapper.readValue(payload, CreateBackupMessage.class);

        logger.info("CreateBackup message:" + cbMessage.toString());
        logger.info("Submitting the create backup request");

        URL url = new URL(BT_ADMIN_BASE_URL + "/projects/" + cbMessage.getProjectId()
                              + "/instances/" + cbMessage.getInstanceId()
                              + "/clusters/" + cbMessage.getClusterId()
                              + "/backups?backupId=" + buildBackupId(cbMessage.getTableId()));

        conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");

        String sourceTableField = "projects/" + cbMessage.getProjectId()
            + "/instances/" + cbMessage.getInstanceId()
            + "/tables/" + cbMessage.getTableId();

        CreateBackupRequestHTTP request = new CreateBackupRequestHTTP();
        request.setSourceTable(sourceTableField);
        request.setExpireTime(buildExpireTime(cbMessage.getExpireHours()));

        String requestBody = mapper.writeValueAsString(request);

        OutputStream os = conn.getOutputStream();
        os.write(requestBody.getBytes());
        os.flush();

        if (conn.getResponseCode() != 200) {
          logger.warning("Create backup of Bigtable table:" + cbMessage.getTableId()
                             + " failed with response code " + conn.getResponseCode());
        }

        logger.info(conn.getResponseMessage());
        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

        String output;
        logger.info("Response .... \n");
        while ((output = br.readLine()) != null) {
          logger.info(output);
        }
      } catch (Exception e) {
        logger.log(Level.WARNING, "Caught Exception running a BT backup function:"
            + e.toString(), e);
      } finally {
        try {
          if (conn != null) {
            conn.disconnect();
          }
        } catch (Exception fex) {
          logger.warning("Caught Exception closing the connection:" + fex.toString());
        }
      }
    }
    return;
  }

  String buildBackupId(String tableName) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmm");
    return tableName + "-backup-" + sdf.format(new Date());
  }

  String buildExpireTime(int expireHours) {
    return Instant.now().plusSeconds(expireHours * 3600).toString();
  }
}
