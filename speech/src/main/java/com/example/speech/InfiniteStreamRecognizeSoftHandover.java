/*
 * Copyright 2023 Google LLC
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

// [START speech_transcribe_infinite_streaming_soft_handover]

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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.TargetDataLine;

/*
 * This class demonstrates how to perform infinite streaming speech recognition using the
 * StreamingRecognize functionality of the Speech API.
 * This class is almost identical to the InfiniteStreamRecognize.java class, except that it
 * demonstrates how to perform "soft handover" between two streams.
 * A "soft handover" is making a new stream before breaking the old stream. As against a "hard
 * handover" where you break the old stream before making the new stream.
 * This is useful in situations where you want to perform speech recognition on a continuous audio
 * input, but need to periodically restart the stream to avoid exceeding the maximum allowed
 * continuous streaming duration of 30 seconds.
 * This class uses two streams, STREAM1 and STREAM2, and alternates between them.
 * When one stream is active, the other stream is used to buffer audio input.
 * When the active stream is stopped, the buffered audio input is used to create a new stream.
 * This class also demonstrates how to align the transcript of the previous stream with the
 * transcript of the current stream. This is useful in situations where you want to perform
 * continuous speech recognition on a continuous audio input, but need to restart the stream
 */

public class InfiniteStreamRecognizeSoftHandOver {

  private static final int STREAMING_LIMIT = 30000; // resetting 30 seconds to test

  enum Stream {
    STREAM1,
    STREAM2;
  }

  public static final String RED = "\033[0;31m";
  public static final String GREEN = "\033[0;32m";
  public static final String YELLOW = "\033[0;33m";

  // Creating shared object
  private static volatile BlockingQueue<byte[]> sharedQueue = new LinkedBlockingQueue<byte[]>();
  private static TargetDataLine targetDataLine;
  private static int BYTES_PER_BUFFER = 6400; // buffer size in bytes

  private static int restartCounter = 0;
  private static ArrayList<ByteString> audioInput = new ArrayList<ByteString>();
  private static int resultEndTimeInMS = 0;
  private static boolean newStream = false;
  private static double bridgingOffset = 0;
  private static volatile Stream activeStream = Stream.STREAM1;
  private static ByteString tempByteString;
  private static Map<String, StreamingRecognitionResult> streamWiseLatestResults =
      new HashMap<String, StreamingRecognitionResult>();

