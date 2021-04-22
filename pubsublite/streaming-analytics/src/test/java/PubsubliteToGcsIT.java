// Copyright 2021 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import static com.google.common.truth.Truth.assertThat;

import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.beam.examples.common.WriteOneFilePerWindow;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.testing.TestStream;
import org.apache.beam.sdk.transforms.windowing.AfterProcessingTime;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.IntervalWindow;
import org.apache.beam.sdk.transforms.windowing.Repeatedly;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TimestampedValue;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.junit.Rule;
import org.junit.Test;

public class PubsubliteToGcsIT {
  @Rule public final TestPipeline p = TestPipeline.create();

  private Instant baseTime = new Instant(0);

  @Test
  public void testPubsubliteToGcs() {
    // Create a test stream.
    TestStream<String> teststream =
        TestStream.create(StringUtf8Coder.of())
            .advanceWatermarkTo(baseTime)
            .addElements(
                TimestampedValue.of("Hello", baseTime.plus(Duration.standardSeconds(10L))),
                TimestampedValue.of("Hello again", baseTime.plus(Duration.standardSeconds(50L))))
            .advanceProcessingTime(Duration.standardSeconds(60))
            .addElements(
                TimestampedValue.of(
                    "Hello last time", baseTime.plus(Duration.standardSeconds(90L))))
            .advanceWatermarkToInfinity();

    // Create an input PCollection.
    PCollection<String> input = p.apply("Create elements", teststream);

    // Apply the same windowing function as in the sample with a fixed time interval of 1 minute.
    PCollection<String> output =
        input.apply(
            "Apply windowing function",
            Window.<String>into(FixedWindows.of(Duration.standardMinutes(1L)))
                .triggering(
                    Repeatedly.forever(
                        AfterProcessingTime.pastFirstElementInPane()
                            .plusDelayOf(Duration.standardSeconds(30))))
                .withAllowedLateness(Duration.ZERO)
                .accumulatingFiredPanes());

    IntervalWindow firstWindow = new IntervalWindow(baseTime, Duration.standardMinutes(1));
    IntervalWindow secondWindow =
        new IntervalWindow(baseTime.plus(Duration.standardMinutes(1)), Duration.standardMinutes(1));

    // Assert that the first window has produced two elements.
    PAssert.that(output).inWindow(firstWindow).containsInAnyOrder("Hello", "Hello again");

    // Assert that the second window has produced one element.
    PAssert.that(output).inWindow(secondWindow).containsInAnyOrder("Hello last time");

    // Test writing the windowed elements to files locally instead of on Cloud Storage.
    output.apply("Write elements to files", new WriteOneFilePerWindow("output", 1));

    // Assert that the pipeline has created two files.
    assertThat(Files.exists(Paths.get("output-00:00-00:01-0-of-1")));
    assertThat(Files.exists(Paths.get("output-00:01-00:02-0-of-1")));

    // Run the pipeline.
    p.run().waitUntilFinish();
  }
}
