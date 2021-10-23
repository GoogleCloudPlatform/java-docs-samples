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

import com.google.cloud.solutions.flexenv.common.LogEntry;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import java.io.IOException;
import java.lang.Override;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * MessageProcessorServlet is responsible for receiving user event logs
 * from clients and printing logs when requested.
 *
 * @author teppeiy
 */
public class MessageProcessorServlet extends HttpServlet {
  private static final long serialVersionUID = 8126789192972477663L;

  // Firebase keys shared with client apps
  private static final String IBX = "inbox";
  private static final String CH = "channels";
  private static final String REQLOG = "requestLogger";

  // The Logger object records application-level events. The events are
  // displayed in the local console if the servlet is running on the local
  // server, or in GCP Console if the servlet is running on the cloud.
  private static Logger localLog = Logger.getLogger(MessageProcessorServlet.class.getName());
  private DatabaseReference firebase;

  private String channels;
  private String inbox;

  // If the number of messages or user events in each channel exceeds
  // the value of purgeLogs, they are purged.
  private int purgeLogs;
  // Purger is invoked with every "purgeInterval".
  private int purgeInterval;
  private MessagePurger purger;

  private ConcurrentLinkedQueue<LogEntry> logs;

  @Override
  public void init(ServletConfig config) {
    final String credential = config.getInitParameter("credential");
    final String databaseUrl = config.getInitParameter("databaseUrl");
    channels = config.getInitParameter("channels");
    purgeLogs = Integer.parseInt(config.getInitParameter("purgeLogs"));
    purgeInterval = Integer.parseInt(config.getInitParameter("purgeInterval"));

    logs = new ConcurrentLinkedQueue<LogEntry>();
    generateUniqueId();

    localLog.info("Credential file : " + credential);
    FirebaseOptions options = new FirebaseOptions.Builder()
        .setServiceAccount(config.getServletContext().getResourceAsStream(credential))
        .setDatabaseUrl(databaseUrl)
        .build();
    FirebaseApp.initializeApp(options);
    firebase = FirebaseDatabase.getInstance().getReference();

    // [START replyToRequest]
    /*
     * Receive a request from a client and reply back its inbox ID.
     * Using a transaction ensures that only a single servlet instance replies
     * to the client. This lets the client know to which servlet instance
     * send consecutive user event logs.
     */
    firebase.child(REQLOG).addChildEventListener(new ChildEventListener() {
      public void onChildAdded(DataSnapshot snapshot, String prevKey) {
        firebase.child(IBX + "/" + snapshot.getValue()).runTransaction(new Transaction.Handler() {
          public Transaction.Result doTransaction(MutableData currentData) {
            // Only the first servlet instance writes its ID to the client inbox.
            if (currentData.getValue() == null) {
              currentData.setValue(inbox);
            }
            return Transaction.success(currentData);
          }

          public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {}
        });
        firebase.child(REQLOG).removeValue();
      }
      // [START_EXCLUDE]

      public void onCancelled(DatabaseError error) {
        localLog.warning(error.getDetails());
      }

      public void onChildChanged(DataSnapshot snapshot, String prevKey) {}

      public void onChildMoved(DataSnapshot snapshot, String prevKey) {}

      public void onChildRemoved(DataSnapshot snapshot) {}
      // [END_EXCLUDE]
    });
    // [END replyToRequest]

    purger = new MessagePurger(firebase, purgeInterval, purgeLogs);
    String[] channelArray = channels.split(",");
    for (int i = 0; i < channelArray.length; i++) {
      purger.registerBranch(CH + "/" + channelArray[i]);
    }
    initLogger();
    purger.setPriority(Thread.MIN_PRIORITY);
    purger.start();
  }

  /*
   * To generate a unique ID for each servlet instance and clients
   * push messages to "/inbox/<inbox>".
   */
  private void generateUniqueId() {
    Random rand = new Random();
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < 16; i++) {
      buf.append(Integer.toString(rand.nextInt(10)));
    }
    inbox = buf.toString();
  }

  // [START initializeEventLogger]
  /*
   * Initialize user event logger. This is just a sample implementation to
   * demonstrate receiving updates. A production version of this app should
   * transform, filter, or load to another data store such as Google BigQuery.
   */
  private void initLogger() {
    String loggerKey = IBX + "/" + inbox + "/logs";
    purger.registerBranch(loggerKey);
    firebase.child(loggerKey).addChildEventListener(new ChildEventListener() {
      public void onChildAdded(DataSnapshot snapshot, String prevKey) {
        if (snapshot.exists()) {
          LogEntry entry = snapshot.getValue(LogEntry.class);
          logs.add(entry);
        }
      }

      public void onCancelled(DatabaseError error) {
        localLog.warning(error.getDetails());
      }

      public void onChildChanged(DataSnapshot arg0, String arg1) {}

      public void onChildMoved(DataSnapshot arg0, String arg1) {}

      public void onChildRemoved(DataSnapshot arg0) {}
    });
  }
  // [END initializeEventLogger]

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    doPost(req, resp);
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, 
   * javax.servlet.http.HttpServletResponse)
   * Just printing all user event logs stored in memory of this servlet instance.
   */
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setContentType("text/plain");
    resp.getWriter().println("Inbox : " + inbox);

    for (Iterator<LogEntry> iter = logs.iterator(); iter.hasNext();) {
      LogEntry entry = (LogEntry) iter.next();
      resp.getWriter().println(new Date(entry.getTimeLong()).toString() + "(id=" + entry.getTag()
          + ")" +  " : " + entry.getLog());
    }
  }

  @Override
  public void destroy() {
    purger.interrupt();
    firebase.child(IBX + "/" + inbox).removeValue();
  }
}
