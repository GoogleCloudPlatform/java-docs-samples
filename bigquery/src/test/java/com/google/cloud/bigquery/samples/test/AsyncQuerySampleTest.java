package com.google.cloud.bigquery.samples.test;

import com.google.api.services.bigquery.model.GetQueryResultsResponse;
import com.google.cloud.bigquery.samples.AsyncQuerySample;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import org.junit.*;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

public class AsyncQuerySampleTest extends BigquerySampleTest{

  /**
   * @throws JsonSyntaxException
   * @throws JsonIOException
   * @throws FileNotFoundException
   */
  public AsyncQuerySampleTest() throws JsonSyntaxException, JsonIOException,
      FileNotFoundException {
    super();
    // TODO(elibixby): Auto-generated constructor stub
  }


  @Test
  public void testInteractive() throws IOException, InterruptedException{
    Iterator<GetQueryResultsResponse> pages = AsyncQuerySample.run(CONSTANTS.getProjectId(), CONSTANTS.getQuery(), false, 5000);
    while(pages.hasNext()){
      assertTrue(!pages.next().getRows().isEmpty());
    }
  }
  
  
  @Test
  @Ignore // Batches can take up to 3 hours to run, probably shouldn't use this
  public void testBatch() throws IOException, InterruptedException{
    Iterator<GetQueryResultsResponse> pages = AsyncQuerySample.run(CONSTANTS.getProjectId(), CONSTANTS.getQuery(), true, 5000);
    while(pages.hasNext()){
      assertTrue(!pages.next().getRows().isEmpty());
    }
  }
  
  
}
