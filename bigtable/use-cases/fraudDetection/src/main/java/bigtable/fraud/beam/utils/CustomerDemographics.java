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
package bigtable.fraud.beam.utils;

import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.apache.hadoop.hbase.client.Result;

@DefaultCoder(AvroCoder.class)
public final class CustomerDemographics extends RowDetails {

  /**
   * The incoming request's customer id.
   */
  private String id;
  /**
   * The incoming request's customer credit card number.
   */
  private String ccNumber;
  /**
   * The incoming request's customer first name.
   */
  private String firstName;
  /**
   * The incoming request's customer last name.
   */
  private String lastName;
  /**
   * The incoming request's date of birth.
   */
  private String dob;
  /**
   * The incoming request's account number.
   */
  private String accountNumber;

  /**
   * Constructs CustomerDemographics object.
   *
   * @param line a CustomerDemographics comma-seperated line
   */
  public CustomerDemographics(final String line) {
    super(line);
  }

  /**
   * Constructs CustomerDemographics object.
   *
   * @param row a row result read from Cloud Bigtable.
   */
  public CustomerDemographics(final Result row) {
    super(row);
  }

  /**
   * @return customer id.
   */
  public String getId() {
    return id;
  }

  /**
   * @return customer credit card number.
   */
  public String getCcNumber() {
    return ccNumber;
  }

  @Override
  public String getColFamily() {
    return "demographics";
  }
}
