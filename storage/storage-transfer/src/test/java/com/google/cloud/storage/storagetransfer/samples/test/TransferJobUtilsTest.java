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

import com.google.api.services.storagetransfer.model.Date;
import com.google.api.services.storagetransfer.model.TimeOfDay;
import com.google.cloud.storage.storagetransfer.samples.TransferJobUtils;

import org.junit.Test;

import java.util.Random;

public class TransferJobUtilsTest {
  private Random rand = new Random();

  /**
   * Tests whether createDate() builds the correct date from a formatted String.
   */
  @Test
  public void testCreateDate() throws Exception {
    int year = rand.nextInt(2000) + 1;
    int month = rand.nextInt(12) + 1;
    int day = rand.nextInt(30) + 1;
    String dateString =
        Integer.toString(year) + "-" + Integer.toString(month) + "-" + Integer.toString(day);

    Date date = TransferJobUtils.createDate(dateString);

    assertEquals(date, Date.class.newInstance().setYear(year).setMonth(month).setDay(day));
  }

  /**
   * Tests whether createTimeOfDay() builds the correct time from a formatted String.
   */
  @Test
  public void testCreateTimeOfDay() throws Exception {
    int hour = rand.nextInt(24);
    int minute = rand.nextInt(60);
    int second = rand.nextInt(60);
    String timeString =
        Integer.toString(hour) + ":" + Integer.toString(minute) + ":" + Integer.toString(second);

    TimeOfDay time = TransferJobUtils.createTimeOfDay(timeString);

    assertEquals(time,
        TimeOfDay.class.newInstance().setHours(hour).setMinutes(minute).setSeconds(second));
  }
}
