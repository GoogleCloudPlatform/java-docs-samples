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

import java.io.IOException;
import java.io.InputStream;

public class StorageUtils {

  /**
   * Reads the contents of an InputStream and does nothing with it.
   */
  public static void readStream(InputStream is) throws IOException {
    byte[] inputBuffer = new byte[256];
    while (is.read(inputBuffer) != -1) {}
    // The caller is responsible for closing this InputStream.
    is.close();
  }

  /**
   * A helper class to provide input streams of any size.
   * The input streams will be full of null bytes.
   */
  static class ArbitrarilyLargeInputStream extends InputStream {

    private long bytesRead;
    private final long streamSize;

    public ArbitrarilyLargeInputStream(long streamSizeInBytes) {
      bytesRead = 0;
      this.streamSize = streamSizeInBytes;
    }

    @Override
    public int read() throws IOException {
      if (bytesRead >= streamSize) {
        return -1;
      }
      bytesRead++;
      return 0;
    }
  }
}

