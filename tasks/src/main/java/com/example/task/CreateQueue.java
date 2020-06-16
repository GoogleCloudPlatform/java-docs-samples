package com.example.task;

// [START cloud_tasks_create_queue]
import com.google.cloud.tasks.v2.CloudTasksClient;
import com.google.cloud.tasks.v2.LocationName;
import com.google.cloud.tasks.v2.QueueName;
import com.google.cloud.tasks.v2.Queue;

public class CreateQueue {
  /**
   * Create a queue using the Cloud Tasks client.
   *
   * @param projectId the Id of the project.
   * @param queueId the name of your Queue.
   * @param locationId the GCP region of your queue.
   * @throws Exception on Cloud Tasks Client errors.
   */
  public static void createQueue(String projectId, String locationId, String queueId)
      throws Exception {

    // Instantiates a client.
    try (CloudTasksClient client = CloudTasksClient.create()) {

      // Construct the fully qualified location.
      String parent = LocationName.of(projectId, locationId).toString();

      // Construct the fully qualified queue path.
      String queuePath = QueueName.of(projectId, locationId, queueId).toString();

      // Send create queue request.
      Queue queue = client.createQueue(parent, Queue.newBuilder().setName(queuePath).build());

      System.out.println("Queue created: " + queue.getName());
    }
  }
}
// [END cloud_tasks_create_queue]
