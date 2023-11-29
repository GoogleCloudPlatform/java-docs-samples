package com.example.demo;

import com.google.api.client.util.Preconditions;
import com.google.cloud.trace.v2.TraceServiceClient;
import com.google.cloud.trace.v2.TraceServiceSettings;
import com.google.devtools.cloudtrace.v2.ProjectName;
import com.google.devtools.cloudtrace.v2.Span;
import com.google.devtools.cloudtrace.v2.SpanName;
import com.google.devtools.cloudtrace.v2.TruncatableString;
import com.google.protobuf.Timestamp;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.commons.codec.binary.Hex;
import org.threeten.bp.Duration;

public class TestUploadTrace {
  private static final String PROJECT_ID =
      Preconditions.checkNotNull(System.getenv("GOOGLE_CLOUD_PROJECT"));

  private static Timestamp fromInstant(Instant instant) {
    return Timestamp.newBuilder()
        .setSeconds(instant.getEpochSecond())
        .setNanos(instant.getNano())
        .build();
  }

  public static void main(String[] args) throws IOException {
    System.out.println("hello from TestUploadTrace!");

    TraceServiceSettings.Builder settingsBuilder = TraceServiceSettings.newBuilder();
    settingsBuilder.batchWriteSpansSettings().setSimpleTimeoutNoRetries(Duration.ofSeconds(10));

    try (TraceServiceClient traceServiceClient =
        TraceServiceClient.create(settingsBuilder.build())) {

      ProjectName name = ProjectName.of(PROJECT_ID);
      List<Span> spans = new ArrayList<>();

      String spanId = "c18747b8b4fee5f9";

      Instant now = Instant.now();
      byte[] traceBytes = new byte[16];
      ThreadLocalRandom.current().nextBytes(traceBytes);
      String traceId = Hex.encodeHexString(traceBytes);

      spans.add(
          Span.newBuilder()
              .setName(
                  SpanName.newBuilder()
                      .setProject(PROJECT_ID)
                      .setTrace(traceId)
                      .setSpan(spanId)
                      .build()
                      .toString())
              .setSpanId(spanId)
              .setDisplayName(TruncatableString.newBuilder().setValue("Foo").build())
              .setStartTime(fromInstant(now.minusSeconds(1)))
              .setEndTime(fromInstant(now))
              .build());

      traceServiceClient.batchWriteSpans(name, spans);

      System.out.println("Wrote spans succesfully:\n" + spans.toString());
    }
  }
}
