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

import com.example.pubsubcbt.RowDetails;
import java.util.ArrayList;
import java.util.List;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

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

  /**
   * @param row the row result read from Cloud Bigtable.
   * @param duration the duration in milliseconds to go back in time for.
   * @return the last transaction details in the last "duration" for this
   * customer.
   */
  public ArrayList<TransactionDetails> getLastTransactions(
      final Result row, final long duration)
      throws IllegalAccessException {
    ArrayList<TransactionDetails> lastTransactions = new ArrayList<>();
    String[] headers = getHeaders();

    // Create ArrayList that will hold the cells when we read from CBT and
    // ignore the first element because it will hold the rowKey, and we
    // already know the rowKey (userID).
    ArrayList<List<Cell>> cells = new ArrayList<>();
    cells.add(null);
    for (int i = 0; i < headers.length; i++) {
      if (i != 0) {
        cells.add(row.getColumnCells(Bytes.toBytes(getColFamily()),
            Bytes.toBytes(headers[i])));
        if (cells.get(i).size() == 0) {
          return lastTransactions;
        }
      }
    }

    // Iterate over all the transactions of that user that fit in the
    // timeMilliseconds range.
    // If we find a transaction that was declared as fraudulent,
    // we should ignore it.
    int transactionIteration = 0;
    boolean hasCells = true;
    while (hasCells) {
      // Build a historical transaction.
      ArrayList<String> historicalTransactionBuilder = new ArrayList<>();
      historicalTransactionBuilder.add(customerID);
      long transactionTimeMillisecond = -1;
      for (int header = 1; header < headers.length; header++) {
        // Populate the historical transaction.
        List<Cell> currentCells = cells.get(header);
        // Stop if we iterated over all transactions for that customer.
        if (currentCells.size() == transactionIteration + 1) {
          hasCells = false;
        }
        historicalTransactionBuilder.add(
            new String(currentCells.get(transactionIteration).getValueArray()));
        transactionTimeMillisecond = currentCells.get(transactionIteration)
            .getTimestamp();
      }

      // Create the historical transaction.
      TransactionDetails historicalTransaction =
          new TransactionDetails(
              UtilFunctions.arrayListToCommasString(
                  historicalTransactionBuilder));

      // Populate the historical transaction timestamp.
      if (transactionTimeMillisecond != -1) {
        historicalTransaction.setTimestampMillisecond(
            transactionTimeMillisecond);
      }

      // If we reach transactions from an older timestamp than what we want,
      // stop.
      if (historicalTransaction.getTimestampMillisecond() < duration) {
        break;
      }

      // If the transaction was legit, add it to the list of historical
      // transactions.
      if (!historicalTransaction.isFraud()) {
        lastTransactions.add(historicalTransaction);
      }

      // Go to the next historical transaction.
      transactionIteration++;
    }
    return lastTransactions;
  }

  @Override
  public String toPubsub() {
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
