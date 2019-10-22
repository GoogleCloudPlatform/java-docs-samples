/*
 * Copyright 2016 Google Inc.
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

package com.example.compute.mailjet;

import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for {@link MailjetSender}.
 */
@RunWith(JUnit4.class)
public class MailjetSenderTest {

  @Mock MailjetClient mockClient;
  @Mock MailjetResponse mockResponse;
  private MailjetSender sender;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    Mockito.when(mockClient.post(Matchers.anyObject())).thenReturn(mockResponse);
    sender = new MailjetSender();
  }

  @Test
  public void doGet_defaultEnvironment_writesResponse() throws Exception {
    sender.sendMailjet("fake recipient", "fake sender", mockClient);
    Mockito.verify(mockClient).post(Matchers.anyObject());
  }
}
