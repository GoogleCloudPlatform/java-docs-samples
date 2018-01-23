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

import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Status;
import java.nio.file.Path;
import java.util.List;
import javax.annotation.Nullable;

/**
 * A data object for mapping text to file paths.
 */
public class ImageText {
  private Path pth;
  private List<EntityAnnotation> ts;
  private Status err;

  public static Builder builder() {
    return new Builder();
  }

  private ImageText() {}

  public Path path() {
    return this.pth;
  }

  public List<EntityAnnotation> textAnnotations() {
    return this.ts;
  }

  @Nullable
  public Status error() {
    return this.err;
  }

  public static class Builder {
    private Path pth;
    private List<EntityAnnotation> ts;
    private Status err;

    Builder() {}

    public Builder path(Path path) {
      this.pth = path;
      return this;
    }

    public Builder textAnnotations(List<EntityAnnotation> ts) {
      this.ts = ts;
      return this;
    }

    public Builder error(@Nullable Status err) {
      this.err = err;
      return this;
    }

    public ImageText build() {
      ImageText out = new ImageText();
      out.pth = this.pth;
      out.ts = this.ts;
      out.err = this.err;
      return out;
    }
  }
}
