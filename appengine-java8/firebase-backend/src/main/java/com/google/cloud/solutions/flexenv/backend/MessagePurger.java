/*
 * Copyright 2018 Google LLC
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

package com.google.cloud.solutions.flexenv.backend;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.lang.Override;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

/**
 * MessagePurger is responsible for purging messages pushed under
 * registered parent keys. If the number of entries exceeds "maxLogs",
 * the excess entries are purged. It checks each registered 
 * parent key under regular interval, "purgeInterval".
 *
 * @author teppeiy
 */
public class MessagePurger extends Thread {
  private static Logger logger = Logger.getLogger(MessagePurger.class.getName());

  private DatabaseReference firebase;
  private int purgeInterval;
  private int purgeLogs;
  private ConcurrentLinkedQueue<String> branches;

  public MessagePurger(DatabaseReference firebase, int purgeInterval, int purgeLogs) {
    this.setDaemon(true);
    this.firebase = firebase;
    this.purgeInterval = purgeInterval;
    this.purgeLogs = purgeLogs;
    branches = new ConcurrentLinkedQueue<String>();
  }

  public void registerBranch(String branchKey) {
    branches.add(branchKey);
  }

  public void run() {
    while (true) {
      try {
        Thread.sleep(purgeInterval);

        Iterator<String> iter = branches.iterator();
        while (iter.hasNext()) {
          final String branchKey = (String) iter.next();
          // Query to check whether entries exceed "maxLogs".
          Query query = firebase.child(branchKey).orderByKey().limitToFirst(purgeLogs);
          query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
              // If entries are less than "maxLogs", do nothing.
              if (snapshot.getChildrenCount() == purgeLogs) {
                for (DataSnapshot child : snapshot.getChildren()) {
                  firebase.child(branchKey + "/" + child.getKey()).removeValue();
                }
              }
            }

            @Override
            public void onCancelled(DatabaseError error) {
              logger.warning(error.getDetails());
            }
          });
        }
      } catch (InterruptedException ie) {
        logger.warning(ie.getMessage());
        break;
      }
    }
  }
}
