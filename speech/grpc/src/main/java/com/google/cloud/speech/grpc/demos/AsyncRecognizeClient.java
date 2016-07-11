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

// Client that sends audio to Speech.AsyncRecognize via gRPC and returns longrunning operation.
// The results are received via the google.longrunning.Operations interface.
//
// Uses a service account for OAuth2 authentication, which you may obtain at
// https://console.developers.google.com
// API Manager > Google Cloud Speech API > Enable
// API Manager > Credentials > Create credentials > Service account key > New service account.
//
// Then set environment variable GOOGLE_APPLICATION_CREDENTIALS to the full path of that file.

package com.google.cloud.speech.grpc.demos;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1beta1.AsyncRecognizeRequest;
import com.google.cloud.speech.v1beta1.AsyncRecognizeResponse;
import com.google.cloud.speech.v1beta1.RecognitionAudio;
import com.google.cloud.speech.v1beta1.RecognitionConfig;
import com.google.cloud.speech.v1beta1.RecognitionConfig.AudioEncoding;
import com.google.cloud.speech.v1beta1.SpeechGrpc;

import com.google.longrunning.GetOperationRequest;
import com.google.longrunning.Operation;
import com.google.longrunning.OperationsGrpc;

import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.auth.ClientAuthInterceptor;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client that sends audio to Speech.AsyncRecognize and returns transcript.
 */
public class AsyncRecognizeClient {

  private static final Logger logger =
      Logger.getLogger(AsyncRecognizeClient.class.getName());

  private static final List<String> OAUTH2_SCOPES =
      Arrays.asList("https://www.googleapis.com/auth/cloud-platform");

  private final String host;
  private final int port;
  private final URI input;
  private final int samplingRate;

  private final ManagedChannel channel;
  private final SpeechGrpc.SpeechBlockingStub stub;
  private final OperationsGrpc.OperationsBlockingStub statusStub;

  /**
   * Construct client connecting to Cloud Speech server at {@code host:port}.
   */
  public AsyncRecognizeClient(String host, int port, URI input, int samplingRate)
      throws IOException {
    this.host = host;
    this.port = port;
    this.input = input;
    this.samplingRate = samplingRate;

    GoogleCredentials creds = GoogleCredentials.getApplicationDefault();
    creds = creds.createScoped(OAUTH2_SCOPES);
    channel = NettyChannelBuilder.forAddress(host, port)
        .negotiationType(NegotiationType.TLS)
        .intercept(new ClientAuthInterceptor(creds, Executors.newSingleThreadExecutor()))
        .build();
    stub = SpeechGrpc.newBlockingStub(channel);
    statusStub = OperationsGrpc.newBlockingStub(channel);

    logger.info("Created stub for " + host + ":" + port);
  }

  public void shutdown() throws InterruptedException {
    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
  }

  /** Send an async-recognize request to server. */
  public void recognize() {
    RecognitionAudio audio;
    try {
      audio = RecognitionAudioFactory.createRecognitionAudio(this.input);
    } catch (IOException e) {
      logger.log(Level.WARNING, "Failed to read audio uri input: " + input);
      return;
    }
    logger.info("Sending " + audio.getContent().size() + " bytes from audio uri input: " + input);
    RecognitionConfig config = RecognitionConfig.newBuilder()
        .setEncoding(AudioEncoding.LINEAR16)
        .setSampleRate(samplingRate)
        .build();
    AsyncRecognizeRequest request = AsyncRecognizeRequest.newBuilder()
        .setConfig(config)
        .setAudio(audio)
        .build();

    Operation operation;
    Operation status;
    try {
      operation = stub.asyncRecognize(request);

      //Print the long running operation handle
      logger.log(Level.INFO, String.format("Operation handle: %s, URI: %s", operation.getName(),
            input.toString()));
    } catch (StatusRuntimeException e) {
      logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
      return;
    }

    while (true) {
      try {
        logger.log(Level.INFO, "Waiting 2s for operation, {0} processing...", operation.getName());
        Thread.sleep(2000);
        GetOperationRequest operationReq = GetOperationRequest.newBuilder()
            .setName(operation.getName())
            .build();
        status = statusStub.getOperation(
            GetOperationRequest.newBuilder()
                .setName(operation.getName())
                .build()
                );

        if (status.getDone()) {
          break;
        }
      } catch (Exception ex) {
        logger.log(Level.WARNING, ex.getMessage());
      }
    }

    try {
      AsyncRecognizeResponse asyncRes = status.getResponse().unpack(AsyncRecognizeResponse.class);

      logger.info("Received response: " + asyncRes);
    } catch (com.google.protobuf.InvalidProtocolBufferException ex) {
      logger.log(Level.WARNING, "Unpack error, {0}",ex.getMessage());
    }
  }

  public static void main(String[] args) throws Exception {

    String audioFile = "";
    String host = "speech.googleapis.com";
    Integer port = 443;
    Integer sampling = 16000;

    CommandLineParser parser = new DefaultParser();

    Options options = new Options();
    options.addOption(OptionBuilder.withLongOpt("uri")
        .withDescription("path to audio uri")
        .hasArg()
        .withArgName("FILE_PATH")
        .create());
    options.addOption(OptionBuilder.withLongOpt("host")
        .withDescription("endpoint for api, e.g. speech.googleapis.com")
        .hasArg()
        .withArgName("ENDPOINT")
        .create());
    options.addOption(OptionBuilder.withLongOpt("port")
        .withDescription("SSL port, usually 443")
        .hasArg()
        .withArgName("PORT")
        .create());
    options.addOption(OptionBuilder.withLongOpt("sampling")
        .withDescription("Sampling Rate, i.e. 16000")
        .hasArg()
        .withArgName("RATE")
        .create());

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

    AsyncRecognizeClient client =
        new AsyncRecognizeClient(host, port, URI.create(audioFile), sampling);
    try {
      client.recognize();
    } finally {
      client.shutdown();
    }
  }
}
