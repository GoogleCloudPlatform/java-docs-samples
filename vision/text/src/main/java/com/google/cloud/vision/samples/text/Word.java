/*
 * Copyright 2016 Google Inc.
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

package com.google.cloud.vision.samples.text;

import java.nio.file.Path;

/**
 * A data object for mapping words to file paths.
 */
public class Word {
  private Path pth;
  private String wrd;

  public static Builder builder() {
    return new Builder();
  }

  public Path path() {
    return this.pth;
  }

  public String word() {
    return this.wrd;
  }

  @Override
  public boolean equals(Object other) {
    if (other == null) {
      return false;
    }
    if (!(other instanceof Word)) {
      return false;
    }
    Word otherWord = (Word) other;
    return this.path().equals(otherWord.path()) && this.word().equals(otherWord.word());
  }

  @Override
  public int hashCode() {
    return this.word().hashCode() ^ this.path().hashCode();
  }

  public static class Builder {
    private Path pth;
    private String wrd;

    Builder() {}

    public Builder path(Path path) {
      this.pth = path;
      return this;
    }

    public Builder word(String word) {
      this.wrd = word;
      return this;
    }

    public Word build() {
      Word out = new Word();
      out.pth = this.pth;
      out.wrd = this.wrd;
      return out;
    }
  }
}
