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

package com.example.cloudsql.r2dbcsample;

import io.r2dbc.spi.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;

@Component
public class CommandLineRunnerSchemaCreator implements CommandLineRunner {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(CommandLineRunnerSchemaCreator.class);

  @Autowired
  private ConnectionFactory connectionFactory;

  @Override
  public void run(String... args) {
    runDdl("CREATE TABLE IF NOT EXISTS vote ( "
        + "vote_id SERIAL NOT NULL, "
        + "time_cast timestamp NOT NULL, "
        + "candidate CHAR(6) NOT NULL, "
        + "PRIMARY KEY (vote_id) );");
  }

  private void runDdl(String schema) {
    DatabaseClient client = DatabaseClient.create(connectionFactory);

    client.execute(schema)
        .fetch()
        .rowsUpdated()
        .block();

    LOGGER.info("Executed DDL: " + schema);
  }
}
