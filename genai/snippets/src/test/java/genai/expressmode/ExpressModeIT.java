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

package genai.expressmode;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.genai.Client;
import com.google.genai.Models;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockedStatic;

@RunWith(JUnit4.class)
public class ExpressModeIT {

  private static final String GEMINI_FLASH = "gemini-2.5-flash";
  private ByteArrayOutputStream bout;
  private PrintStream out;

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
  }

  @After
  public void tearDown() {
    System.setOut(null);
  }

  @Test
  public void testExpressModeWithApiKey() throws NoSuchFieldException, IllegalAccessException {
    String response = "Bubble sort is one of the simplest sorting algorithms";

    Client.Builder mockBuilder = mock(Client.Builder.class, RETURNS_SELF);
    Client mockClient = mock(Client.class);
    Models mockModels = mock(Models.class);
    GenerateContentResponse mockResponse = mock(GenerateContentResponse.class);

    try (MockedStatic<Client> mockStatic = mockStatic(Client.class)) {
      mockStatic.when(Client::builder).thenReturn(mockBuilder);
      when(mockBuilder.build()).thenReturn(mockClient);

      // Using reflection because 'models' is a final field and cannot be mocked directly
      Field field = Client.class.getDeclaredField("models");
      field.setAccessible(true);
      field.set(mockClient, mockModels);

      when(mockClient.models.generateContent(
              anyString(), anyString(), any(GenerateContentConfig.class)))
          .thenReturn(mockResponse);
      when(mockResponse.text()).thenReturn(response);

      String generatedResponse = ExpressModeWithApiKey.generateContent(GEMINI_FLASH, "API_KEY");

      verify(mockClient.models, times(1))
          .generateContent(anyString(), anyString(), any(GenerateContentConfig.class));
      assertThat(generatedResponse).isNotEmpty();
      assertThat(response).isEqualTo(generatedResponse);
    }
  }
}
