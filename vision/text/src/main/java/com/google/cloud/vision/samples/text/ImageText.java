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

package com.google.cloud.vision.samples.text;

import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Status;
import com.google.auto.value.AutoValue;

import java.nio.file.Path;
import java.util.List;

import javax.annotation.Nullable;

/**
 * A data object for mapping text to file paths.
 */
@AutoValue
abstract class ImageText {

  public static Builder builder() {
    return new AutoValue_ImageText.Builder();
  }

  public abstract Path path();

  public abstract List<EntityAnnotation> textAnnotations();

  @Nullable
  public abstract Status error();

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder path(Path path);

    public abstract Builder textAnnotations(List<EntityAnnotation> ts);

    public abstract Builder error(@Nullable Status err);

    public abstract ImageText build();
  }
}
