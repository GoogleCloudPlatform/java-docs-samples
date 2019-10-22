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

package com.example.time;

import org.joda.time.Instant;

/**
 * Provides the current value of "now." To preserve testability, avoid all other libraries that
 * access the system clock (whether {@linkplain System#currentTimeMillis directly} or {@linkplain
 * org.joda.time.DateTime#DateTime() indirectly}).
 *
 * <p>In production, use the {@link SystemClock} implementation to return the "real" system time. In
 * tests, either use {@link com.example.time.testing.FakeClock}, or get an instance from a mocking
 * framework such as Mockito.
 */
public interface Clock {

  /**
   * Returns the current, absolute time according to this clock.
   */
  Instant now();
}
