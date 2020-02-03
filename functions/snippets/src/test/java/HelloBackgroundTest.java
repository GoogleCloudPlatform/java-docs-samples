/*
 * Copyright 2019 Google LLC
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

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Optional;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Logger.class, HelloBackground.class})
public class HelloBackgroundTest {
  @Mock private static Logger loggerInstance;

  @Mock private HttpRequest request;
  @Mock private HttpResponse response;

  private EnvironmentVariables environmentVariables;

  @Before
  public void beforeTest() throws Exception {
    environmentVariables = new EnvironmentVariables();

    // Use a new mock for each test
    request = mock(HttpRequest.class);
    response = mock(HttpResponse.class);

    BufferedReader reader = new BufferedReader(new StringReader("{}"));
    when(request.getReader()).thenReturn(reader);

    BufferedWriter writer = new BufferedWriter(new StringWriter());
    when(response.getWriter()).thenReturn(writer);

    // Capture logs
    PowerMockito.mockStatic(Logger.class);
    if (loggerInstance == null) {
      loggerInstance = mock(Logger.class);
    }

    when(Logger.getLogger(anyString())).thenReturn(loggerInstance);
  }

  @After
  public void afterTest() {
    request = null;
    response = null;
    Mockito.reset();
  }

  @Test
  public void helloBackground_printsName() throws Exception {

    when(request.getFirstQueryParameter("name")).thenReturn(Optional.of("John"));

    new HelloBackground().accept(request, null);
    verify(loggerInstance, times(1)).info("Hello John!");
  }

  @Test
  public void helloBackground_printsHelloWorld() throws Exception {
    new HelloBackground().accept(request, null);

    verify(loggerInstance, times(1)).info("Hello world!");
  }
}
