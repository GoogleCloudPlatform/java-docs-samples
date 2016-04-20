/**
 * Copyright 2016 Google Inc. All Rights Reserved.
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

package com.example.appengine.remote;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.remoteapi.RemoteApiInstaller;
import com.google.appengine.tools.remoteapi.RemoteApiOptions;

import java.io.IOException;

// [START example]
public class RemoteApiExample {
  private static final String SERVER_STRING = "YOUR-APP-ID.appspot.com";

  public static void main(String[] args) throws IOException {
    RemoteApiOptions options;
    if (SERVER_STRING.equals("localhost")) {
      options = new RemoteApiOptions().server(SERVER_STRING,
        8080).useDevelopmentServerCredential();
    } else {
      options = new RemoteApiOptions().server(SERVER_STRING,
        443).useApplicationDefaultCredential();
    }
    RemoteApiInstaller installer = new RemoteApiInstaller();
    installer.install(options);
    try {
      DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
      System.out.println("Key of new entity is " + ds.put(new Entity("Hello Remote API!")));
    } finally {
      installer.uninstall();
    }
  }
}
//[END example]