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

package com.example.time.testing;

import com.example.time.Clock;
import java.util.concurrent.atomic.AtomicLong;
import org.joda.time.Instant;
import org.joda.time.ReadableDuration;
import org.joda.time.ReadableInstant;

/**
 * A Clock that returns a fixed Instant value as the current clock time.  The
 * fixed Instant is settable for testing.  Test code should hold a reference to
 * the FakeClock, while code under test should hold a Clock reference.
 *
 * <p>The clock time can be incremented/decremented manually, with
 * {@link #incrementTime} and {@link #decrementTime} respectively.
 *
 * <p>The clock can also be configured so that the time is incremented whenever
 * {@link #now()} is called: see {@link #setAutoIncrementStep}.
 */
public class FakeClock implements Clock {
  private static final Instant DEFAULT_TIME = new Instant(1000000000L);
  private final long baseTimeMs;
  private final AtomicLong fakeNowMs;
  private volatile long autoIncrementStepMs;

  /**
   * Creates a FakeClock instance initialized to an arbitrary constant.
   */
  public FakeClock() {
    this(DEFAULT_TIME);
  }

  /**
   * Creates a FakeClock instance initialized to the given time.
   */
  public FakeClock(ReadableInstant now) {
    baseTimeMs = now.getMillis();
    fakeNowMs = new AtomicLong(baseTimeMs);
  }

  /**
   * Sets the value of the underlying instance for testing purposes.
   *
   * @return this
   */
  public FakeClock setNow(ReadableInstant now) {
    fakeNowMs.set(now.getMillis());
    return this;
  }

  @Override
  public Instant now() {
    return getAndAdd(autoIncrementStepMs);
  }

  /**
   * Returns the current time without applying an auto increment, if configured.
   * The default behavior of {@link #now()} is the same as this method.
   */
  public Instant peek() {
    return new Instant(fakeNowMs.get());
  }

  /**
   * Reset the given clock back to the base time with which the FakeClock was
   * initially constructed.
   *
   * @return this
   */
  public FakeClock resetTime() {
    fakeNowMs.set(baseTimeMs);
    return this;
  }

  /**
   * Increments the clock time by the given duration.
   *
   * @param duration the duration to increment the clock time by
   * @return this
   */
  public FakeClock incrementTime(ReadableDuration duration) {
    incrementTime(duration.getMillis());
    return this;
  }

  /**
   * Increments the clock time by the given duration.
   *
   * @param durationMs the duration to increment the clock time by,
   *     in milliseconds
   * @return this
   */
  public FakeClock incrementTime(long durationMs) {
    fakeNowMs.addAndGet(durationMs);
    return this;
  }

  /**
   * Decrements the clock time by the given duration.
   *
   * @param duration the duration to decrement the clock time by
   * @return this
   */
  public FakeClock decrementTime(ReadableDuration duration) {
    incrementTime(-duration.getMillis());
    return this;
  }

  /**
   * Decrements the clock time by the given duration.
   *
   * @param durationMs the duration to decrement the clock time by,
   *     in milliseconds
   * @return this
   */
  public FakeClock decrementTime(long durationMs) {
    incrementTime(-durationMs);
    return this;
  }

  /**
   * Sets the increment applied to the clock whenever it is queried.
   * The increment is zero by default: the clock is left unchanged when queried.
   *
   * @param autoIncrementStep the new auto increment duration
   * @return this
   */
  public FakeClock setAutoIncrementStep(ReadableDuration autoIncrementStep) {
    setAutoIncrementStep(autoIncrementStep.getMillis());
    return this;
  }

  /**
   * Sets the increment applied to the clock whenever it is queried.
   * The increment is zero by default: the clock is left unchanged when queried.
   *
   * @param autoIncrementStepMs the new auto increment duration, in milliseconds
   * @return this
   */
  public FakeClock setAutoIncrementStep(long autoIncrementStepMs) {
    this.autoIncrementStepMs = autoIncrementStepMs;
    return this;
  }

  /**
   * Atomically adds the given value to the current time.
   *
   * @see AtomicLong#addAndGet
   *
   * @param durationMs the duration to add, in milliseconds
   * @return the updated current time
   */
  protected final Instant addAndGet(long durationMs) {
    return new Instant(fakeNowMs.addAndGet(durationMs));
  }

  /**
   * Atomically adds the given value to the current time.
   *
   * @see AtomicLong#getAndAdd
   *
   * @param durationMs the duration to add, in milliseconds
   * @return the previous time
   */
  protected final Instant getAndAdd(long durationMs) {
    return new Instant(fakeNowMs.getAndAdd(durationMs));
  }
}
