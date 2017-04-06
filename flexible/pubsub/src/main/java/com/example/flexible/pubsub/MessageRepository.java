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
package com.example.flexible.pubsub;

import com.google.cloud.datastore.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/** Storage for Message objects using Cloud Datastore. */
public class MessageRepository {
  private static String messagesKind = "messages";
  private KeyFactory keyFactory = getDatastoreInstance().newKeyFactory().setKind(messagesKind);

  /** Create a lazy initialized singleton of MessageRepository. */
  private static class LazyInit {
    private static final MessageRepository INSTANCE;

    static {
      INSTANCE = new MessageRepository();
    }
  }

  public static MessageRepository getInstance() {
    return LazyInit.INSTANCE;
  }

  /** Save message id, data (decoded from base64), publish time */
  public void save(Message message) {
    // Save message to "messages"
    Datastore datastore = getDatastoreInstance();
    Key key = datastore.allocateId(keyFactory.newKey());

    Entity.Builder messageEntityBuilder = Entity.newBuilder(key).set("messageId", message.getId());

    if (message.getData() != null) {
      messageEntityBuilder = messageEntityBuilder.set("data", decode(message.getData()));
    }

    if (message.getPublishTime() != null) {
      messageEntityBuilder = messageEntityBuilder.set("publishTime", message.getPublishTime());
    }
    datastore.put(messageEntityBuilder.build());
  }

  /**
   * Retrieve most recent messages.
   *
   * @param limit number of messages
   * @return list of messages
   */
  public List<Message> retrieve(int limit) {
    // Get Message saved in Datastore
    Datastore datastore = getDatastoreInstance();
    Query<Entity> query =
        Query.newEntityQueryBuilder()
            .setKind(messagesKind)
            .setLimit(limit)
            .addOrderBy(StructuredQuery.OrderBy.desc("publishTime"))
            .build();
    QueryResults<Entity> results = datastore.run(query);

    List<Message> messages = new ArrayList<>();
    while (results.hasNext()) {
      Entity entity = results.next();
      Message message = new Message(entity.getString("messageId"));
      String data = entity.getString("data");
      if (data != null) {
        message.setData(data);
      }
      String publishTime = entity.getString("publishTime");
      if (publishTime != null) {
        message.setPublishTime(publishTime);
      }
      messages.add(message);
    }
    return messages;
  }

  private String decode(String data) {
    return new String(Base64.getDecoder().decode(data));
  }

  private Datastore getDatastoreInstance() {
    return DatastoreOptions.getDefaultInstance().getService();
  }
}
