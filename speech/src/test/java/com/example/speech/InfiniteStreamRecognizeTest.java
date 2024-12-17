/*
 * Copyright 2024 Google LLC
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

import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.speech.v1p1beta1.RecognitionConfig;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.junit.Before;
import org.junit.Test;

public class InfiniteStreamRecognizeTest {
  private static final String COMMERCIAL_MONO_FILE = "resources/commercial_mono.wav";
  private ByteArrayOutputStream bout;

  @Before
  public void setUp() throws IOException, ExecutionException, InterruptedException,
          TimeoutException {
    bout = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(bout);
    System.setOut(out);
  }

  @Test
  public void infiniteStreamingRecognizeTest() throws Exception {
    InfiniteStreamRecognize.putDataToSharedQueue(readAudioFile());
    InfiniteStreamRecognize.putDataToSharedQueue("exit".getBytes(StandardCharsets.UTF_8));
    InfiniteStreamRecognize.infiniteStreamingRecognize("en-US",
            () -> {}, 16000, RecognitionConfig.AudioEncoding.LINEAR16);
    // wait responses from server
    Thread.sleep(10000);
    assertThat(bout.toString().toLowerCase()).contains("hi i want to");
    assertThat(bout.toString().toLowerCase()).contains("recognition was stopped");
  }

  private byte[] readAudioFile() throws UnsupportedAudioFileException, IOException {
    File file = new File(COMMERCIAL_MONO_FILE);
    AudioInputStream stream = AudioSystem.getAudioInputStream(file);
    AudioFormat format = stream.getFormat();
    int length = (int) (stream.getFrameLength() * format.getFrameSize());
    byte[] samples = new byte[length];
    DataInputStream in = new DataInputStream(stream);
    in.readFully(samples);

    return samples;
  }
}