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
package PubsubCBTHelper;

import Util.UtilFunctions;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

@DefaultCoder(AvroCoder.class)

// Facilitates reading/writing to/from pubsub and reading/writing to/from Cloud Bigtable.
// Used to create an object with the following format:
// rowKey, [TimestampMilliseconds=VALUE], column1, column2, ... etc
// The TimestampMilliseconds field is optional.
// In order to use this class, member variables needs to be the same name as the columns read
// from Cloud Bigtable, and they should be in the same sequence when read from Cloud Pubsub.
public abstract class RowDetails {

  private long timestampMillisecond;

  public RowDetails() {
    timestampMillisecond = Long.MAX_VALUE;
  }

  // Construct an object by reading a line from Pubsub.
  // Supports writing a custom timestamp.
  public RowDetails(String line) throws IllegalAccessException {
    timestampMillisecond = Long.MAX_VALUE;
    List<String> values = new LinkedList<>(Arrays.asList(line.split(", ")));

    // If a custom timestamp is to be used, it should be the second field and in this format:
    // TimestampMilliseconds=TIME
    if (values.size() >= 2 && values.get(1).contains("TimestampMilliseconds=")) {
      long timestampMilliseconds = Long.parseLong(values.get(1).split("TimestampMilliseconds=")[1]);
      values.remove(1);
      setTimestampMillisecond(timestampMilliseconds);
    }

    // Convert the values to a string array and populate all the class fields.
    String[] processedValues = new String[values.size()];
    processedValues = values.toArray(processedValues);
    setValues(processedValues);
  }

  // Construct an object by reading all the necessary columns for a specific row.
  public RowDetails(Result row) throws IllegalAccessException {
    // All the columns in this object need to belong to the same column family.
    byte[] columnFamilyBytes = getColFamily().getBytes();

    byte[] headerBytes;
    // Get all the headers so that we can read them from CBT.
    String[] headers = getHeaders();
    String[] values = new String[headers.length];
    for (int i = 0; i < headers.length; i++) {
      // Adding the row key.
      if (i == 0) {
        values[i] = new String(row.getRow());
      } else {
        headerBytes = Bytes.toBytes(headers[i]);
        Cell latestCell = row.getColumnLatestCell(columnFamilyBytes, headerBytes);
        if (latestCell == null) {
          return;
        }
        values[i] = new String(latestCell.getValueArray());
        setTimestampMillisecond(latestCell.getTimestamp());
      }
    }
    // Populate all the class fields with the values we read.
    setValues(values);
  }

  // Return all the names of all class member variables in sequence.
  public String[] getHeaders() {
    Field[] fields = getClass().getDeclaredFields();
    String[] headers = new String[fields.length];
    for (int i = 0; i < fields.length; i++) {
      headers[i] = fields[i].getName();
    }
    return headers;
  }

  // Return all the values of all class member variables in sequence.
  public String[] getValues() throws IllegalAccessException {
    Field[] fields = getClass().getDeclaredFields();
    String[] values = new String[fields.length];
    for (int i = 0; i < fields.length; i++) {
      values[i] = String.valueOf(fields[i].get(this));
    }
    return values;
  }

  // Set the values to all class member variables in sequence.
  public void setValues(String[] values) throws IllegalAccessException, RuntimeException {
    Field[] fields = getClass().getDeclaredFields();
    if (fields.length != values.length) {
      throw new RuntimeException("Trying to set values that doesn't equal to the number of fields");
    }
    for (int i = 0; i < fields.length; i++) {
      fields[i].set(this, values[i]);
    }
  }

  // Return the row key.
  public String getRowKey() throws IllegalAccessException {
    String[] values = getValues();
    if (values.length < 1 || values[0] != null) {
      throw new RuntimeException("Trying to get the rowKey it is not set.");
    }
    return values[0];
  }

  public long getTimestampMillisecond() {
    return timestampMillisecond;
  }

  public void setTimestampMillisecond(long timestampMillisecond) {
    this.timestampMillisecond = timestampMillisecond;
  }

  // Convert the current object to a line to write to Cloud Pubsub
  public String toPubsub() throws IllegalAccessException {
    ArrayList<String> values =
        new ArrayList<>(Arrays.asList(getValues()));
    return UtilFunctions.arrayListToCommasString(values);
  }

  // This class only supports reading values from one column family.
  public abstract String getColFamily();
}
