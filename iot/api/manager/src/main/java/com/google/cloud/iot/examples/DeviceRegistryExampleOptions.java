/**
 * Copyright 2017, Google, Inc.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.cloud.iot.examples;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/** Command line options for the Device Manager example */
public class DeviceRegistryExampleOptions {
  String projectId;
  String pubsubTopic;
  String ecPublicKeyFile = "ec_public.pem";
  String rsaCertificateFile = "rsa_cert.pem";
  String cloudRegion = "us-central1";

  /** Construct an DeviceRegistryExampleOptions class from command line flags. */
  public static DeviceRegistryExampleOptions fromFlags(String args[]) {
    Options options = new Options();
    // Required arguments
    options.addOption(
        Option.builder()
            .type(String.class)
            .longOpt("project_id")
            .hasArg()
            .desc("GCP cloud project name.")
            .required()
            .build());
    options.addOption(
        Option.builder()
            .type(String.class)
            .longOpt("pubsub_topic")
            .hasArg()
            .desc(
                "Pub/Sub topic to create registry with, i.e. 'projects/project-id/topics/topic-id'")
            .required()
            .build());

    // Optional arguments.
    options.addOption(
        Option.builder()
            .type(String.class)
            .longOpt("ec_public_key_file")
            .hasArg()
            .desc("Path to ES256 public key file.")
            .build());
    options.addOption(
        Option.builder()
            .type(String.class)
            .longOpt("rsa_certificate_file")
            .hasArg()
            .desc("Path to RS256 certificate file.")
            .build());
    options.addOption(
        Option.builder()
            .type(String.class)
            .longOpt("cloud_region")
            .hasArg()
            .desc("GCP cloud region.")
            .build());

    CommandLineParser parser = new DefaultParser();
    CommandLine commandLine;
    try {
      commandLine = parser.parse(options, args);
      DeviceRegistryExampleOptions res = new DeviceRegistryExampleOptions();

      res.projectId = commandLine.getOptionValue("project_id");
      res.pubsubTopic = commandLine.getOptionValue("pubsub_topic");

      if (commandLine.hasOption("ec_public_key_file")) {
        res.ecPublicKeyFile = commandLine.getOptionValue("ec_public_key_file");
      }
      if (commandLine.hasOption("rsa_certificate_file")) {
        res.rsaCertificateFile = commandLine.getOptionValue("rsa_certificate_file");
      }
      if (commandLine.hasOption("cloud_region")) {
        res.cloudRegion = commandLine.getOptionValue("cloud_region");
      }
      return res;
    } catch (ParseException e) {
      System.err.println(e.getMessage());
      return null;
    }
  }
}
