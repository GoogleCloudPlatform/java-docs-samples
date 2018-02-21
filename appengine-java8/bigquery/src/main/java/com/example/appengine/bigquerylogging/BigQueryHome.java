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

import com.google.cloud.bigquery.FieldValueList;
import com.google.cloud.bigquery.TableResult;

import java.io.IOException;
import java.util.List;

public class BigQueryHome {
  private static BigQueryRunner queryRunner;

  private static BigQueryRunner getQueryRunner() throws IOException {
    if (queryRunner == null) {
      queryRunner = BigQueryRunner.getInstance();
    }
    return queryRunner;
  }

  public static String getMostRecentRun() throws IOException {
    return convertRunToHtmlTable(BigQueryRunner.getMostRecentRunResult());
  }

  public static String getMetricAverages() throws IOException {
    return convertAveragesToHtmlTable(getQueryRunner().getTimeSeriesValues());
  }

  private static String convertRunToHtmlTable(TableResult result) {
    if (result == null) {
      return "";
    }

    StringBuilder sb = new StringBuilder();
    for (FieldValueList row : result.iterateAll()) {
      sb.append("<tr>");
      String url = row.get("url").getStringValue();
      addColumn(sb, String.format("<a href=\"%s\">%s</a>", url, url));
      addColumn(sb, row.get("view_count").getLongValue());
      sb.append("</tr>");
    }
    return sb.toString();
  }

  private static String convertAveragesToHtmlTable(List<TimeSeriesSummary> values) {

    StringBuilder sb = new StringBuilder();
    for (TimeSeriesSummary metric : values) {
      sb.append("<tr>");
      addColumn(sb, metric.getName());
      addColumn(sb, metric.getValues().size());
      addColumn(sb, metric.getMostRecentRunTime());
      addColumn(sb, metric.getMostRecentValue());
      addColumn(sb, metric.getAverage());
      sb.append("</tr>");
    }
    return sb.toString();
  }

  private static <T> void addColumn(StringBuilder sb, T content) {
    sb.append("<td>").append(content.toString()).append("</td>");
  }
}
