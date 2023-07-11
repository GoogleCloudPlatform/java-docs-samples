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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.bigtable.BigtableIO;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.FlatMapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.joda.time.Instant;

public class ChangeStreamsHelloWorld {

  public static void main(String[] args) {
    // [START bigtable_cdc_hw_pipeline]
    BigtableOptions options =
        PipelineOptionsFactory.fromArgs(args).withValidation().as(BigtableOptions.class);
    Pipeline p = Pipeline.create(options);

    final Instant startTime = Instant.now();

    p.apply(
            "Read Change Stream",
            BigtableIO.readChangeStream()
                .withProjectId(options.getBigtableProjectId())
                .withInstanceId(options.getBigtableInstanceId())
                .withTableId(options.getBigtableTableId())
                .withAppProfileId(options.getBigtableAppProfile())
                .withStartTime(startTime))
        .apply(
            "Flatten Mutation Entries",
            FlatMapElements.into(TypeDescriptors.strings())
                .via(ChangeStreamsHelloWorld::mutationEntriesToString))
        .apply(
            "Print mutations",
            ParDo.of(
                new DoFn<String, Void>() { // a DoFn as an anonymous inner class instance
                  @ProcessElement
                  public void processElement(@Element String mutation) {
                    System.out.println("Change captured: " + mutation);
                  }
                }));
    p.run();
    // [END bigtable_cdc_hw_pipeline]
  }

  // [START bigtable_cdc_hw_tostring_mutation]
  static List<String> mutationEntriesToString(KV<ByteString, ChangeStreamMutation> mutationPair) {
    List<String> mutations = new ArrayList<>();
    String rowKey = mutationPair.getKey().toStringUtf8();
    ChangeStreamMutation mutation = mutationPair.getValue();
    MutationType mutationType = mutation.getType();
    for (Entry entry : mutation.getEntries()) {
      if (entry instanceof SetCell) {
        mutations.add(setCellToString(rowKey, mutationType, (SetCell) entry));
      } else if (entry instanceof DeleteCells) {
        mutations.add(deleteCellsToString(rowKey, mutationType, (DeleteCells) entry));
      } else if (entry instanceof DeleteFamily) {
        // Note: DeleteRow mutations are mapped into one DeleteFamily per-family
        mutations.add(deleteFamilyToString(rowKey, mutationType, (DeleteFamily) entry));
      } else {
        throw new RuntimeException("Entry type not supported.");
      }
    }
    return mutations;
  }
  // [END bigtable_cdc_hw_tostring_mutation]

  // [START bigtable_cdc_hw_tostring_setcell]
  private static String setCellToString(String rowKey, MutationType mutationType, SetCell setCell) {
    List<String> mutationParts =
        Arrays.asList(
            rowKey,
            mutationType.name(),
            "SetCell",
            setCell.getFamilyName(),
            setCell.getQualifier().toStringUtf8(),
            setCell.getValue().toStringUtf8());
    return String.join(",", mutationParts);
  }
  // [END bigtable_cdc_hw_tostring_setcell]

  // [START bigtable_cdc_hw_tostring_deletecell]
  private static String deleteCellsToString(
      String rowKey, MutationType mutationType, DeleteCells deleteCells) {
    String timestampRange =
        deleteCells.getTimestampRange().getStart() + "-" + deleteCells.getTimestampRange().getEnd();
    List<String> mutationParts =
        Arrays.asList(
            rowKey,
            mutationType.name(),
            "DeleteCells",
            deleteCells.getFamilyName(),
            deleteCells.getQualifier().toStringUtf8(),
            timestampRange);
    return String.join(",", mutationParts);
  }
  // [END bigtable_cdc_hw_tostring_deletecell]

  // [START bigtable_cdc_hw_tostring_deletefamily]

  private static String deleteFamilyToString(
      String rowKey, MutationType mutationType, DeleteFamily deleteFamily) {
    List<String> mutationParts =
        Arrays.asList(rowKey, mutationType.name(), "DeleteFamily", deleteFamily.getFamilyName());
    return String.join(",", mutationParts);
  }
  // [END bigtable_cdc_hw_tostring_deletefamily]

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
