/*
 * Copyright (c) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.cloud.bigquery.samples;


import com.google.api.services.bigquery.Bigquery;
import com.google.api.services.bigquery.Bigquery.Jobs.GetQueryResults;
import com.google.api.services.bigquery.model.GetQueryResultsResponse;
import com.google.api.services.bigquery.model.Job;
import com.google.api.services.bigquery.model.JobConfiguration;
import com.google.api.services.bigquery.model.JobConfigurationQuery;

import java.io.IOException;
import java.util.Iterator;
import java.util.Scanner;


/**
 * Example of authorizing with BigQuery and reading from a public dataset.
 */
public class AsyncQuerySample extends BigqueryUtils{

  
  // [START main]
  /**
   * @param args
   * @throws IOException
   * @throws InterruptedException
   */
  public static void main(String[] args) 
      throws IOException, InterruptedException {

    Scanner scanner = new Scanner(System.in);
    System.out.println("Enter your project id: ");
    String projectId = scanner.nextLine();
    System.out.println("Enter your query string: ");
    String queryString = scanner.nextLine();
    System.out.println("Run query in batch mode? [true|false] ");
    boolean batch = Boolean.valueOf(scanner.nextLine());
    System.out.println("Enter how often to check if your job is complete (milliseconds): ");
    long waitTime = scanner.nextLong();
    scanner.close();
    Iterator<GetQueryResultsResponse> pages = run(projectId, queryString, batch, waitTime);
    while(pages.hasNext()){
      printRows(pages.next().getRows(), System.out);
    }

  }
  // [END main]

  // [START run]
  public static Iterator<GetQueryResultsResponse> run(String projectId,
      String queryString,
      boolean batch, 
      long waitTime) 
      throws IOException, InterruptedException{
    
    Bigquery bigquery = BigqueryServiceFactory.getService();

    Job query = asyncQuery(bigquery, projectId, queryString, batch);
    Bigquery.Jobs.Get getRequest = bigquery.jobs().get(
        projectId, query.getJobReference().getJobId());
    
    //Poll every waitTime milliseconds, 
    //retrying at most retries times if there are errors
    pollJob(getRequest, waitTime);

    GetQueryResults resultsRequest = bigquery.jobs().getQueryResults(
        projectId, query.getJobReference().getJobId());
    
     return getPages(resultsRequest);
  }
  // [END run]
  
  // [START asyncQuery]
  /**
   * Inserts an asynchronous query Job for a particular query
   *
   * @param bigquery  an authorized BigQuery client
   * @param projectId a String containing the project ID
   * @param querySql  the actual query string
   * @return a reference to the inserted query job
   * @throws IOException
   */
  public static Job asyncQuery(Bigquery bigquery, 
      String projectId,
      String querySql,
      boolean batch) throws IOException {
    
    JobConfigurationQuery query_config = new JobConfigurationQuery()
          .setQuery(querySql);
    
    if(batch){
      query_config.setPriority("BATCH");
    }
    
    Job job = new Job().setConfiguration(
        new JobConfiguration().setQuery(query_config));
            
    return bigquery.jobs().insert(projectId, job).execute();
  }
  // [END asyncQuery]

}
