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

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.gax.core.SettableApiFuture;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;

public class PubSubPublishTest {

  @Test
  public void servletPublishesPayloadMessage() throws Exception {
    assertNotNull(System.getenv("PUBSUB_TOPIC"));
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getParameter("payload")).thenReturn("test-message");

    HttpServletResponse response = mock(HttpServletResponse.class);
    Publisher publisher = mock(Publisher.class);
    PubsubMessage message = PubsubMessage.newBuilder()
        .setData(ByteString.copyFromUtf8("test-message")).build();
    when(publisher.publish(eq(message))).thenReturn(SettableApiFuture.create());
    PubSubPublish pubSubPublish = new PubSubPublish(publisher);
    // verify content of published test message
    pubSubPublish.doPost(request, response);
    verify(publisher, times(1)).publish(eq(message));
  }
}
