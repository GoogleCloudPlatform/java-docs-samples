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
package com.example.util;

import com.example.pubsubcbt.RowDetails;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.apache.hadoop.hbase.client.Result;

@DefaultCoder(AvroCoder.class)
public final class TransactionDetails extends RowDetails {

  /**
   * The incoming transaction customer id.
   */
  private String customerID;
  /**
   * The incoming transaction customer id.
   */
  private String transactionID;
  /**
   * The incoming transaction id.
   */
  private String transactionAmount;
  /**
   * The incoming transaction merchant id.
   */
  private String merchantID;
  /**
   * The incoming transaction merchant longitude.
   */
  private String merchantLong;
  /**
   * The incoming transaction merchant latitude.
   */
  private String merchantLat;
  /**
   * Is this transaction fraudulent?.
   */
  private String isFraud;

  /**
   * Construct a TransactionDetails object.
   *
   * @param line a comma-seperated TransactionDetails line.
   */
  public TransactionDetails(final String line) throws IllegalAccessException {
    super(line);
  }

  /**
   * Construct a TransactionDetails object.
   *
   * @param row a result row read from Cloud Bigtable.
   */
  public TransactionDetails(final Result row) throws IllegalAccessException {
    super(row);
  }

  /**
   * @return the customer id.
   */
  public String getCustomerID() {
    return customerID;
  }

  /**
   * @return the transaction amount.
   */
  public String getTransactionAmount() {
    return transactionAmount;
  }

  /**
   * @return the merchant id.
   */
  public String getMerchantID() {
    return merchantID;
  }

  /**
   * @return the merchant longitude.
   */
  public String getMerchantLong() {
    return merchantLong;
  }

  /**
   * @return the merchant latitude.
   */
  public String getMerchantLat() {
    return merchantLat;
  }

  /**
   * @param input the isFraud value to set.
   */
  public void setIsFraud(final String input) {
    this.isFraud = input;
  }

  @Override
  public String toCommaSeparatedString() {
    return "Transaction id: " + transactionID + ", isFraud: " + isFraud;
  }

  @Override
  public String getColFamily() {
    return "history";
  }

  /**
   * @return return true if the transaction is fraudulent, false if not.
   */
  public boolean isFraud() {
    return this.isFraud.equals("1");
  }
}
