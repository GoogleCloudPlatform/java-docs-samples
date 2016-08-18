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

import static com.google.common.truth.Truth.assertThat;

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
    Date date = TransferJobUtils.createDate("2000-12-30");
    assertThat(date).isEqualTo(new Date().setYear(2000).setMonth(12).setDay(30));

    date = TransferJobUtils.createDate("2016-09-08");
    assertThat(date).isEqualTo(new Date().setYear(2016).setMonth(9).setDay(8));
  }

  /**
   * Tests whether createTimeOfDay() builds the correct time from a formatted String.
   */
  @Test
  public void testCreateTimeOfDay() throws Exception {
    TimeOfDay time = TransferJobUtils.createTimeOfDay("17:00:42");
    assertThat(time).isEqualTo(new TimeOfDay().setHours(17).setMinutes(0).setSeconds(42));

    time = TransferJobUtils.createTimeOfDay("08:09:08");
    assertThat(time).isEqualTo(new TimeOfDay().setHours(8).setMinutes(9).setSeconds(8));
  }
}
