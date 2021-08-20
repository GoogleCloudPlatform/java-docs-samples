/*
 * Copyright 2020 Google LLC
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

package com.example;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.google.common.truth.Truth.assertThat;

@RunWith(JUnit4.class)
public class JobsExampleTest {

  private String runSample(int failRate) throws IOException, InterruptedException {
    // Initialize the JAR-running process
    String baseDir = System.getProperty("user.dir");

    ProcessBuilder builder = new ProcessBuilder()
        .command("java", "-jar", "target/app-0.0.1.jar")
        .directory(new File(baseDir));

    Map<String, String> env = builder.environment();
    env.put("FAIL_RATE", Integer.toString(failRate));

    // Run the JAR + get its output
    Process process = builder.start();
    ByteArrayOutputStream outBytes = new ByteArrayOutputStream();

    InputStream stdoutStream = process.getInputStream();
    InputStream stderrStream = process.getErrorStream();

    Thread.sleep(500);

    outBytes.write(stdoutStream.readNBytes(stdoutStream.available()));
    outBytes.write(stderrStream.readNBytes(stderrStream.available()));

    String output = outBytes.toString(StandardCharsets.UTF_8);

    // Terminate the JAR
    if (process.isAlive()) {
      process.destroy();
    }

    // Done!
    return output;
  }

  @Test
  public void handlesSuccess() throws Exception {
    assertThat(runSample(0)).contains("Completed Task 0");
  }

  @Test
  public void handlesFailure() throws Exception {
    assertThat(runSample(1)).contains("Task 0, Attempt 0 failed.");
  }
}
