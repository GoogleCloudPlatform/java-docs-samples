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
import com.google.cloud.bigtable.data.v2.models.Entry;
import com.google.cloud.bigtable.data.v2.models.SetCell;
import com.google.protobuf.ByteString;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.io.gcp.bigtable.BigtableIO;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.Validation;
import org.apache.beam.sdk.transforms.Count;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.Top;
import org.apache.beam.sdk.transforms.windowing.AfterFirst;
import org.apache.beam.sdk.transforms.windowing.AfterPane;
import org.apache.beam.sdk.transforms.windowing.AfterProcessingTime;
import org.apache.beam.sdk.transforms.windowing.GlobalWindows;
import org.apache.beam.sdk.transforms.windowing.Repeatedly;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.vendor.guava.v26_0_jre.com.google.common.base.Preconditions;
import org.joda.time.Duration;
import org.joda.time.Instant;

public class SongRank {

  public static void main(String[] args) {
    BigtableOptions options =
        PipelineOptionsFactory.fromArgs(args).withValidation().as(
            BigtableOptions.class);
    Pipeline p = Pipeline.create(options);

    Preconditions.checkArgument(options.getOutputLocation().endsWith("/"),
        "Output location must end with a slash.");

    // [START bigtable_cdc_tut_readchangestream]
    p.apply(
            "Stream from Bigtable",
            BigtableIO.readChangeStream()
                .withProjectId(options.getBigtableProjectId())
                .withInstanceId(options.getBigtableInstanceId())
                .withTableId(options.getBigtableTableId())
                .withAppProfileId(options.getBigtableAppProfile())

        )
        // [END bigtable_cdc_tut_readchangestream]
        .apply("Add key", ParDo.of(new ExtractSongName()))
        .apply(
            "Collect listens in 5 second windows",
            Window.<String>into(new GlobalWindows())
                .triggering(
                    Repeatedly.forever(
                        AfterProcessingTime
                            .pastFirstElementInPane()
                            .plusDelayOf(Duration.standardSeconds(10))
                    ))
                .discardingFiredPanes())
        // [START bigtable_cdc_tut_countrank]
        .apply(Count.perElement())
        .apply("Top songs", Top.of(5, new SongComparator()).withoutDefaults())
        // [END bigtable_cdc_tut_countrank]
        // [START bigtable_cdc_tut_output]
        .apply("Print", ParDo.of(new PrintFn()))
        .apply(
            "Collect at least 10 elements or 1 minute of elements",
            Window.<String>into(new GlobalWindows())
                .triggering(
                    Repeatedly.forever(
                        AfterFirst.of(
                            AfterPane.elementCountAtLeast(10),
                            AfterProcessingTime
                                .pastFirstElementInPane()
                                .plusDelayOf(Duration.standardMinutes(1)
                                )
                        )
                    ))
                .discardingFiredPanes())
        .apply(
            "Output top songs",
            TextIO.write()
                .to(options.getOutputLocation() + "song-charts/")
                .withSuffix(".txt")
                .withNumShards(1)
                .withWindowedWrites()
        );
    // [END bigtable_cdc_tut_output]

    p.run();
  }


  // [START bigtable_cdc_tut_songname]
  private static class ExtractSongName extends DoFn<KV<ByteString, ChangeStreamMutation>, String> {

    @DoFn.ProcessElement
    public void processElement(ProcessContext c) {

      for (Entry e : Objects.requireNonNull(Objects.requireNonNull(c.element()).getValue())
          .getEntries()) {
        if (e instanceof SetCell) {
          SetCell setCell = (SetCell) e;
          if (setCell.getFamilyName().equals("cf") && setCell.getQualifier().toStringUtf8()
              .equals("song")) {
            c.output(setCell.getValue().toStringUtf8());
          }
        }
      }
    }
  }
  // [END bigtable_cdc_tut_songname]

  private static class SongComparator implements Comparator<KV<String, Long>>, Serializable {

    @Override
    public int compare(KV<String, Long> o1, KV<String, Long> o2) {
      return (int) (o1.getValue() - o2.getValue());
    }
  }


  private static class PrintFn extends DoFn<List<KV<String, Long>>, String> {

    @DoFn.ProcessElement
    public void processElement(ProcessContext c) throws Exception {
      String result = Instant.now() + " " + Objects.requireNonNull(c.element());
      System.out.println(result);
      c.output(result);
    }
  }


  public interface BigtableOptions extends DataflowPipelineOptions {

    @Description("The Bigtable project ID, this can be different than your Dataflow project")
    String getBigtableProjectId();

    void setBigtableProjectId(String bigtableProjectId);

    @Description("The Bigtable instance ID")
    String getBigtableInstanceId();

    void setBigtableInstanceId(String bigtableInstanceId);

    @Description("The Bigtable table ID in the instance.")
    String getBigtableTableId();

    void setBigtableTableId(String bigtableTableId);

    @Description("The Bigtable application profile in the instance.")
    @Default.String("default")
    String getBigtableAppProfile();

    void setBigtableAppProfile(String bigtableAppProfile);

    @Description("The location to write to. Begin with gs:// to write to a Cloud Storage bucket. "
        + "End with a slash.")
    String getOutputLocation();

    void setOutputLocation(String value);
  }
}
