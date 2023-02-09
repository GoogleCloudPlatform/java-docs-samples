package dlp.snippets;

import com.google.api.gax.rpc.ApiException;
import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.privacy.dlp.v2.DeleteInspectTemplateRequest;
import com.google.privacy.dlp.v2.DeleteStoredInfoTypeRequest;
import com.google.privacy.dlp.v2.DlpJob;
import com.google.privacy.dlp.v2.DlpJobType;
import com.google.privacy.dlp.v2.ListDlpJobsRequest;
import com.google.privacy.dlp.v2.RiskAnalysisJobConfig;
import java.io.IOException;

public class DeleteJob {

  public static void main(String[] args) throws IOException {

    // String dlpJobName;
    // DeleteDlpJobRequest deleteDlpJobRequest =
    //     DeleteDlpJobRequest.newBuilder().setName(dlpJobName).build();

    try (DlpServiceClient client = DlpServiceClient.create()) {

      for (DlpJob job : client.listDlpJobs(ListDlpJobsRequest.newBuilder()
              .setParent("projects/java-docs-samples-testing")
              .setLocationId("global")
              .setType(DlpJobType.RISK_ANALYSIS_JOB)
          .build()).iterateAll()) {
        client.deleteDlpJob(job.getName());
      }

      // client.deleteDlpJob("projects/java-docs-samples-testing/locations/global/dlpJobs/r-7040431212525235833");
      // client.delete
      //
      // try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      //   topicAdminClient.deleteTopic(topicName);
      // } catch (ApiException e) {
      //   System.err.println(String.format("Error deleting topic %s: %s", topicName.getTopic(), e));
      //   // Keep trying to clean up
      // }
      //
      // // Delete the test subscription
      // try (SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create()) {
      //   subscriptionAdminClient.deleteSubscription(subscriptionName);
      // } catch (ApiException e) {
      //   System.err.println(
      //       String.format(
      //           "Error deleting subscription %s: %s", subscriptionName.getSubscription(), e));
      //   // Keep trying to clean up
      // }
    }
  }

}
