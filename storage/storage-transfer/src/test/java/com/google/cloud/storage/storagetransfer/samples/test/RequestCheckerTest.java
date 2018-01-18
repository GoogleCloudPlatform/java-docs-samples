/*
 * Copyright 2015 Google Inc.
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

package com.google.cloud.storage.storagetransfer.samples.test;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.services.storagetransfer.v1.Storagetransfer;
import com.google.api.services.storagetransfer.v1.Storagetransfer.TransferOperations;
import com.google.api.services.storagetransfer.v1.Storagetransfer.TransferOperations.List;
import com.google.cloud.storage.storagetransfer.samples.RequestChecker;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Matchers;
import org.mockito.Mockito;

@RunWith(JUnit4.class)
public class RequestCheckerTest {
  private Storagetransfer mockClient = Mockito.mock(Storagetransfer.class);
  private List mockList = Mockito.mock(List.class);
  private TransferOperations mockOps = Mockito.mock(TransferOperations.class);

  /**
   * Tests whether checkTransfer() makes the API call to list TransferOperations.
   */
  @Test
  public void testCheckTransfer() throws Exception {
    when(mockClient.transferOperations()).thenReturn(mockOps);
    when(mockOps.list(Matchers.anyString())).thenReturn(mockList);
    when(mockList.setFilter(Matchers.anyString())).thenReturn(mockList);

    RequestChecker.checkTransfer(mockClient, "DUMMY_PROJECT_ID", "DUMMY_JOB_NAME");

    verify(mockList).execute();
  }
}
