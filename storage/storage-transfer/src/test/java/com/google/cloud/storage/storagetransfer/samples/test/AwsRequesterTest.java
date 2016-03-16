/*
 * Copyright 2015 Google Inc. All Rights Reserved.
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.google.api.services.storagetransfer.Storagetransfer;
import com.google.api.services.storagetransfer.Storagetransfer.TransferJobs;
import com.google.api.services.storagetransfer.Storagetransfer.TransferJobs.Create;
import com.google.api.services.storagetransfer.model.Date;
import com.google.api.services.storagetransfer.model.Schedule;
import com.google.api.services.storagetransfer.model.TimeOfDay;
import com.google.api.services.storagetransfer.model.TransferJob;
import com.google.api.services.storagetransfer.model.TransferSpec;
import com.google.cloud.storage.storagetransfer.samples.AwsRequester;
import com.google.cloud.storage.storagetransfer.samples.TransferClientCreator;
import com.google.cloud.storage.storagetransfer.samples.TransferJobUtils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ TransferJobUtils.class, TransferClientCreator.class })
public class AwsRequesterTest {

  /**
   * Tests whether AwsRequester executes a request to create a TransferJob.
   */
  @Test
  public void testTest() throws Exception {
    Date date = TransferJobUtils.createDate("2000-1-1");
    TimeOfDay time = TransferJobUtils.createTimeOfDay("1:1:1");
    TransferJob dummyJob = TransferJob.class
        .newInstance()
        .setDescription("DUMMY DESCRIPTION")
        .setProjectId("DUMMY_PROJECT_ID")
        .setTransferSpec(TransferSpec.class.newInstance())
        .setSchedule(
            Schedule.class.newInstance().setScheduleStartDate(date).setScheduleEndDate(date)
                .setStartTimeOfDay(time)).setStatus("ENABLED");

    PowerMockito.mockStatic(TransferClientCreator.class);
    PowerMockito.mockStatic(TransferJobUtils.class);
    Storagetransfer client = Mockito.mock(Storagetransfer.class);
    TransferJobs jobs = Mockito.mock(TransferJobs.class);
    Create create = Mockito.mock(Create.class);

    PowerMockito.when(TransferClientCreator.createStorageTransferClient()).thenReturn(client);
    when(client.transferJobs()).thenReturn(jobs);
    when(jobs.create(Matchers.any(TransferJob.class))).thenReturn(create);
    when(create.execute()).thenReturn(dummyJob);

    TransferJob returnedJob = AwsRequester.createAwsTransferJob();

    assertEquals(returnedJob, dummyJob);
  }
}
