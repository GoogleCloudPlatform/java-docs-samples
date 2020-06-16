package com.example.task;

// [START cloud_tasks_list_queues]
import com.google.cloud.tasks.v2.CloudTasksClient;
import com.google.cloud.tasks.v2.LocationName;
import com.google.cloud.tasks.v2.Queue;

public class ListQueues {
  /**
   * List queues using the Cloud Tasks client.
   *
   * @param projectId the Id of the project.
   * @param locationId the GCP region of your queue.
   * @throws Exception on Cloud Tasks Client errors.
   */
  public static void listQueues(String projectId, String locationId)
      throws Exception {

    // Instantiates a client.
    try (CloudTasksClient client = CloudTasksClient.create()) {

      // Construct the fully qualified location path.
      String parent = LocationName.of(projectId, locationId).toString();

      // Send list queues request.
      CloudTasksClient.ListQueuesPagedResponse response = client.listQueues(parent);

      // Iterate over results and print queue names
      int total = 0;
      for(Queue queue : response.iterateAll()){
          System.out.println(queue.getName());
          total++;
      }

      if(total == 0){
          System.out.println("No queues found!");
      }
    }
  }
}
// [END cloud_tasks_list_queues]
