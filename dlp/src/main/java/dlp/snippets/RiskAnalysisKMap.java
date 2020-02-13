
/*
 * Copyright 2020 Google LLC
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

package dlp.snippets;
// [START dlp_k_map]
import com.google.pubsub.v1.PubsubMessage;
import com.google.api.core.SettableApiFuture;
import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.privacy.dlp.v2.Action;
import com.google.privacy.dlp.v2.Action.PublishToPubSub;
import com.google.privacy.dlp.v2.AnalyzeDataSourceRiskDetails.KMapEstimationResult;
import com.google.privacy.dlp.v2.AnalyzeDataSourceRiskDetails.KMapEstimationResult.KMapEstimationHistogramBucket;
import com.google.privacy.dlp.v2.AnalyzeDataSourceRiskDetails.KMapEstimationResult.KMapEstimationQuasiIdValues;
import com.google.privacy.dlp.v2.BigQueryTable;
import com.google.privacy.dlp.v2.CreateDlpJobRequest;
import com.google.privacy.dlp.v2.DlpJob;
import com.google.privacy.dlp.v2.FieldId;
import com.google.privacy.dlp.v2.GetDlpJobRequest;
import com.google.privacy.dlp.v2.InfoType;
import com.google.privacy.dlp.v2.PrivacyMetric;
import com.google.privacy.dlp.v2.PrivacyMetric.KMapEstimationConfig;
import com.google.privacy.dlp.v2.PrivacyMetric.KMapEstimationConfig.TaggedField;
import com.google.privacy.dlp.v2.ProjectName;
import com.google.privacy.dlp.v2.RiskAnalysisJobConfig;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.ProjectTopicName;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class RiskAnalysisKMap {

    public static void calculateKMap() throws Exception {
        // TODO(developer): Replace these variables before running the sample.
        String projectId = "your-project-id";
        String datasetId = "your-bigquery-dataset-id";
        String tableId = "your-bigquery-table-id";
        String topicId = "pub-sub-topic";
        String subscriptionId = "pub-sub-subscription";
        List<String> quasiIdColumns = Arrays.asList("age", "gender");
        List<String> infoTypeNames = Arrays.asList("AGE", "GENDER");
        calculateKMap(
                projectId, datasetId, tableId, topicId,
                subscriptionId, quasiIdColumns, infoTypeNames);
    }

    public static void calculateKMap(
            String projectId,
            String datasetId,
            String tableId,
            String topicId,
            String subscriptionId,
            List<String> quasiIds,
            List<String> infoTypeNames) throws Exception {

        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests. After completing all of your requests, call
        // the "close" method on the client to safely clean up any remaining background resources.
        try (DlpServiceClient dlpServiceClient = DlpServiceClient.create()) {



            // Specify the BigQuery table to analyze
            BigQueryTable bigQueryTable =
                    BigQueryTable.newBuilder()
                            .setProjectId(projectId)
                            .setDatasetId(datasetId)
                            .setTableId(tableId)
                            .build();


            // Tag each of the quasiId column names with its corresponding infoType
            List<InfoType> infoTypes =
                    infoTypeNames.stream()
                            .map(it -> InfoType.newBuilder().setName(it).build())
                            .collect(Collectors.toList());

            Iterator<String> quasiIdsIterator = quasiIds.iterator();
            Iterator<InfoType> infoTypesIterator = infoTypes.iterator();

            if (quasiIds.size() != infoTypes.size()) {
                throw new IllegalArgumentException("The numbers of quasi-IDs and infoTypes must be equal!");
            }

            ArrayList<TaggedField> taggedFields = new ArrayList();
            while (quasiIdsIterator.hasNext() || infoTypesIterator.hasNext()) {
                taggedFields.add(
                        TaggedField.newBuilder()
                                .setField(FieldId.newBuilder().setName(quasiIdsIterator.next()).build())
                                .setInfoType(infoTypesIterator.next())
                                .build());
            }

            // The k-map distribution region can be specified by any ISO-3166-1 region code.
            String regionCode = "US";


            // Configure the privacy metric for the job
            KMapEstimationConfig kmapConfig =
                    KMapEstimationConfig.newBuilder()
                            .addAllQuasiIds(taggedFields)
                            .setRegionCode(regionCode)
                            .build();
            PrivacyMetric privacyMetric =
                    PrivacyMetric.newBuilder().setKMapEstimationConfig(kmapConfig).build();


            // Create action to publish job status notifications over Google Cloud Pub/Sub
            ProjectTopicName topicName = ProjectTopicName.of(projectId, topicId);
            PublishToPubSub publishToPubSub =
                    PublishToPubSub.newBuilder()
                            .setTopic(topicName.toString())
                            .build();
            Action action = Action.newBuilder().setPubSub(publishToPubSub).build();

            // Configure the risk analysis job to perform
            RiskAnalysisJobConfig riskAnalysisJobConfig =
                    RiskAnalysisJobConfig.newBuilder()
                            .setSourceTable(bigQueryTable)
                            .setPrivacyMetric(privacyMetric)
                            .addActions(action)
                            .build();

            // Build the request to be sent by the client
            CreateDlpJobRequest createDlpJobRequest =
                    CreateDlpJobRequest.newBuilder()
                            .setParent(ProjectName.of(projectId).toString())
                            .setRiskJob(riskAnalysisJobConfig)
                            .build();

            // Send the request to the API using the client
            DlpJob dlpJob = dlpServiceClient.createDlpJob(createDlpJobRequest);

            // Set up a Pub/Sub subscriber to listen on the job completion status
            final SettableApiFuture<Boolean> done = SettableApiFuture.create();

            ProjectSubscriptionName subscriptionName =
                    ProjectSubscriptionName.of(projectId, subscriptionId);

            MessageReceiver handleMessage =
                    (PubsubMessage pubsubMessage, AckReplyConsumer ackReplyConsumer) -> {
                        String messageAttribute = pubsubMessage.getAttributesMap().get("DlpJobName");
                        if (dlpJob.getName().equals(messageAttribute)) {
                            done.set(true);
                            ackReplyConsumer.ack();
                        } else {
                            ackReplyConsumer.nack();
                        }
                    };
            Subscriber subscriber = Subscriber.newBuilder(subscriptionName, handleMessage).build();
            subscriber.startAsync();

            // Wait for job completion semi-synchronously
            // For long jobs, consider using a truly asynchronous execution model such as Cloud Functions
            try {
                done.get(1, TimeUnit.MINUTES);
                Thread.sleep(500); // Wait for the job to become available
            } catch (TimeoutException e) {
                System.out.println("Unable to verify job completion.");
            }

            // Retrieve completed job status
            DlpJob completedJob =
                    dlpServiceClient.getDlpJob(
                            GetDlpJobRequest.newBuilder()
                                    .setName(dlpJob.getName())
                                    .build());
            System.out.println("Job status: " + completedJob.getState());

            // Get the result and parse through and process the information
            KMapEstimationResult kmapResult  = completedJob.getRiskDetails().getKMapEstimationResult();

            for (KMapEstimationHistogramBucket result : kmapResult.getKMapEstimationHistogramList()) {

                System.out.printf(
                        "\tAnonymity range: [%d, %d]\n", result.getMinAnonymity(), result.getMaxAnonymity());
                System.out.printf("\tSize: %d\n", result.getBucketSize());

                for (KMapEstimationQuasiIdValues valueBucket : result.getBucketValuesList()) {
                    String quasiIdValues =
                            valueBucket
                                    .getQuasiIdsValuesList()
                                    .stream()
                                    .map(
                                            v -> {
                                                String s = v.toString();
                                                return s.substring(s.indexOf(':') + 1).trim();
                                            })
                                    .collect(Collectors.joining(", "));

                    System.out.printf("\tValues: {%s}\n", quasiIdValues);
                    System.out.printf(
                            "\tEstimated k-map anonymity: %d\n", valueBucket.getEstimatedAnonymity());
                }
            }
        }
    }
}

// [END dlp_k_map]