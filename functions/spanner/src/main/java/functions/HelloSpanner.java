/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package functions;

// [START spanner_functions_quickstart]
import com.google.api.client.http.HttpStatusCodes;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.DatabaseId;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerException;
import com.google.cloud.spanner.SpannerOptions;
import com.google.cloud.spanner.Statement;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

// HelloSpanner is an example of querying Spanner from a Cloud Function.
public class HelloSpanner implements HttpFunction {
  private static final Logger LOGGER = Logger.getLogger(HelloSpanner.class.getName());

  @VisibleForTesting
  static Spanner createSpanner() {
    return SpannerOptions.newBuilder().build().getService();
  }

  // SpannerHolder is a holder class for a Spanner instance that is initialized lazily.
  private static final class SpannerHolder {
    private final Object lock = new Object();
    private volatile boolean initialized;
    private volatile DatabaseClient client;
    private volatile Throwable error;

    private SpannerHolder() {}

    // Initialize the {@link Spanner} instance in a method and not as a static variable, as it
    // might throw an error, and we want to catch and log that specific error. An administrator must
    // take action to mitigate the reason for the initialization failure, for example ensuring that
    // the service account being used to access Cloud Spanner has permission to do so.
    DatabaseClient get() throws Throwable {
      if (!initialized) {
        synchronized (lock) {
          if (!initialized) {
            try {
              DatabaseId db =
                  DatabaseId.of(
                      SpannerOptions.getDefaultProjectId(),
                      SPANNER_INSTANCE_ID,
                      SPANNER_DATABASE_ID);
              client = createSpanner().getDatabaseClient(db);
            } catch (Throwable t) {
              error = t;
            }
            initialized = true;
          }
        }
      }
      if (error != null) {
        throw error;
      }
      return client;
    }
  }

  // The SpannerHolder instance is shared across all instances of the HelloSpanner class.
  private static final SpannerHolder SPANNER_HOLDER = new SpannerHolder();

  @VisibleForTesting
  DatabaseClient getClient() throws Throwable {
    return SPANNER_HOLDER.get();
  }

  // TODO<developer>: Set these environment variables.
  private static final String SPANNER_INSTANCE_ID =
      MoreObjects.firstNonNull(System.getenv("SPANNER_INSTANCE"), "my-instance");
  private static final String SPANNER_DATABASE_ID =
      MoreObjects.firstNonNull(System.getenv("SPANNER_DATABASE"), "example-db");

  @Override
  public void service(HttpRequest request, HttpResponse response) throws Exception {
    var writer = new PrintWriter(response.getWriter());
    try {
      DatabaseClient client = getClient();
      try (ResultSet rs =
          client
              .singleUse()
              .executeQuery(Statement.of("SELECT SingerId, AlbumId, AlbumTitle FROM Albums"))) {
        writer.printf("Albums:\n");
        while (rs.next()) {
          writer.printf(
              "%d %d %s\n",
              rs.getLong("SingerId"), rs.getLong("AlbumId"), rs.getString("AlbumTitle"));
        }
      } catch (SpannerException e) {
        writer.printf("Error querying database: %s\n", e.getMessage());
        response.setStatusCode(HttpStatusCodes.STATUS_CODE_SERVER_ERROR, e.getMessage());
      }
    } catch (Throwable t) {
      LOGGER.log(Level.SEVERE, "Spanner example failed", t);
      writer.printf("Error setting up Spanner: %s\n", t.getMessage());
      response.setStatusCode(HttpStatusCodes.STATUS_CODE_SERVER_ERROR, t.getMessage());
    }
  }
}
// [END spanner_functions_quickstart]
