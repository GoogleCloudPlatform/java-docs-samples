/*
 * Copyright 2023 Google LLC
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

import com.google.cloud.bigtable.data.v2.models.ChangeStreamMutation;
import com.google.cloud.bigtable.data.v2.models.ChangeStreamMutation.MutationType;
import com.google.cloud.bigtable.data.v2.models.DeleteCells;
import com.google.cloud.bigtable.data.v2.models.DeleteFamily;
import com.google.cloud.bigtable.data.v2.models.Entry;
import com.google.cloud.bigtable.data.v2.models.SetCell;
import com.google.protobuf.ByteString;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.bigtable.BigtableIO;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Count;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.FlatMapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.Top;
import org.apache.beam.sdk.transforms.windowing.AfterProcessingTime;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.GlobalWindows;
import org.apache.beam.sdk.transforms.windowing.Repeatedly;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.joda.time.Duration;
import org.joda.time.Instant;

public class SongRank {

  public static void main(String[] args) {
    BigtableOptions options =
        PipelineOptionsFactory.fromArgs(args).withValidation().as(
            BigtableOptions.class);
    Pipeline p = Pipeline.create(options);
    p.apply("Stream from Bigtable",
            BigtableIO.readChangeStream()
                .withProjectId(options.getBigtableProjectId())
                .withInstanceId(options.getBigtableInstanceId())
                .withTableId(options.getBigtableTableId())
                .withAppProfileId(options.getBigtableAppProfile())
                .withHeartbeatDuration(Duration.standardSeconds(1))
        )
        .apply("Add key", ParDo.of(new ExtractSongName()))
        .apply("Windowing", Window.into(FixedWindows.of(Duration.standardSeconds(30))))
//         .apply("Windowing", Window.<String>into(new GlobalWindows())
//             .withAllowedLateness(Duration.standardDays(100))
//             .triggering(Repeatedly.forever(AfterProcessingTime.pastFirstElementInPane()
//                 .plusDelayOf(Duration.standardSeconds(30))))
//             .discardingFiredPanes())
        .apply(Count.perElement())
        .apply("Top songs", Top.of(5, new SongComparator()).withoutDefaults())
        .apply("Print", ParDo.of(new PrintFn()));
    p.run().waitUntilFinish();
  }


  private static class ExtractSongName extends DoFn<KV<ByteString, ChangeStreamMutation>, String> {

    @DoFn.ProcessElement
    public void processElement(ProcessContext c) {

      for (Entry e : Objects.requireNonNull(c.element()).getValue().getEntries()) {
        if (e instanceof SetCell) {
          SetCell setCell = (SetCell) e;
          if (setCell.getFamilyName().equals("data1") && setCell.getQualifier().toStringUtf8()
              .equals("data")) {
            c.outputWithTimestamp(setCell.getValue().toStringUtf8(), c.timestamp());
          }
        }
      }
    }
  }

  private static class SongComparator implements Comparator<KV<String, Long>>, Serializable {

    @Override
    public int compare(KV<String, Long> o1, KV<String, Long> o2) {
      return (int) (o1.getValue() - o2.getValue());
    }
  }


  private static class PrintFn extends DoFn<List<KV<String, Long>>, Void> {

    @DoFn.ProcessElement
    public void processElement(ProcessContext c) throws Exception {
      System.out.println(c.timestamp() + " " + Objects.requireNonNull(c.element()));
    }
  }


  public interface BigtableOptions extends DataflowPipelineOptions {

    @Description("The Bigtable project ID, this can be different than your Dataflow project")
    @Default.String("bigtable-project")
    String getBigtableProjectId();

    void setBigtableProjectId(String bigtableProjectId);

    @Description("The Bigtable instance ID")
    @Default.String("bigtable-instance")
    String getBigtableInstanceId();

    void setBigtableInstanceId(String bigtableInstanceId);

    @Description("The Bigtable table ID in the instance.")
    @Default.String("change-stream-hello-world")
    String getBigtableTableId();

    void setBigtableTableId(String bigtableTableId);

    @Description("The Bigtable application profile in the instance.")
    @Default.String("default")
    String getBigtableAppProfile();

    void setBigtableAppProfile(String bigtableAppProfile);
  }
}
