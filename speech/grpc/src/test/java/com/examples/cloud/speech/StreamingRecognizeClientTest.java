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

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.when;

import io.grpc.ManagedChannel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import javax.sound.sampled.TargetDataLine;


/**
 * Unit tests for {@link StreamingRecognizeClient }.
 */
@RunWith(JUnit4.class)
public class StreamingRecognizeClientTest {
  private final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
  private static final PrintStream REAL_OUT = System.out;

  @Mock private TargetDataLine mockDataLine;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    System.setOut(new PrintStream(stdout));
  }

  @After
  public void tearDown() {
    System.setOut(REAL_OUT);
  }

  @Test
  public void test16KHzAudio() throws InterruptedException, IOException {
    String host = "speech.googleapis.com";
    int port = 443;
    ManagedChannel channel = StreamingRecognizeClient.createChannel(host, port);

    final FileInputStream in = new FileInputStream("resources/audio.raw");

    final int samplingRate = 16000;
    final StreamingRecognizeClient client = new StreamingRecognizeClient(channel, samplingRate);

    // When audio data is requested from the mock, get it from the file
    when(mockDataLine.read(any(byte[].class), anyInt(), anyInt())).thenAnswer(new Answer() {
      public Object answer(InvocationOnMock invocation) {
        Object[] args = invocation.getArguments();
        byte[] buffer = (byte[])args[0];
        int offset = (int)args[1];
        int len = (int)args[2];
        assertThat(buffer.length).isEqualTo(len);

        try {
          // Sleep, to simulate realtime
          int samplesPerBuffer = client.bytesPerBuffer / StreamingRecognizeClient.BYTES_PER_SAMPLE;
          int samplesPerMillis = samplingRate / 1000;
          Thread.sleep(samplesPerBuffer / samplesPerMillis);

          // Provide the audio bytes from the file
          return in.read(buffer, offset, len);

        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    });
    client.mockDataLine = mockDataLine;

    client.recognize();

    assertThat(stdout.toString()).contains("how old is the Brooklyn Bridge");
  }

  @Test
  public void test32KHzAudio() throws InterruptedException, IOException {
    String host = "speech.googleapis.com";
    int port = 443;
    ManagedChannel channel = StreamingRecognizeClient.createChannel(host, port);

    final FileInputStream in = new FileInputStream("resources/audio32KHz.raw");

    final int samplingRate = 32000;
    final StreamingRecognizeClient client = new StreamingRecognizeClient(channel, samplingRate);

    // When audio data is requested from the mock, get it from the file
    when(mockDataLine.read(any(byte[].class), anyInt(), anyInt())).thenAnswer(new Answer() {
      public Object answer(InvocationOnMock invocation) {
        Object[] args = invocation.getArguments();
        byte[] buffer = (byte[])args[0];
        int offset = (int)args[1];
        int len = (int)args[2];
        assertThat(buffer.length).isEqualTo(len);

        try {
          // Sleep, to simulate realtime
          int samplesPerBuffer = client.bytesPerBuffer / StreamingRecognizeClient.BYTES_PER_SAMPLE;
          int samplesPerMillis = samplingRate / 1000;
          Thread.sleep(samplesPerBuffer / samplesPerMillis);

          // Provide the audio bytes from the file
          return in.read(buffer, offset, len);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    });
    client.mockDataLine = mockDataLine;

    client.recognize();

    assertThat(stdout.toString()).contains("how old is the Brooklyn Bridge");
  }
}
