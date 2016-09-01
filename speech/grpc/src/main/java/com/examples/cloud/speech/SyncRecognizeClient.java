/*
 * Copyright 2016 Google Inc. All Rights Reserved.
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

package com.examples.cloud.speech;

import com.google.cloud.speech.v1beta1.RecognitionAudio;
import com.google.cloud.speech.v1beta1.RecognitionConfig;
import com.google.cloud.speech.v1beta1.RecognitionConfig.AudioEncoding;
import com.google.cloud.speech.v1beta1.SpeechGrpc;
import com.google.cloud.speech.v1beta1.SyncRecognizeRequest;
import com.google.cloud.speech.v1beta1.SyncRecognizeResponse;
import com.google.protobuf.TextFormat;

import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client that sends audio to Speech.SyncRecognize and returns transcript.
 */
public class SyncRecognizeClient {

  private static final Logger logger = Logger.getLogger(SyncRecognizeClient.class.getName());

  private static final List<String> OAUTH2_SCOPES =
      Arrays.asList("https://www.googleapis.com/auth/cloud-platform");

  private final URI input;
  private final int samplingRate;

  private final ManagedChannel channel;
  private final SpeechGrpc.SpeechBlockingStub speechClient;

  /**
   * Construct client connecting to Cloud Speech server at {@code host:port}.
   */
  public SyncRecognizeClient(ManagedChannel channel, URI input, int samplingRate)
      throws IOException {
    this.input = input;
    this.samplingRate = samplingRate;
    this.channel = channel;

    speechClient = SpeechGrpc.newBlockingStub(channel);
  }

  private RecognitionAudio createRecognitionAudio() throws IOException {
    return RecognitionAudioFactory.createRecognitionAudio(this.input);
  }

  public void shutdown() throws InterruptedException {
    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
  }

  /** Send a non-streaming-recognize request to server. */
  public void recognize() {
    RecognitionAudio audio;
    try {
      audio = createRecognitionAudio();
    } catch (IOException e) {
      logger.log(Level.WARNING, "Failed to read audio uri input: " + input);
      return;
    }
    logger.info("Sending " + audio.getContent().size() + " bytes from audio uri input: " + input);
    RecognitionConfig config =
        RecognitionConfig.newBuilder()
            .setEncoding(AudioEncoding.LINEAR16)
            .setSampleRate(samplingRate)
            .build();
    SyncRecognizeRequest request =
        SyncRecognizeRequest.newBuilder().setConfig(config).setAudio(audio).build();

    SyncRecognizeResponse response;
    try {
      response = speechClient.syncRecognize(request);
    } catch (StatusRuntimeException e) {
      logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
      return;
    }
    logger.info("Received response: " + TextFormat.printToString(response));
  }

  public static void main(String[] args) throws Exception {

    String audioFile = "";
    String host = "speech.googleapis.com";
    Integer port = 443;
    Integer sampling = 16000;

    CommandLineParser parser = new DefaultParser();

    Options options = new Options();
    options.addOption(
        Option.builder()
            .longOpt("uri")
            .desc("path to audio uri")
            .hasArg()
            .argName("FILE_PATH")
            .build());
    options.addOption(
        Option.builder()
            .longOpt("host")
            .desc("endpoint for api, e.g. speech.googleapis.com")
            .hasArg()
            .argName("ENDPOINT")
            .build());
    options.addOption(
        Option.builder()
            .longOpt("port")
            .desc("SSL port, usually 443")
            .hasArg()
            .argName("PORT")
            .build());
    options.addOption(
        Option.builder()
            .longOpt("sampling")
            .desc("Sampling Rate, i.e. 16000")
            .hasArg()
            .argName("RATE")
            .build());

    try {
      CommandLine line = parser.parse(options, args);
      if (line.hasOption("uri")) {
        audioFile = line.getOptionValue("uri");
      } else {
        System.err.println("An Audio uri must be specified (e.g. file:///foo/baz.raw).");
        System.exit(1);
      }

      if (line.hasOption("host")) {
        host = line.getOptionValue("host");
      } else {
        System.err.println("An API enpoint must be specified (typically speech.googleapis.com).");
        System.exit(1);
      }

      if (line.hasOption("port")) {
        port = Integer.parseInt(line.getOptionValue("port"));
      } else {
        System.err.println("An SSL port must be specified (typically 443).");
        System.exit(1);
      }

      if (line.hasOption("sampling")) {
        sampling = Integer.parseInt(line.getOptionValue("sampling"));
      } else {
        System.err.println("An Audio sampling rate must be specified.");
        System.exit(1);
      }
    } catch (ParseException exp) {
      System.err.println("Unexpected exception:" + exp.getMessage());
      System.exit(1);
    }

    ManagedChannel channel = AsyncRecognizeClient.createChannel(host, port);
    SyncRecognizeClient client = new SyncRecognizeClient(channel, URI.create(audioFile), sampling);
    try {
      client.recognize();
    } finally {
      client.shutdown();
    }
  }
}
