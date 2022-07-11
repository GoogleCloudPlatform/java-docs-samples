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

import java.util.ArrayList;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.apache.hadoop.hbase.client.Result;

@DefaultCoder(AvroCoder.class)
public class AggregatedData {

  public TransactionDetails transactionDetails;
  public CustomerDemographics customerDemographics;
  public double lastTransactionMinutesDiff;
  public double lastTransactionKMsDiff;
  public double avgAmountSpentLastWeek;
  public double avgAmountSpentLastMonth;
  public double numOfTransactionLastDay;

  // Given CustomerDemographics, CurrentTransactionDetails, and a CBT row, generate an Aggregated
  // data object.
  public AggregatedData(
      CustomerDemographics customerDemographics, TransactionDetails transactionDetails, Result row)
      throws IllegalAccessException {
    this.customerDemographics = customerDemographics;
    this.transactionDetails = transactionDetails;

    // Get last transaction.
    TransactionDetails lastTransaction = new TransactionDetails(row);
    String lastTransactionLat = lastTransaction.merchantLat;
    String lastTransactionLong = lastTransaction.merchantLong;
    long lastTransactionTime = lastTransaction.getTimestampMillisecond();

    // Get all transactions made by this customer in the last month.
    ArrayList<TransactionDetails> lastMonthTransactions =
        transactionDetails.GetLastTransactions(
            row, lastTransactionTime - UtilFunctions.MONTH_IN_MILLISECONDS);

    // Generate the required aggregated data.
    long lastDayTransactionsCount = 0;
    long lastWeekTransactionsCount = 0;
    long lastMonthTransactionsCount = 0;
    long lastWeekTransactionsAmount = 0;
    long lastMonthTransactionsAmount = 0;

    for (TransactionDetails transaction : lastMonthTransactions) {
      if (lastTransaction.getTimestampMillisecond() - transaction.getTimestampMillisecond()
          <= UtilFunctions.DAY_IN_MILLISECONDS) {
        lastDayTransactionsCount++;
      }
      if (lastTransaction.getTimestampMillisecond() - transaction.getTimestampMillisecond()
          <= UtilFunctions.WEEK_IN_MILLISECONDS) {
        lastWeekTransactionsCount++;
        lastWeekTransactionsAmount += Double.parseDouble(transaction.transactionAmount);
      }
      if (lastTransaction.getTimestampMillisecond() - transaction.getTimestampMillisecond()
          <= UtilFunctions.MONTH_IN_MILLISECONDS) {
        lastMonthTransactionsCount++;
        lastMonthTransactionsAmount += Double.parseDouble(transaction.transactionAmount);
      }
    }

    // Calculate the time between this transaction and the last transaction made by the customer.
    this.lastTransactionMinutesDiff =
        (transactionDetails.getTimestampMillisecond() - lastTransactionTime) / 1000.0 / 60.0;

    // Calculate the distance between this transaction and the last transaction made by the customer.
    this.lastTransactionKMsDiff =
        UtilFunctions.distanceKM(
            Double.parseDouble(transactionDetails.merchantLat),
            Double.parseDouble(lastTransactionLat),
            Double.parseDouble(transactionDetails.merchantLong),
            Double.parseDouble(lastTransactionLong));

    // Populate the number of transactions made by the same customer in the last day.
    this.numOfTransactionLastDay = lastDayTransactionsCount;

    // Calculate the average transaction amounts.
    if (lastWeekTransactionsCount != 0) {
      this.avgAmountSpentLastWeek = lastWeekTransactionsAmount / lastWeekTransactionsCount;
    }
    if (lastMonthTransactionsCount != 0) {
      this.avgAmountSpentLastMonth = lastMonthTransactionsAmount / lastMonthTransactionsCount;
    }
  }

  // Generate the ML features vector that the ML model anticipates.
  public String GetMLFeatures() {
    ArrayList<String> mlFeatures = new ArrayList<>();

    mlFeatures.add(String.valueOf(lastTransactionMinutesDiff));
    mlFeatures.add(String.valueOf(lastTransactionKMsDiff));
    mlFeatures.add(String.valueOf(avgAmountSpentLastWeek));
    mlFeatures.add(String.valueOf(avgAmountSpentLastMonth));
    mlFeatures.add(String.valueOf(numOfTransactionLastDay));
    mlFeatures.add(String.valueOf(customerDemographics.id));
    mlFeatures.add(customerDemographics.ccNumber);
    mlFeatures.add(String.valueOf(transactionDetails.transactionAmount));
    mlFeatures.add(String.valueOf(transactionDetails.merchantID));

    // Convert it to the format that the ML model accepts.
    return "[[" + UtilFunctions.arrayListToCommasString(mlFeatures) + "]]";
  }
}
