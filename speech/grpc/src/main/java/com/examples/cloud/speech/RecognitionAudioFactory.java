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
import com.google.protobuf.ByteString;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/*
 * RecognitionAudioFactory takes a URI as an input and creates a RecognitionAudio.
 * The URI can point to a local file or a file on Google Cloud Storage.
 */
public class RecognitionAudioFactory {

  private static final String FILE_SCHEME = "file";
  private static final String GS_SCHEME = "gs";

  /**
   * Takes an input URI of form $scheme:// and converts to audio request.
   *
   * @param uri input uri
   * @return RecognitionAudio recognition audio
   */
  public static RecognitionAudio createRecognitionAudio(URI uri) throws IOException {
    if (uri.getScheme() == null) {
      uri = new File(uri.toString()).toURI();
      Path path = Paths.get(uri);
      return audioFromBytes(Files.readAllBytes(path));
    } else if (uri.getScheme().equals(FILE_SCHEME)) {
      Path path = Paths.get(uri);
      return audioFromBytes(Files.readAllBytes(path));
    } else if (uri.getScheme().equals(GS_SCHEME)) {
      return RecognitionAudio.newBuilder().setUri(uri.toString()).build();
    }
    throw new RuntimeException("scheme not supported " + uri.getScheme());
  }

  /**
   * Convert bytes to RecognitionAudio.
   *
   * @param bytes input bytes
   * @return RecognitionAudio recognition audio
   */
  private static RecognitionAudio audioFromBytes(byte[] bytes) {
    return RecognitionAudio.newBuilder().setContent(ByteString.copyFrom(bytes)).build();
  }
}
