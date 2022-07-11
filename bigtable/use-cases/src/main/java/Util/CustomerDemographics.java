/*
 * Copyright 2022 Google LLC
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
package Util;

import PubsubCBTHelper.RowDetails;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.apache.hadoop.hbase.client.Result;

@DefaultCoder(AvroCoder.class)
public class CustomerDemographics extends RowDetails {

  // The naming and the order match the line read from and written to pubsub.
  public String id;
  public String ccNumber;
  public String firstName;
  public String lastName;
  public String dob;
  public String accountNumber;

  public CustomerDemographics(String input) throws IllegalAccessException {
    super(input);
  }

  public CustomerDemographics(Result input) throws IllegalAccessException {
    super(input);
  }

  @Override
  public String getColFamily() {
    return "demographics";
  }
}
