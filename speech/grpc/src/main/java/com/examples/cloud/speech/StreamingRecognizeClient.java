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

import static org.apache.log4j.ConsoleAppender.SYSTEM_OUT;

import com.google.cloud.speech.v1beta1.RecognitionConfig;
import com.google.cloud.speech.v1beta1.RecognitionConfig.AudioEncoding;
import com.google.cloud.speech.v1beta1.SpeechGrpc;
import com.google.cloud.speech.v1beta1.StreamingRecognitionConfig;
import com.google.cloud.speech.v1beta1.StreamingRecognizeRequest;
import com.google.cloud.speech.v1beta1.StreamingRecognizeResponse;
import com.google.protobuf.ByteString;
import com.google.protobuf.TextFormat;

import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


/**
 * Client that sends streaming audio to Speech.Recognize and returns streaming transcript.
 */
public class StreamingRecognizeClient {

  private final String file;
  private final int samplingRate;

  private static final Logger logger = Logger.getLogger(StreamingRecognizeClient.class.getName());

  private final ManagedChannel channel;

  private final SpeechGrpc.SpeechStub speechClient;

  private static final int BYTES_PER_BUFFER = 3200; //buffer size in bytes
  private static final int BYTES_PER_SAMPLE = 2; //bytes per sample for LINEAR16

  private static final List<String> OAUTH2_SCOPES =
      Arrays.asList("https://www.googleapis.com/auth/cloud-platform");

  /**
   * Construct client connecting to Cloud Speech server at {@code host:port}.
   */
  public StreamingRecognizeClient(ManagedChannel channel, String file, int samplingRate)
      throws IOException {
    this.file = file;
    this.samplingRate = samplingRate;
    this.channel = channel;

    speechClient = SpeechGrpc.newStub(channel);

    //Send log4j logs to Console
    //If you are going to run this on GCE, you might wish to integrate with gcloud-java logging.
    //See https://github.com/GoogleCloudPlatform/gcloud-java/blob/master/README.md#stackdriver-logging-alpha
    
    ConsoleAppender appender = new ConsoleAppender(new SimpleLayout(), SYSTEM_OUT);
    logger.addAppender(appender);
  }

  public void shutdown() throws InterruptedException {
    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
  }

  /** Send streaming recognize requests to server. */
  public void recognize() throws InterruptedException, IOException {
    final CountDownLatch finishLatch = new CountDownLatch(1);
    StreamObserver<StreamingRecognizeResponse> responseObserver =
        new StreamObserver<StreamingRecognizeResponse>() {
          @Override
          public void onNext(StreamingRecognizeResponse response) {
            logger.info("Received response: " + TextFormat.printToString(response));
          }

          @Override
          public void onError(Throwable error) {
            logger.log(Level.WARN, "recognize failed: {0}", error);
            finishLatch.countDown();
          }

          @Override
          public void onCompleted() {
            logger.info("recognize completed.");
            finishLatch.countDown();
          }
        };

    StreamObserver<StreamingRecognizeRequest> requestObserver =
        speechClient.streamingRecognize(responseObserver);
    try {
      // Build and send a StreamingRecognizeRequest containing the parameters for
      // processing the audio.
      RecognitionConfig config =
          RecognitionConfig.newBuilder()
              .setEncoding(AudioEncoding.LINEAR16)
              .setSampleRate(samplingRate)
              .build();
      StreamingRecognitionConfig streamingConfig =
          StreamingRecognitionConfig.newBuilder()
              .setConfig(config)
              .setInterimResults(true)
              .setSingleUtterance(true)
              .build();

      StreamingRecognizeRequest initial =
          StreamingRecognizeRequest.newBuilder().setStreamingConfig(streamingConfig).build();
      requestObserver.onNext(initial);

      // Open audio file. Read and send sequential buffers of audio as additional RecognizeRequests.
      FileInputStream in = new FileInputStream(new File(file));
      // For LINEAR16 at 16000 Hz sample rate, 3200 bytes corresponds to 100 milliseconds of audio.
      byte[] buffer = new byte[BYTES_PER_BUFFER];
      int bytesRead;
      int totalBytes = 0;
      int samplesPerBuffer = BYTES_PER_BUFFER / BYTES_PER_SAMPLE;
      int samplesPerMillis = samplingRate / 1000;

      while ((bytesRead = in.read(buffer)) != -1) {
        totalBytes += bytesRead;
        StreamingRecognizeRequest request =
            StreamingRecognizeRequest.newBuilder()
                .setAudioContent(ByteString.copyFrom(buffer, 0, bytesRead))
                .build();
        requestObserver.onNext(request);
        // To simulate real-time audio, sleep after sending each audio buffer.
        Thread.sleep(samplesPerBuffer / samplesPerMillis);
      }
      logger.info("Sent " + totalBytes + " bytes from audio file: " + file);
    } catch (RuntimeException e) {
      // Cancel RPC.
      requestObserver.onError(e);
      throw e;
    }
    // Mark the end of requests.
    requestObserver.onCompleted();

    // Receiving happens asynchronously.
    finishLatch.await(1, TimeUnit.MINUTES);
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
            .longOpt("file")
            .desc("path to audio file")
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
      if (line.hasOption("file")) {
        audioFile = line.getOptionValue("file");
      } else {
        System.err.println("An Audio file must be specified (e.g. /foo/baz.raw).");
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
    StreamingRecognizeClient client = new StreamingRecognizeClient(channel, audioFile, sampling);
    try {
      client.recognize();
    } finally {
      client.shutdown();
    }
  }
}
