/*
 * Copyright 2018 Google Inc.
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

//Imports the Google Cloud client library

import com.google.api.gax.rpc.ApiStreamObserver;
import com.google.api.gax.rpc.BidiStreamingCallable;
import com.google.cloud.speech.v1p1beta1.RecognitionConfig;
import com.google.cloud.speech.v1p1beta1.RecognitionConfig.AudioEncoding;
import com.google.cloud.speech.v1p1beta1.SpeechClient;
import com.google.cloud.speech.v1p1beta1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1p1beta1.StreamingRecognitionConfig;
import com.google.cloud.speech.v1p1beta1.StreamingRecognitionResult;
import com.google.cloud.speech.v1p1beta1.StreamingRecognizeRequest;
import com.google.cloud.speech.v1p1beta1.StreamingRecognizeResponse;
import com.google.common.util.concurrent.SettableFuture;
import com.google.protobuf.ByteString;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.List;

/**
 * Google Cloud SpeechToText API sample application. Example usage: mvn package exec:java
 * -Dexec.mainClass='com.example.speech.MicStreamRecognize' -Dexec.args="micstreamrecognize <duration>"
 */
public class MicStreamRecognize {

  // [START speech_mic_streaming]
  /**
   * Demonstrates using the Speech to Text client to convert Microphone streaming speech to text.
   *
   * @throws Exception on SpeechToTextClient Errors.
   */

  private static final int BYTES_PER_BUFFER = 6400; //buffer size in bytes


  public static void main(String... args) throws Exception {
    if (args.length < 1) {
      System.out.println("Usage:");
      System.out.printf(
          "\tjava %s \"<command>\" \"<duration>\"\n"
              + "Command:\n"
              + "\tmicstreamrecognize\n"
              + "Duration(optional):\n\tIn seconds.(Maximum of 60 seconds)\n",
          MicStreamRecognize.class.getCanonicalName());
      return;
    }
    String command = args[0];
    Integer duration = args.length > 1 ? Integer.parseInt(args[1]) : 10;

    // Use command to invoke transcription
    if (command.equals("micstreamrecognize")) {
      micRecognize(duration);
    }
  }

  /**
   * Performs streaming speech recognition on microphone audio data.
   *
   * @param duration the time duration for the microphone streaming
   */
  public static void micRecognize(Integer duration) throws InterruptedException, IOException {
      //Microphone audio format specification
      AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
      DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
      // checks if system supports the data line
      if (!AudioSystem.isLineSupported(info)) {
          System.out.println("Line not supported");
          System.exit(0);
      }
      byte[] data = new byte[BYTES_PER_BUFFER];

      // Instantiates a client with GOOGLE_APPLICATION_CREDENTIALS
      try (SpeechClient speech = SpeechClient.create()) {

          //Configure request with microphone audio
          RecognitionConfig recConfig = RecognitionConfig.newBuilder()
              .setEncoding(AudioEncoding.LINEAR16)
              .setLanguageCode("en-US")
              .setSampleRateHertz(16000)
              .setModel("default")
              .build();
          StreamingRecognitionConfig config = StreamingRecognitionConfig.newBuilder()
              .setConfig(recConfig)
                  .setInterimResults(true)
              .build();

          class ResponseApiStreamingObserver<T> implements ApiStreamObserver<T> {
              private final SettableFuture<List<T>> future = SettableFuture.create();
              private final List<T> messages = new java.util.ArrayList<T>();

              @Override
              public void onNext(T message) { messages.add(message); }

              @Override
              public void onError(Throwable t) {
                  future.setException(t);
              }

              @Override
              public void onCompleted() { future.set(messages); }

              // Returns the SettableFuture object to get received messages / exceptions.
              public SettableFuture<List<T>> future() {
                  return future;
              }
          }

          ResponseApiStreamingObserver<StreamingRecognizeResponse> responseObserver =
              new ResponseApiStreamingObserver<>();

          BidiStreamingCallable<StreamingRecognizeRequest, StreamingRecognizeResponse> callable =
              speech.streamingRecognizeCallable();

          ApiStreamObserver<StreamingRecognizeRequest> requestObserver =
              callable.bidiStreamingCall(responseObserver);

          TargetDataLine targetLine = (TargetDataLine) AudioSystem.getLine(info);
          targetLine.open(format);

          long startTime = System.currentTimeMillis();
          ByteArrayOutputStream out  = new ByteArrayOutputStream();

          // The first request must **only** contain the audio configuration:
          requestObserver.onNext(StreamingRecognizeRequest.newBuilder()
              .setStreamingConfig(config)
              .build());
          System.out.println("Start speaking...");

          while (true) {
              targetLine.start();
              int numBytesRead = targetLine.read(data, 0, data.length);

              if (numBytesRead <= 0) continue;

              out.write(data,0,data.length);
              // Subsequent requests must **only** contain the audio data.
              requestObserver.onNext(StreamingRecognizeRequest.newBuilder()
                  .setAudioContent(ByteString.copyFrom(out.toByteArray()))
                  .build());
              Thread.sleep(1000);
              long estimatedTime = System.currentTimeMillis() - startTime;
              out.flush();
              if (estimatedTime > (duration*1000)) {
                  targetLine.stop();
                  targetLine.close();
                  out.close();
                  System.out.println("Stop speaking");
                  break;
              }
          }
          // Mark transmission as completed after sending the data.
          requestObserver.onCompleted();
          List<StreamingRecognizeResponse> responses = responseObserver.future().get();
          for (StreamingRecognizeResponse response : responses) {
              // For streaming recognize, the results list has one is_final result (if available) followed
              // by a number of in-progress results (if interim_results is true) for subsequent utterances.
              // Just print the first result here.
              StreamingRecognitionResult result = response.getResultsList().get(0);
              // There can be several alternative transcripts for a given chunk of speech. Just use the
              // first (most likely) one here.
              SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
              System.out.printf("Transcript : %s\n", alternative.getTranscript());
          }

      } catch (Exception e) {
          System.err.println(e);
      }
  }

}
