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
import java.util.ArrayList;
import java.util.List;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

@DefaultCoder(AvroCoder.class)
public class TransactionDetails extends RowDetails {

  public String userID;
  public String transactionID;
  public String transactionAmount;
  public String merchantID;
  public String merchantLong;
  public String merchantLat;
  public String isFraud;

  public TransactionDetails(String input) throws IllegalAccessException {
    super(input);
  }

  public TransactionDetails(Result row) throws IllegalAccessException {
    super(row);
  }

  // Returns the last transaction details in the last "timeMilliSeconds" for this customer.
  public ArrayList<TransactionDetails> GetLastTransactions(
      Result row, long timeMilliSeconds) throws IllegalAccessException {
    ArrayList<TransactionDetails> lastTransactions = new ArrayList<>();
    String[] headers = getHeaders();

    // Create ArrayList that will hold the cells when we read from CBT and ignore the first
    // element because it will hold the rowKey, and we already know the rowKey (userID).
    ArrayList<List<Cell>> cells = new ArrayList<>();
    cells.add(null);
    for (int i = 0; i < headers.length; i++) {
      if (i != 0) {
        cells.add(row.getColumnCells(Bytes.toBytes(getColFamily()), Bytes.toBytes(headers[i])));
        if (cells.get(i).size() == 0) {
          return lastTransactions;
        }
      }
    }

    // Iterate over all the transactions of that user that fit in the timeMilliseconds range.
    // If we find a transaction that was declared as fraudulent, we should ignore it.
    int transactionIteration = 0;
    boolean hasCells = true;
    while (hasCells) {
      // id, trans_num, unix_time_seconds, amt, merchant, merch_lat, merch_long, is_fraud
      // Build a historical transaction.
      ArrayList<String> historicalTransactionBuilder = new ArrayList<>(1000);
      historicalTransactionBuilder.add(userID);
      long transactionTimeMillisecond = -1;
      for (int header = 1; header < headers.length; header++) {
        // Populate the historical transaction.
        List<Cell> currentCells = cells.get(header);
        // Stop iterating if we iterated over all transactions for that customer.
        if (currentCells.size() == transactionIteration + 1) {
          hasCells = false;
        }
        historicalTransactionBuilder.add(
            new String(currentCells.get(transactionIteration).getValueArray()));
        transactionTimeMillisecond = currentCells.get(transactionIteration).getTimestamp();
      }

      // Create the historical transaction.
      TransactionDetails historicalTransaction =
          new TransactionDetails(
              UtilFunctions.arrayListToCommasString(historicalTransactionBuilder));

      // Populate the historical transaction timestamp.
      if (transactionTimeMillisecond != -1) {
        historicalTransaction.setTimestampMillisecond(transactionTimeMillisecond);
      }

      // If we reach transactions from an older timestamp than what we want, stop.
      if (historicalTransaction.getTimestampMillisecond() < timeMilliSeconds) {
        break;
      }

      // If the transaction was legit, add it to the list of historical transactions.
      if (!historicalTransaction.isFraud()) {
        lastTransactions.add(historicalTransaction);
      }

      // Go to the next historical transaction.
      transactionIteration++;
    }
    return lastTransactions;
  }

  @Override
  public String toPubsub() throws IllegalAccessException {
    return "Transaction id: " + transactionID + ", isFraud: " + isFraud;
  }

  @Override
  public String getColFamily() {
    return "history";
  }

  public boolean isFraud() {
    return this.isFraud.equals("1");
  }
}