  public static void main(String... args) {
    InfiniteStreamRecognizeOptions options = InfiniteStreamRecognizeOptions.fromFlags(args);
    if (options == null) {
      // Could not parse.
      System.out.println("Failed to parse options.");
      System.exit(1);
    }

    try {
      infiniteStreamingRecognize(options.langCode);
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

  private static boolean computeIntersectionRatio(
      String previousTranscript, String currentTranscript) { // Compare the
    // transcripts
    // for
    // alignment
    int commonWordCount = 0;
    String[] previousWords = previousTranscript.split("\\s+");
    String[] currentWords = currentTranscript.split("\\s+");

    // Compute word intersection ratio between previous and current by reverse traversing the arrays
    for (int i = previousWords.length - 1; i >= 0; i--) {
      for (int j = currentWords.length - 1; j >= 0; j--) {
        if (previousWords[i].equalsIgnoreCase(currentWords[j])) {
          commonWordCount++;
          // This is a simplistic alignment algorithm which works ok.
          // However, real world use cases may require more sophisticated algorithms
          if (commonWordCount >= 3) {
            return true;
          }
        }
      }
    }

    return false;
  }

  /** Performs infinite streaming speech recognition */
  public static void infiniteStreamingRecognize(String languageCode) throws Exception {

    // Microphone Input buffering
    class MicBuffer implements Runnable {

      @Override
      public void run() {
        System.out.println(YELLOW);
        System.out.println("Start speaking...Press Ctrl-C to stop");
        targetDataLine.start();
        byte[] data = new byte[BYTES_PER_BUFFER];
        while (targetDataLine.isOpen()) {
          try {
            int numBytesRead = targetDataLine.read(data, 0, data.length);
            if ((numBytesRead <= 0) && (targetDataLine.isOpen())) {
              continue;
            }
            sharedQueue.put(data.clone());
          } catch (InterruptedException e) {
            System.out.println("Microphone input buffering interrupted : " + e.getMessage());
          }
        }
      }
    }

    // Creating microphone input buffer thread
    MicBuffer micrunnable = new MicBuffer();
    Thread micThread = new Thread(micrunnable);
    // one for each stream, just for clarity. Can be optimized.
    ResponseObserver<StreamingRecognizeResponse> responseObserver1 = null;
    ResponseObserver<StreamingRecognizeResponse> responseObserver2 = null;
    try (SpeechClient client = SpeechClient.create()) {
      // Active stream is used to send data.
      ClientStream<StreamingRecognizeRequest> stream1 = null;
      ClientStream<StreamingRecognizeRequest> stream2 = null;

      responseObserver1 =
          new ResponseObserver<StreamingRecognizeResponse>() {
            ArrayList<StreamingRecognizeResponse> responses = new ArrayList<>();

            public void onStart(StreamController controller) {}

            public void onResponse(StreamingRecognizeResponse response) {
              responses.add(response);
              if (response.getResults(0).getIsFinal()) { // we only compare final and finals has
                // only one results
                streamWiseLatestResults.put("stream1", response.getResults(0));
              }
              StreamingRecognitionResult result = response.getResultsList().get(0);
              Duration resultEndTime = result.getResultEndTime();
              resultEndTimeInMS =
                  (int)
                      ((resultEndTime.getSeconds() * 1000) + (resultEndTime.getNanos() / 1000000));
              double correctedTime =
                  resultEndTimeInMS - bridgingOffset + (STREAMING_LIMIT * restartCounter);

              if (activeStream == Stream.STREAM1) {
                SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                if (result.getIsFinal()) {
                  System.out.print(GREEN);
                  System.out.print("\033[2K\r");
                  System.out.printf(
                      "%s: %s [confidence: %.2f] [stream: %s]\n",
                      convertMillisToDate(correctedTime),
                      alternative.getTranscript(),
                      alternative.getConfidence(),
                      "stream1");
                } else {
                  System.out.print(RED);
                  System.out.print("\033[2K\r");
                  System.out.printf(
                      "%s: %s", convertMillisToDate(correctedTime), alternative.getTranscript());
                }
              }
            }

            public void onComplete() {}

            @Override
            public void onError(Throwable t) {}
          };
      stream1 = client.streamingRecognizeCallable().splitCall(responseObserver1);

      responseObserver2 =
          new ResponseObserver<StreamingRecognizeResponse>() {
            ArrayList<StreamingRecognizeResponse> responses = new ArrayList<>();

            public void onStart(StreamController controller) {}

            public void onResponse(StreamingRecognizeResponse response) {
              responses.add(response);
              if (response.getResults(0).getIsFinal()) { // we only compare final and finals has
                // only one results
                streamWiseLatestResults.put("stream2", response.getResults(0));
              }
              StreamingRecognitionResult result = response.getResultsList().get(0);
              Duration resultEndTime = result.getResultEndTime();
              resultEndTimeInMS =
                  (int)
                      ((resultEndTime.getSeconds() * 1000) + (resultEndTime.getNanos() / 1000000));
              double correctedTime =
                  resultEndTimeInMS - bridgingOffset + (STREAMING_LIMIT * restartCounter);

              if (activeStream == Stream.STREAM2) {
                SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                if (result.getIsFinal()) {
                  System.out.print(GREEN);
                  System.out.print("\033[2K\r");
                  System.out.printf(
                      "%s: %s [confidence: %.2f] [stream: %s]\n",
                      convertMillisToDate(correctedTime),
                      alternative.getTranscript(),
                      alternative.getConfidence(),
                      "stream2");

                } else {
                  System.out.print(RED);
                  System.out.print("\033[2K\r");
                  System.out.printf(
                      "%s: %s", convertMillisToDate(correctedTime), alternative.getTranscript());
                }
              }
            }

            public void onComplete() {}

            public void onError(Throwable t) {}
          };

      RecognitionConfig recognitionConfig =
          RecognitionConfig.newBuilder()
              .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
              .setLanguageCode(languageCode)
              .setSampleRateHertz(16000)
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

      stream1.send(request);

      try {
        // SampleRate:16000Hz, SampleSizeInBits: 16, Number of channels: 1, Signed:
        // true,
        // bigEndian: false
        AudioFormat audioFormat = new AudioFormat(16000, 16, 1, true, false);
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
        micThread.start();

        long startTime = System.currentTimeMillis();
        AtomicBoolean isAligned = new AtomicBoolean(false);

        while (true) {

          long estimatedTime = System.currentTimeMillis() - startTime;

          if (estimatedTime >= STREAMING_LIMIT) {
            // reset started, bring passive stream to life!
            restartCounter++;
            newStream = true;

            request =
                StreamingRecognizeRequest.newBuilder()
                    .setStreamingConfig(streamingRecognitionConfig)
                    .build();

            if (activeStream
                == Stream.STREAM2) { // meaning stream2 is active, so prepare stream 1 for reset
              stream1 = client.streamingRecognizeCallable().splitCall(responseObserver1);
              while (!stream1.isSendReady()) {
                Thread.sleep(5);
              }
              stream1.send(request);
            } else { // meaning stream1 is active, so prepare stream 2 for reset
              stream2 = client.streamingRecognizeCallable().splitCall(responseObserver2);
              while (!stream2.isSendReady()) {
                Thread.sleep(5);
              }
              stream2.send(request);
            }

            System.out.println(YELLOW);
            System.out.printf("%d: SOFT RESTARTING REQUEST\n", restartCounter * STREAMING_LIMIT);

            // reset startTime
            startTime = System.currentTimeMillis();

          } else {
            if (newStream) {
              // we're here, that means the second stream has been opened now

              if (!isAligned.get()) {
                // now we're comparing for aligment
                // Send audio to two streams to check for alignment:
                request =
                    StreamingRecognizeRequest.newBuilder().setAudioContent(tempByteString).build();

                stream1.send(request);
                stream2.send(request);

                if (streamWiseLatestResults.size() > 0) {
                  // we have results to compare!
                  StreamingRecognitionResult result1 = streamWiseLatestResults.get("stream1");
                  StreamingRecognitionResult result2 = streamWiseLatestResults.get("stream2");

                  if (result1 != null && result2 != null) {
                    isAligned.set(
                        computeIntersectionRatio(
                            result1.getAlternatives(0).getTranscript(),
                            result2.getAlternatives(0).getTranscript()));
                  }
                }
              } else {
                newStream = false;
                isAligned.set(false);
                // toggle streams
                if (activeStream == Stream.STREAM1) {
                  activeStream = Stream.STREAM2;
                  stream1.closeSend();
                  stream1 = null;
                } else {
                  activeStream = Stream.STREAM1;
                  stream2.closeSend();
                  stream2 = null;
                }
              }
            }
            tempByteString = ByteString.copyFrom(sharedQueue.take());

            request =
                StreamingRecognizeRequest.newBuilder().setAudioContent(tempByteString).build();

            audioInput.add(tempByteString);

            if (activeStream == Stream.STREAM1) {
              stream1.send(request);
            } else {
              stream2.send(request);
            }
          }
        }
      } catch (Exception e) {
        System.out.println(e);
      }
    }
  }
}
// [END speech_transcribe_infinite_streaming_soft_handover]
