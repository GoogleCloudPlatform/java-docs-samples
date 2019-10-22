/*
 * Copyright 2018 Google Inc.
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

package com.example.appengine.bigquerylogging;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.MetricDescriptor;
import com.google.api.gax.rpc.UnaryCallable;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.monitoring.v3.MetricServiceClient;
import com.google.cloud.monitoring.v3.MetricServiceClient.ListMetricDescriptorsPagedResponse;
import com.google.cloud.monitoring.v3.stub.MetricServiceStub;
import com.google.monitoring.v3.CreateMetricDescriptorRequest;
import com.google.monitoring.v3.CreateTimeSeriesRequest;
import com.google.monitoring.v3.ListMetricDescriptorsRequest;
import com.google.protobuf.Empty;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for simple app sample.
 */
@RunWith(JUnit4.class)
public class BigQueryRunnerTest {
  private ByteArrayOutputStream bout;
  private BigQueryRunner app;

  @Mock
  private MetricServiceStub metricsServiceStub;
  @Mock
  private UnaryCallable<ListMetricDescriptorsRequest, ListMetricDescriptorsPagedResponse>
      listCallable;
  @Mock
  private UnaryCallable<CreateMetricDescriptorRequest, MetricDescriptor> createMetricCallable;
  @Mock
  private UnaryCallable<CreateTimeSeriesRequest, Empty> createTimeSeriesCallable;
  @Mock
  private ListMetricDescriptorsPagedResponse listResponse;

  @Captor
  private ArgumentCaptor<CreateTimeSeriesRequest> createTimeSeriesRequest;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    bout = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(bout);

    MetricServiceClient metricsClient = MetricServiceClient.create(metricsServiceStub);
    app = new BigQueryRunner(metricsClient, BigQueryOptions.getDefaultInstance().getService(), out);

    when(metricsServiceStub.listMetricDescriptorsPagedCallable()).thenReturn(listCallable);
    when(listCallable.call(any(ListMetricDescriptorsRequest.class))).thenReturn(listResponse);
    when(listResponse.iterateAll()).thenReturn(Collections.EMPTY_LIST);

    when(metricsServiceStub.createMetricDescriptorCallable()).thenReturn(createMetricCallable);
    when(createMetricCallable.call(any(CreateMetricDescriptorRequest.class))).thenReturn(null);

    when(metricsServiceStub.createTimeSeriesCallable()).thenReturn(createTimeSeriesCallable);
    when(createTimeSeriesCallable.call(any(CreateTimeSeriesRequest.class)))
        .thenReturn(Empty.getDefaultInstance());
  }

  @Test
  public void testRun() throws Exception {
    app.runQuery();
    String got = bout.toString();
    assertThat(got).contains("Done writing metrics.");
    verify(metricsServiceStub).listMetricDescriptorsPagedCallable();

    verify(metricsServiceStub, times(2)).createMetricDescriptorCallable();

    verify(metricsServiceStub).createTimeSeriesCallable();
    verify(createTimeSeriesCallable).call(createTimeSeriesRequest.capture());
    CreateTimeSeriesRequest actual = createTimeSeriesRequest.getValue();
    assertEquals(2, actual.getTimeSeriesCount());
    assertThat(actual.getTimeSeries(0).getMetric().getType()).isEqualTo(
        "custom.googleapis.com/queryDuration");
    assertThat(actual.getTimeSeries(0).getPoints(0).getValue().getInt64Value()).isGreaterThan(0L);
    assertThat(actual.getTimeSeries(1).getMetric().getType()).isEqualTo(
        "custom.googleapis.com/rowsReturned");
    assertThat(actual.getTimeSeries(1).getPoints(0).getValue().getInt64Value()).isGreaterThan(0L);
  }
}
