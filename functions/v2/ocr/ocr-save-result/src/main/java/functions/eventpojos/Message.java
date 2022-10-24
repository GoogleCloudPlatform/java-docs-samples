/*
 * Copyright 2022 Google LLC
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

package functions.eventpojos;

import java.time.OffsetDateTime;
import java.util.Map;

// Represents a PubSub message
// https://cloud.google.com/pubsub/docs/reference/rest/v1/PubsubMessage
@lombok.Data
public class Message {
    private Map<String, String> attributes;
    private String data;
    private String messageID;
    private String orderingKey;
    private OffsetDateTime publishTime;
}