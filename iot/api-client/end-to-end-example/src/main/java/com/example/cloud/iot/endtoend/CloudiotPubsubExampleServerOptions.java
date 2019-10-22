/*
 * Copyright 2019 Google Inc.
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

package com.example.cloud.iot.endtoend;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CloudiotPubsubExampleServerOptions {
  String projectId;
  String pubsubSubscription;

  static final Options options = new Options();

  public static CloudiotPubsubExampleServerOptions fromFlags(String[] args) {
    // Required arguments
    options.addOption(
        Option.builder()
            .type(String.class)
            .longOpt("pubsub_subscription")
            .hasArg()
            .desc("Google Cloud Pub/Sub subscription name.")
            .required()
            .build());

    // Optional arguments
    options.addOption(
        Option.builder()
            .type(String.class)
            .longOpt("project_id")
            .hasArg()
            .desc("GCP cloud project name.")
            .build());

    CommandLineParser parser = new DefaultParser();
    CommandLine commandLine;

    try {
      commandLine = parser.parse(options, args);
      CloudiotPubsubExampleServerOptions res = new CloudiotPubsubExampleServerOptions();

      if (commandLine.hasOption("project_id")) {
        res.projectId = commandLine.getOptionValue("project_id");
      } else {
        try {
          res.projectId = System.getenv("GOOGLE_CLOUD_PROJECT");
        } catch (NullPointerException npe) {
          res.projectId = System.getenv("GCLOUD_PROJECT");
        }
      }

      if (commandLine.hasOption("pubsub_subscription")) {
        res.pubsubSubscription = commandLine.getOptionValue("pubsub_subscription");
      }

      return res;
    } catch (ParseException e) {
      System.err.println(e.getMessage());
      return null;
    }
  }
}
