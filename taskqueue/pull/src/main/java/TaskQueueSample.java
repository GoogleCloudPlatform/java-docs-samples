/*
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

// [START import_libraries]
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
// [END import_libraries]
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.taskqueue.Taskqueue;
import com.google.api.services.taskqueue.TaskqueueRequest;
import com.google.api.services.taskqueue.TaskqueueRequestInitializer;
import com.google.api.services.taskqueue.TaskqueueScopes;
import com.google.api.services.taskqueue.model.Task;
import com.google.api.services.taskqueue.model.Tasks;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.codec.binary.Base64;
/**
 * Command-line sample which leases tasks from a pull task queue, processes the payload
 * of the task and then deletes the task.
 */
public class TaskQueueSample {
  /**
   * Be sure to specify the name of your application. If the application name is {@code null} or
   * blank, the application will log a warning. Suggested format is "MyCompany-ProductName/1.0".
   */
  private static final String APPLICATION_NAME = "";
  private static String projectId;
  private static String taskQueueName;
  private static int leaseSecs;
  private static int numTasks;
  /** Global instance of the HTTP transport. */
  // [START intialize_transport]
  private static HttpTransport httpTransport;
  // [END intialize_transport]
  /** Global instance of the JSON factory. */
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  private static void run() throws Exception {
    httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    // [START auth_and_intitalize_client]
    // Authenticate using Google Application Default Credentials.
    GoogleCredential credential = GoogleCredential.getApplicationDefault();
    if (credential.createScopedRequired()) {
      List<String> scopes = new ArrayList<>();
      // Set TaskQueue Scopes
      scopes.add(TaskqueueScopes.TASKQUEUE);
      scopes.add(TaskqueueScopes.TASKQUEUE_CONSUMER);
      credential = credential.createScoped(scopes);
    }
    // Intialize Taskqueue object.
    Taskqueue taskQueue =
        new Taskqueue.Builder(httpTransport, JSON_FACTORY, credential)
            .setApplicationName(APPLICATION_NAME)
            .build();
   // [END auth_and_intitalize_client]

    /* Get the task queue using the name of an existing task queue
    *  listed in the App Engine Task Queue section of the Developers console.
    *  See the following sample for an example App Engine app that creates a
    *  pull task queue:
    *  https://github.com/GoogleCloudPlatform/java-docs-samples/tree/master/appengine/taskqueue
    */
    com.google.api.services.taskqueue.model.TaskQueue queue = getQueue(taskQueue);
    System.out.println("================== Listing Task Queue details ==================");
    System.out.println(queue);
    // Lease, process and delete tasks
    // [START lease_tasks]
    Tasks tasks = getLeasedTasks(taskQueue);
    if (tasks.getItems() == null || tasks.getItems().size() == 0) {
       System.out.println("No tasks to lease, so now exiting");
    } else {
      for (Task leasedTask : tasks.getItems()) {
        processTask(leasedTask);
        deleteTask(taskQueue, leasedTask);
      }
    }
    // [END lease_tasks]
  }

  public static boolean parseParams(String[] args) {
    try {
      projectId = args[0];
      taskQueueName = args[1];
      leaseSecs = Integer.parseInt(args[2]);
      numTasks = Integer.parseInt(args[3]);
      return true;
    } catch (ArrayIndexOutOfBoundsException ae) {
      System.out.println("Insufficient Arguments");
      return false;
    } catch (NumberFormatException ae) {
      System.out.println("Please specify lease seconds "
          + "and Number of tasks to lease, in number "
          + "format");
      return false;
    }
  }
  public static void printUsage() {
    System.out.println("mvn -q exec:java -Dexec.args=\""
        + "<ProjectId> <TaskQueueName> "
        + "<LeaseSeconds> <NumberOfTasksToLease>\"");
  }
  public static void main(String[] args) {
    if (args.length != 4) {
      System.out.println("Insuficient arguments");
      printUsage();
      System.exit(1);
    } else if (!parseParams(args)) {
      printUsage();
      System.exit(1);
    }
    try {
      run();
      // success!
      return;
    } catch (IOException e) {
      System.err.println(e.getMessage());
    } catch (Throwable t) {
      t.printStackTrace();
    }
    System.exit(1);
  }
  /**
   * Method that sends a get request to get the queue.
   *
   * @param taskQueue The task queue that should be used to get the queue from.
   * @return {@link com.google.api.services.taskqueue.model.TaskQueue}
   * @throws IOException if the request fails.
   */
  private static com.google.api.services.taskqueue.model.TaskQueue getQueue(Taskqueue taskQueue)
      throws IOException {
    //[START get_rest_queue]
    Taskqueue.Taskqueues.Get request = taskQueue.taskqueues().get(projectId, taskQueueName);
    request.setGetStats(true);
    return request.execute();
    // [END get_rest_queue]
  }
  /**
   * Method that sends a lease request to the specified task queue.
   *
   * @param taskQueue The task queue that should be used to lease tasks from.
   * @return {@link Tasks}
   * @throws IOException if the request fails.
   */
  private static Tasks getLeasedTasks(Taskqueue taskQueue) throws IOException {
    Taskqueue.Tasks.Lease leaseRequest =
        taskQueue.tasks().lease(projectId, taskQueueName, numTasks, leaseSecs);
    return leaseRequest.execute();
  }
  /**
   * This method actually performs the desired work on tasks. It can make use of payload of the
   * task. By default, we are just printing the payload of the leased task after converting
   * it from Base64 encoding.
   *
   * @param task The task that should be executed.
   */
  private static void processTask(Task task) {
    byte[] payload = Base64.decodeBase64(task.getPayloadBase64());
    if (payload != null) {
      System.out.println("Payload for the task:");
      System.out.println(new String(payload));
    } else {
      System.out.println("This task has no payload.");
    }
  }
  /**
   * Method that sends a delete request for the specified task object to the taskqueue service.
   *
   * @param taskQueue The task queue the specified task lies in.
   * @param task The task that should be deleted.
   * @throws IOException if the request fails
   */
  private static void deleteTask(Taskqueue taskQueue, Task task) throws IOException {
    System.out.println("Deleting task:" + task.getId());
    // [START delete_task]
    String DeletePrefix = "s~";
    String projectIdFormattedForDelete = String.format("%s%s", DeletePrefix, projectId);
    Taskqueue.Tasks.Delete request =
        taskQueue.tasks().delete(projectIdFormattedForDelete, taskQueueName, task.getId());
    request.execute();
    // [END delete_task]
  }
}
