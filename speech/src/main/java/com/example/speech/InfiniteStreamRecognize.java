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

package com.example.speech;

// [START speech_transcribe_infinite_streaming]

import com.google.api.gax.rpc.ClientStream;
import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.StreamController;
import com.google.cloud.speech.v1p1beta1.RecognitionConfig;
import com.google.cloud.speech.v1p1beta1.SpeechClient;
import com.google.cloud.speech.v1p1beta1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1p1beta1.StreamingRecognitionConfig;
import com.google.cloud.speech.v1p1beta1.StreamingRecognitionResult;
import com.google.cloud.speech.v1p1beta1.StreamingRecognizeRequest;
import com.google.cloud.speech.v1p1beta1.StreamingRecognizeResponse;
import com.google.protobuf.ByteString;
import com.google.protobuf.Duration;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class InfiniteStreamRecognize {

  private static final int STREAMING_BATCH_LIMIT = 290000; // ~5 minutes
  private static final String EXIT_WORD = "exit";
  private static final int BYTES_PER_BUFFER = 6000; // buffer size in bytes
  private static final BlockingQueue<byte[]> sharedQueue = new LinkedBlockingQueue<byte[]>();

  // Creating shared object
  private static int restartCounter = 0;
  private static ArrayList<ByteString> audioInput = new ArrayList<ByteString>();
  private static ArrayList<ByteString> lastAudioInput = new ArrayList<ByteString>();
  private static int resultEndTimeInMS = 0;
  private static int isFinalEndTime = 0;
  private static int finalRequestEndTime = 0;
  private static boolean newStream = true;
  private static double bridgingOffset = 0;
  private static boolean lastTranscriptWasFinal = false;
  private static boolean stopRecognition = false;
  private static StreamController referenceToStreamController;

  public static void main(String... args) throws LineUnavailableException {
    InfiniteStreamRecognizeOptions options = InfiniteStreamRecognizeOptions.fromFlags(args);
    if (options == null) {
      // Could not parse.
      System.out.println("Failed to parse options.");
      System.exit(1);
    }

    // TODO(developer): Replace the variables before running the sample or use default
    // the number of samples per second
    int sampleRate = 16000;
    // the number of bits in each sample
    int sampleSizeInBits = 16;
    // the number of channels (1 for mono, 2 for stereo, and so on)
    int channels = 1;
    // indicates whether the data is signed or unsigned
    boolean signed = true;
    // indicates whether the data for a single sample is stored in big-endian byte
    // order (false means little-endian)
    boolean bigEndian = false;

    MicBuffer micBuffer = new MicBuffer(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    try {
      // Say `exit` to stop application execution
      infiniteStreamingRecognize(options.langCode, micBuffer, sampleRate,
              RecognitionConfig.AudioEncoding.LINEAR16);
      System.out.println("\nThe application has been stopped.");
    } catch (Exception e) {
      System.out.println("Exception caught: " + e);
    }
  }

  public static String convertMillisToDate(double milliSeconds) {
    long millis = (long) milliSeconds;
    DecimalFormat format = new DecimalFormat();
    format.setMinimumIntegerDigits(2);
    return String.format(
        "%s:%s /",
        format.format(TimeUnit.MILLISECONDS.toMinutes(millis)),
        format.format(
            TimeUnit.MILLISECONDS.toSeconds(millis)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))));
  }

  /** Performs infinite streaming speech recognition */
  public static void infiniteStreamingRecognize(String languageCode, Runnable micBuffer,
                                                int sampleRateHertz,
                                                RecognitionConfig.AudioEncoding encoding)
          throws Exception {
    // Creating microphone input buffer thread
    Thread micThread = new Thread(micBuffer);
    try (SpeechClient client = SpeechClient.create()) {
      ResponseObserver<StreamingRecognizeResponse> responseObserver = getResponseObserver();
      ClientStream<StreamingRecognizeRequest> clientStream = client.streamingRecognizeCallable()
              .splitCall(responseObserver);

      RecognitionConfig recognitionConfig =
          RecognitionConfig.newBuilder()
              .setEncoding(encoding)
              .setLanguageCode(languageCode)
              .setSampleRateHertz(sampleRateHertz)
              .build();

      StreamingRecognitionConfig streamingRecognitionConfig =
          StreamingRecognitionConfig.newBuilder()
              .setConfig(recognitionConfig)
              .setInterimResults(true)
              .build();

      StreamingRecognizeRequest request =
          StreamingRecognizeRequest.newBuilder()
              .setStreamingConfig(streamingRecognitionConfig)
              .build(); // The first request in a streaming call has to be a config

      clientStream.send(request);

      try {
        micThread.setDaemon(true);
        micThread.start();

        long startTime = System.currentTimeMillis();
        while (!stopRecognition) {
          long estimatedTime = System.currentTimeMillis() - startTime;

          if (estimatedTime >= STREAMING_BATCH_LIMIT) {

            clientStream.closeSend();
            referenceToStreamController.cancel(); // remove Observer

            if (resultEndTimeInMS > 0) {
              finalRequestEndTime = isFinalEndTime;
            }
            resultEndTimeInMS = 0;

            lastAudioInput = null;
            lastAudioInput = audioInput;
            audioInput = new ArrayList<ByteString>();

            restartCounter++;

            if (!lastTranscriptWasFinal) {
              System.out.print('\n');
            }

            newStream = true;

            clientStream = client.streamingRecognizeCallable().splitCall(responseObserver);

            request =
                StreamingRecognizeRequest.newBuilder()
                    .setStreamingConfig(streamingRecognitionConfig)
                    .build();

            System.out.printf("%d: RESTARTING REQUEST\n", restartCounter * STREAMING_BATCH_LIMIT);

            startTime = System.currentTimeMillis();

          } else {

            if ((newStream) && (lastAudioInput.size() > 0)) {
              // if this is the first audio from a new request
              // calculate amount of unfinalized audio from last request
              // resend the audio to the speech client before incoming audio
              double chunkTime = STREAMING_BATCH_LIMIT / lastAudioInput.size();
              // ms length of each chunk in previous request audio arrayList
              if (chunkTime != 0) {
                if (bridgingOffset < 0) {
                  // bridging Offset accounts for time of resent audio
                  // calculated from last request
                  bridgingOffset = 0;
                }
                if (bridgingOffset > finalRequestEndTime) {
                  bridgingOffset = finalRequestEndTime;
                }
                int chunksFromMs =
                    (int) Math.floor((finalRequestEndTime - bridgingOffset) / chunkTime);
                // chunks from MS is number of chunks to resend
                bridgingOffset =
                    (int) Math.floor((lastAudioInput.size() - chunksFromMs) * chunkTime);
                // set bridging offset for next request
                for (int i = chunksFromMs; i < lastAudioInput.size(); i++) {
                  request =
                      StreamingRecognizeRequest.newBuilder()
                          .setAudioContent(lastAudioInput.get(i))
                          .build();
                  clientStream.send(request);
                }
              }
              newStream = false;
            }

            ByteString tempByteString = ByteString.copyFrom(sharedQueue.take());

            checkStopRecognitionFlag(tempByteString.toByteArray());

            request =
                StreamingRecognizeRequest.newBuilder().setAudioContent(tempByteString).build();

            audioInput.add(tempByteString);
          }

          clientStream.send(request);
        }
        clientStream.closeSend();
      } catch (Exception e) {
        System.out.println(e);
      }
    }
  }

  public static ResponseObserver<StreamingRecognizeResponse> getResponseObserver() {
    return new ResponseObserver<StreamingRecognizeResponse>() {

      final ArrayList<StreamingRecognizeResponse> responses = new ArrayList<>();

      public void onStart(StreamController controller) {
        referenceToStreamController = controller;
      }

      public void onResponse(StreamingRecognizeResponse response) {
        responses.add(response);
        StreamingRecognitionResult result = response.getResultsList().get(0);
        Duration resultEndTime = result.getResultEndTime();
        resultEndTimeInMS =
                (int) ((resultEndTime.getSeconds() * 1000) + (resultEndTime.getNanos() / 1000000));
        double correctedTime =
                resultEndTimeInMS - bridgingOffset + (STREAMING_BATCH_LIMIT * restartCounter);

        SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
        if (result.getIsFinal()) {
          System.out.print("\r");
          System.out.printf(
                  "%s: %s [confidence: %.2f]\n",
                  convertMillisToDate(correctedTime),
                  alternative.getTranscript(),
                  alternative.getConfidence());
          isFinalEndTime = resultEndTimeInMS;
          lastTranscriptWasFinal = true;
        } else {
          System.out.print("\r");
          System.out.printf(
                  "%s: %s", convertMillisToDate(correctedTime), alternative.getTranscript());
          lastTranscriptWasFinal = false;
        }
        checkStopRecognitionFlag(alternative.getTranscript().getBytes(StandardCharsets.UTF_8));
      }

      public void onComplete() {
        System.out.println("Recognition was stopped");
      }

      public void onError(Throwable t) {}
    };
  }

  private static void checkStopRecognitionFlag(byte[] flag) {
    if (flag.length <= (EXIT_WORD.length() + 2)) {
      stopRecognition = new String(flag).trim().equalsIgnoreCase(EXIT_WORD);
      if (stopRecognition) {
        putDataToSharedQueue(EXIT_WORD.getBytes(StandardCharsets.UTF_8));
      }
    }
  }

  // Microphone Input buffering
  static class MicBuffer implements Runnable {
    TargetDataLine targetDataLine;

    public MicBuffer(int sampleRate, int sampleSizeInBits,
                     int channels, boolean signed, boolean bigEndian)
            throws LineUnavailableException {
      AudioFormat audioFormat
              = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
      DataLine.Info targetInfo =
              new Info(
                      TargetDataLine.class,
                      audioFormat); // Set the system information to read from the microphone audio
      // stream

      if (!AudioSystem.isLineSupported(targetInfo)) {
        System.out.println("Microphone not supported");
        System.exit(0);
      }
      // Target data line captures the audio stream the microphone produces.
      targetDataLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
      targetDataLine.open(audioFormat);
    }

    @Override
    public void run() {
      System.out.println("Start speaking...Say `exit` to stop");
      targetDataLine.start();
      byte[] data = new byte[BYTES_PER_BUFFER];
      while (targetDataLine.isOpen()) {
        int numBytesRead = targetDataLine.read(data, 0, data.length);
        if ((numBytesRead <= 0) && (targetDataLine.isOpen())) {
          continue;
        }
        putDataToSharedQueue(data.clone());
      }
    }
  }

  public static void putDataToSharedQueue(byte[] data) {
    try {
      sharedQueue.put(data.clone());
    } catch (InterruptedException e) {
      System.out.printf("Can't insert data to shared queue. Caused by : %s", e.getMessage());
      throw new RuntimeException(e);
    }
  }
}
// [END speech_transcribe_infinite_streaming]
