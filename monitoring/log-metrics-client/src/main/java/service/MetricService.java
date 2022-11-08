package service;

import com.google.api.LabelDescriptor;
import com.google.api.MetricDescriptor;
import com.google.api.MonitoredResourceDescriptor;
import com.google.cloud.logging.v2.MetricsClient;
import com.google.cloud.monitoring.v3.MetricServiceClient;
import com.google.cloud.monitoring.v3.MetricServiceClient.ListMonitoredResourceDescriptorsPagedResponse;
import com.google.gson.Gson;
import com.google.logging.v2.CreateLogMetricRequest;
import com.google.logging.v2.ListLogMetricsRequest;
import com.google.logging.v2.LogMetric;
import com.google.monitoring.v3.*;

import java.io.IOException;
import java.util.List;

public class MetricService {

    private final String metricDomain;
    private final String projectId;

    public MetricService(String metricDomain, String projectId) {
        this.metricDomain = metricDomain;
        this.projectId = projectId;
    }

    public void listLogMetrics() throws IOException {
        // [START list-log-metrics]
        ProjectName name = ProjectName.of(projectId);

        MetricsClient client = MetricsClient.create();

        ListLogMetricsRequest request = ListLogMetricsRequest.newBuilder()
                .setParent(name.toString())
                .build();

        MetricsClient.ListLogMetricsPagedResponse response = client.listLogMetrics(request);

        System.out.println("Listing Log Metrics: ");

        for (LogMetric d : response.iterateAll()) {
            System.out.println(d.getName() + " " + d.getDescription());
        }
        client.close();
        // [END list-log-metrics]
    }

    public void createLogMetrics(String name, String description, String filter) throws IOException {

        ProjectName name = ProjectName.of(projectId);

        MetricsClient client = MetricsClient.create();

        LogMetric inputMetric = LogMetric.newBuilder()
                .setName(name)
                .setDescription(description)
                .setFilter(filter)
                .build();

        CreateLogMetricRequest request = CreateLogMetricRequest.newBuilder()
                .setParent(name.toString())
                .setMetric(inputMetric)
                .build();

        LogMetric response = client.createLogMetric(request);
        System.out.println("Log Metric successfully created: " + response.getName());

        client.close();
    }

    public void deleteLogMetrics(String logMetricName) throws IOException {

        ProjectName name = ProjectName.of(projectId);

        MetricsClient client = MetricsClient.create();

        String path = String.format("%s/metrics/%s", name, logMetricName);

        client.deleteLogMetric(path);
        System.out.println("Log Metric successfully deleted.");
        client.close();
    }
}