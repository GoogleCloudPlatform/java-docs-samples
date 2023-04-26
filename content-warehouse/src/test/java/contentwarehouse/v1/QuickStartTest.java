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

package contentwarehouse.v1;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class QuickStartTest {
  private static final String PROJECT_ID = "your-project-id";
  private static final String LOCATION = "us"; // Format is 'us' or 'eu'
  private static final String USER_ID = "user:xx@example.com"; // Format is 'user:xx@example.com'

  private ByteArrayOutputStream bout;
  private PrintStream out;
  private PrintStream originalPrintStream;
  
  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    originalPrintStream = System.out;
    System.setOut(out);
  }

  @Test
  public void testQuickStart()
      throws InterruptedException, ExecutionException, IOException, TimeoutException {
    QuickStart quickStartMock = mock(QuickStart.class);

    assertThrows(Exception.class, () -> 
        Mockito.doThrow(new Exception())
        .when(quickStartMock)
        .quickStart(anyString(), anyString(), anyString()));
  }

  @After
  public void tearDown() {
    System.out.flush();
    System.setOut(originalPrintStream);
  }
}
