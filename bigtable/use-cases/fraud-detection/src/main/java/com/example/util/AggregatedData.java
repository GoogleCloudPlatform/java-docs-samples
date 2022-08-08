/*
 * Copyright 2020 Google LLC
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

import java.util.ArrayList;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.apache.hadoop.hbase.client.Result;

@DefaultCoder(AvroCoder.class)
public final class AggregatedData {

  /**
   * Stores the incoming transaction details.
   */
  private TransactionDetails transactionDetails;
  /**
   * Stores the incoming transaction customer demographics.
   */
  private CustomerDemographics customerDemographics;
  /**
   * Stores the difference between this transaction and the last one in
   * minutes.
   */
  private double lastTransactionMinutesDiff;
  /**
   * Stores the distance between this transaction and the last one in KMs.
   */
  private double lastTransactionKMsDiff;
  /**
   * Stores the average amount spent last week by the customer.
   */
  private double avgAmountSpentLastWeek;
  /**
   * Stores the average amount spent last month by the customer.
   */
  private double avgAmountSpentLastMonth;
  /**
   * Stores the number of transaction created by that customer in the last 24
   * hours.
   */
  private double numOfTransactionLastDay;

  /**
   * Construct an AggregatedData object.
   *
   * @param iCustomerDemographics the incoming customer demographic object.
   * @param iTransactionDetails the incoming transaction details object.
   * @param row a result row read from Cloud Bigtable.
   */
  public AggregatedData(
      final CustomerDemographics iCustomerDemographics,
      final TransactionDetails iTransactionDetails, final Result row)
      throws IllegalAccessException {
    this.customerDemographics = iCustomerDemographics;
    this.transactionDetails = iTransactionDetails;

    // Get last transaction.
    TransactionDetails lastTransaction = new TransactionDetails(row);
    String lastTransactionLat = lastTransaction.getMerchantLat();
    String lastTransactionLong = lastTransaction.getMerchantLong();
    long lastTransactionTime = lastTransaction.getTimestampMillisecond();

    // Get all transactions made by this customer in the last month.
    ArrayList<TransactionDetails> lastMonthTransactions =
        iTransactionDetails.getLastTransactions(
            row, lastTransactionTime
                - UtilFunctions.MONTH_IN_MILLISECONDS);

    // Generate the required aggregated data.
    long lastDayTransactionsCount = 0;
    long lastWeekTransactionsCount = 0;
    long lastMonthTransactionsCount = 0;
    long lastWeekTransactionsAmount = 0;
    long lastMonthTransactionsAmount = 0;

    for (TransactionDetails transaction : lastMonthTransactions) {
      if (lastTransaction.getTimestampMillisecond()
          - transaction.getTimestampMillisecond()
          <= UtilFunctions.DAY_IN_MILLISECONDS) {
        lastDayTransactionsCount++;
      }
      if (lastTransaction.getTimestampMillisecond()
          - transaction.getTimestampMillisecond()
          <= UtilFunctions.WEEK_IN_MILLISECONDS) {
        lastWeekTransactionsCount++;
        lastWeekTransactionsAmount += Double.parseDouble(
            transaction.getTransactionAmount());
      }
      if (lastTransaction.getTimestampMillisecond()
          - transaction.getTimestampMillisecond()
          <= UtilFunctions.MONTH_IN_MILLISECONDS) {
        lastMonthTransactionsCount++;
        lastMonthTransactionsAmount += Double.parseDouble(
            transaction.getTransactionAmount());
      }
    }

    // Calculate the time between this transaction and the last transaction
    // made by the customer.
    this.lastTransactionMinutesDiff =
        (iTransactionDetails.getTimestampMillisecond() - lastTransactionTime)
            / UtilFunctions.MINUTE_IN_MILLISECONDS;

    // Calculate the distance between this transaction and the last transaction
    // made by the customer.
    this.lastTransactionKMsDiff =
        UtilFunctions.distanceKM(
            Double.parseDouble(iTransactionDetails.getMerchantLat()),
            Double.parseDouble(lastTransactionLat),
            Double.parseDouble(iTransactionDetails.getMerchantLong()),
            Double.parseDouble(lastTransactionLong));

    // Populate the number of transactions made by the same customer in the
    // last day.
    this.numOfTransactionLastDay = lastDayTransactionsCount;

    // Calculate the average transaction amounts.
    if (lastWeekTransactionsCount != 0) {
      this.avgAmountSpentLastWeek =
          lastWeekTransactionsAmount / lastWeekTransactionsCount;
    }
    if (lastMonthTransactionsCount != 0) {
      this.avgAmountSpentLastMonth =
          lastMonthTransactionsAmount / lastMonthTransactionsCount;
    }
  }

  /**
   * Return the incoming transaction details object.
   *
   * @return the current transaction details.
   */
  public TransactionDetails getTransactionDetails() {
    return transactionDetails;
  }

  /**
   * Generates the feature vector in the format that is accepted by the machine
   * learning model.
   *
   * @return a feature vector.
   */
  public String getMLFeatures() {
    ArrayList<String> mlFeatures = new ArrayList<>();

    mlFeatures.add(String.valueOf(lastTransactionMinutesDiff));
    mlFeatures.add(String.valueOf(lastTransactionKMsDiff));
    mlFeatures.add(String.valueOf(avgAmountSpentLastWeek));
    mlFeatures.add(String.valueOf(avgAmountSpentLastMonth));
    mlFeatures.add(String.valueOf(numOfTransactionLastDay));
    mlFeatures.add(String.valueOf(customerDemographics.getId()));
    mlFeatures.add(customerDemographics.getCcNumber());
    mlFeatures.add(String.valueOf(transactionDetails.getTransactionAmount()));
    mlFeatures.add(String.valueOf(transactionDetails.getMerchantID()));

    // Convert it to the format that the ML model accepts.
    return "[[" + UtilFunctions.arrayListToCommasString(mlFeatures) + "]]";
  }
}
