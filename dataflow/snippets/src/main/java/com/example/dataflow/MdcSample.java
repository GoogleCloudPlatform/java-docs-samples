/*
 * Copyright 2025 Google LLC
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

package com.example.dataflow;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubMessage;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.SdkHarnessOptions;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class MdcSample {

  public interface MdcSampleJobOptions extends SdkHarnessOptions {
    @Description("The Pub/Sub subscription to read from.")
    String getInputSubscription();

    void setInputSubscription(String value);
  }

  public static class MessageReaderFn extends DoFn<PubsubMessage, Void> {

    private transient Logger logger;

    @Setup
    public void setup() {
      logger = LoggerFactory.getLogger(MessageReaderFn.class);
    }

    @ProcessElement
    public void processElement(ProcessContext c) {
      PubsubMessage message = c.element();
      String messageId = message.getMessageId();

      try (MDC.MDCCloseable ignored = MDC.putCloseable("messageId", messageId)) {
        String payload = new String(message.getPayload(), java.nio.charset.StandardCharsets.UTF_8);
        logger.info("Received message with payload: {}", payload);

        // This is the example task
        logger.info("Executing example task...");
      } catch (Exception e) {
        logger.error("Failed to process message", e);
      }
    }
  }

  public static void main(String[] args) {
    MdcSampleJobOptions options =
        PipelineOptionsFactory.fromArgs(args).withValidation().as(MdcSampleJobOptions.class);
    // options.setRunner(DirectRunner.class);

    options.setLogMdc(true);


    Pipeline p = Pipeline.create(options);

    p.apply(
            "Read Messages from Pub/Sub",
            PubsubIO.readMessagesWithAttributes().fromSubscription(options.getInputSubscription()))
        .apply("Process Message", ParDo.of(new MessageReaderFn()));

    p.run();
  }
}