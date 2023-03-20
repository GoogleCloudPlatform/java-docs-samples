/*
 * Copyright 2017 Google Inc.
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

package com.example.flexible.pubsub;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;

/**
 * Copyright 2017 Google Inc.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class PubSubPushTest {

  @Test
  public void messageReceivedOverPushEndPointIsSaved() throws Exception {
    MessageRepository messageRepository = mock(MessageRepository.class);
    List<Message> messages = new ArrayList<>();
    doAnswer((invocation) -> {
        messages.add((Message)invocation.getArguments()[0]);
        return null;
      }
    ).when(messageRepository).save(any(Message.class));
    HttpServletRequest request = mock(HttpServletRequest.class);
    assertNotNull(System.getenv("PUBSUB_VERIFICATION_TOKEN"));
    when(request.getParameter("token"))
        .thenReturn(System.getenv("PUBSUB_VERIFICATION_TOKEN"));

    HttpServletResponse response = mock(HttpServletResponse.class);
    BufferedReader reader = mock(BufferedReader.class);
    when (request.getReader()).thenReturn(reader);
    Stream<String> requestBody = Stream.of(
        "{\"message\":{\"data\":\"dGVzdA==\",\"attributes\":{},"
                    + "\"messageId\":\"91010751788941\",\"publishTime\":\"2017-04-05T23:16:42.302Z\"}}");
    when(reader.lines()).thenReturn(requestBody);
    PubSubPush servlet = new PubSubPush(messageRepository);
    assertEquals(messages.size(), 0);
    servlet.doPost(request, response);
    assertEquals(messages.size(), 1);
  }
}

