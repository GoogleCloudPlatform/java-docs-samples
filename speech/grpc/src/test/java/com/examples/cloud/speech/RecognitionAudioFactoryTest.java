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

import static org.junit.Assert.assertEquals;

import com.google.cloud.speech.v1beta1.RecognitionAudio;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.IOException;
import java.net.URI;

/**
 * Unit tests for {@link RecognitionAudioFactory}.
 */
@RunWith(JUnit4.class)
public class RecognitionAudioFactoryTest {

  @Test
  public void verifyBytesInSizeFromLocalFile() throws IOException {
    URI uri = new File("resources/audio.raw").toURI();
    RecognitionAudio audio = RecognitionAudioFactory.createRecognitionAudio(uri);

    int numBytes = audio.getContent().toByteArray().length;

    //assert the number of bytes in the audio as 57958
    assertEquals(57958, numBytes);
  }

  @Test
  public void verifyBytesInSizeFromGoogleStorageFile() throws IOException {
    String audioUri = "gs://cloud-samples-tests/speech/audio.raw";

    URI uri = URI.create(audioUri);
    RecognitionAudio audio = RecognitionAudioFactory.createRecognitionAudio(uri);

    int numBytes = audio.getContent().toByteArray().length;

    //assert the number of bytes in the audio as 0
    assertEquals(0, numBytes);

    //assert the uri
    assertEquals(audioUri, audio.getUri());
  }
}
