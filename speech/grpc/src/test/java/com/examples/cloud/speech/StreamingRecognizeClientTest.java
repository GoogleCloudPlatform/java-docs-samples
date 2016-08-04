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

import io.grpc.ManagedChannel;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.WriterAppender;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * Unit tests for {@link StreamingRecognizeClient }.
 */
@RunWith(JUnit4.class)
public class StreamingRecognizeClientTest {
  private Writer writer;
  private WriterAppender appender;

  @Before
  public void setUp() {
    writer = new StringWriter();
    appender = new WriterAppender(new SimpleLayout(), writer);
    Logger.getRootLogger().addAppender(appender);
  }

  @After
  public void tearDown() {
    Logger.getRootLogger().removeAppender(appender);
  }

  @Test
  public void test16KHzAudio() throws InterruptedException, IOException {
    URI uri = new File("resources/audio.raw").toURI();
    Path path = Paths.get(uri);

    String host = "speech.googleapis.com";
    int port = 443;
    ManagedChannel channel = AsyncRecognizeClient.createChannel(host, port);
    StreamingRecognizeClient client = new StreamingRecognizeClient(channel, path.toString(), 16000);
    
    client.recognize();
    assertThat(writer.toString()).contains("transcript: \"how old is the Brooklyn Bridge\"");
  }

  @Test
  public void test32KHzAudio() throws InterruptedException, IOException {
    URI uri = new File("resources/audio32KHz.raw").toURI();
    Path path = Paths.get(uri);

    String host = "speech.googleapis.com";
    int port = 443;
    ManagedChannel channel = AsyncRecognizeClient.createChannel(host, port);
    StreamingRecognizeClient client = new StreamingRecognizeClient(channel, path.toString(), 32000);

    client.recognize();
    assertThat(writer.toString()).contains("transcript: \"how old is the Brooklyn Bridge\"");
  }
}
