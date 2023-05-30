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

package dlp.snippets;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.pubsub.v1.ProjectSubscriptionName;
import java.util.concurrent.TimeoutException;
import org.junit.Test;

public class MessageParserTests extends TestBase {

  @Override
  protected ImmutableList<String> requiredEnvVars() {
    return ImmutableList.of(
        "GOOGLE_APPLICATION_CREDENTIALS", "GOOGLE_CLOUD_PROJECT", "PUB_SUB_SUBSCRIPTION");
  }

  @Test
  public void testDataProfilePubSubMessageParser() throws TimeoutException {
    DataProfilePubSubMessageParser.parseMessage(PROJECT_ID, SUBSCRIPTION_ID);
    String output = bout.toString();
    ProjectSubscriptionName subscriptionName =
        ProjectSubscriptionName.of(PROJECT_ID, SUBSCRIPTION_ID);
    assertThat(output).contains("Listening for messages on " + subscriptionName);
  }
}
