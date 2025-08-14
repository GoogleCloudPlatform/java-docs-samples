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

package dlp.snippets;

// [START dlp_process_inspect_findings_saved_to_gcs]

import com.google.privacy.dlp.v2.Finding;
import com.google.privacy.dlp.v2.SaveToGcsFindingsOutput;
import com.google.protobuf.ByteString;
import com.google.protobuf.TextFormat;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class ProcessInspectFindingsSavedToGcs {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String inputPath = "src/test/resources/save_to_gcs_findings.txt";
    processFindingsGcsFile(inputPath);
  }

  // Processes a file containing findings from a DLP inspect job.
  public static void processFindingsGcsFile(String inputPath)
      throws IOException {
    SaveToGcsFindingsOutput.Builder builder = SaveToGcsFindingsOutput.newBuilder();
    try (Reader reader =
        new InputStreamReader(new FileInputStream(inputPath), StandardCharsets.UTF_8)) {
      TextFormat.merge(reader, builder);
    }
    SaveToGcsFindingsOutput output = builder.build();

    // Parse the converted proto and process results
    System.out.println("Findings: " + output.getFindingsCount());
    for (Finding f : output.getFindingsList()) {
      System.out.println("\tInfo type: " + f.getInfoType().getName());
      System.out.println("\tLikelihood: " + f.getLikelihood());
    }
  }
}
// [END dlp_process_inspect_findings_saved_to_gcs]