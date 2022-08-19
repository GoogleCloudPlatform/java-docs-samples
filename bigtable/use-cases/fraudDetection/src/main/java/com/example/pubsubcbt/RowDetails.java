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
package com.example.pubsubcbt;

import com.example.util.UtilFunctions;
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

/*
 * Facilitates reading/writing from/to Cloud Bigtable. Classes that extend this
 * class can easily construct their objects by reading a comma-separated line,
 * or reading from a Cloud Bigtable instance.
 * Used to create an object with the following format:
 * rowKey, [TimestampMilliseconds=VALUE], column1, column2, ... etc
 * The TimestampMilliseconds field is optional.
 * In order to use this class, member variables needs to be the same name as
 * the columns read from Cloud Bigtable, and they should be in the same
 * sequence when read from Cloud Pubsub.
 */
public abstract class RowDetails {

  /**
   * The timestamp (millisecond) to use when writing to Cloud Bigtable.
   */
  private long timestampMillisecond;

  /**
   * Constructs a RowDetails Object.
   */
  public RowDetails() {
    timestampMillisecond = Long.MAX_VALUE;
  }

  /**
   * Constructs an object by reading a line from Pubsub. Supports writing a
   * custom timestamp.
   *
   * @param line a comma-seperated line used to build a RowDetails object.
   */
  public RowDetails(final String line) throws IllegalAccessException {
    timestampMillisecond = Long.MAX_VALUE;
    List<String> values = new LinkedList<>(Arrays.asList(line.split(", ")));

    // If a custom timestamp is to be used, it should be the second field
    // and in this format: TimestampMilliseconds=TIME
    if (values.size() >= 2 && values.get(1)
        .contains("TimestampMilliseconds=")) {
      long timestampMilliseconds = Long.parseLong(
          values.get(1).split("TimestampMilliseconds=")[1]);
      values.remove(1);
      setTimestampMillisecond(timestampMilliseconds);
    }

    // Convert the values to a string array and populate all the class fields.
    String[] processedValues = new String[values.size()];
    processedValues = values.toArray(processedValues);
    setValues(processedValues);
  }

  /**
   * Constructs an object by reading all the necessary columns for a
   * specific row.
   *
   * @param row a row result read from Cloud Bigtable
   */
  public RowDetails(final Result row) throws IllegalAccessException {
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
        Cell latestCell = row.getColumnLatestCell(columnFamilyBytes,
            headerBytes);
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

  /**
   * @return the headers used in this class.
   */
  public String[] getHeaders() {
    Field[] fields = getClass().getDeclaredFields();
    String[] headers = new String[fields.length];
    for (int i = 0; i < fields.length; i++) {
      fields[i].setAccessible(true);
      headers[i] = fields[i].getName();
    }
    return headers;
  }

  /**
   * @return the values used in this class.
   */
  public String[] getValues() throws IllegalAccessException {
    Field[] fields = getClass().getDeclaredFields();
    String[] values = new String[fields.length];
    for (int i = 0; i < fields.length; i++) {
      fields[i].setAccessible(true);
      values[i] = String.valueOf(fields[i].get(this));
    }
    return values;
  }

  /**
   * Sets the values into the class member variables.
   * @param values the values to use when initializing the member variables.
   */
  public void setValues(final String[] values)
      throws IllegalAccessException, RuntimeException {
    Field[] fields = getClass().getDeclaredFields();
    if (fields.length != values.length) {
      throw new RuntimeException(
          "Trying to set values that doesn't equal to the number of fields");
    }
    for (int i = 0; i < fields.length; i++) {
      fields[i].setAccessible(true);
      fields[i].set(this, values[i]);
    }
  }

  /**
   * @return the timestampMillisecond.
   */
  public long getTimestampMillisecond() {
    return timestampMillisecond;
  }

  /**
   * @param time in millisecond to use when writing to Cloud Bigtable.
   */
  public void setTimestampMillisecond(final long time) {
    this.timestampMillisecond = time;
  }

  /**
   * @return a comma separated string.
   */
  public String toCommaSeparatedString() throws IllegalAccessException {
    ArrayList<String> values =
        new ArrayList<>(Arrays.asList(getValues()));
    return UtilFunctions.arrayListToCommasString(values);
  }

  /**
   * @return the column family used in this class.
   */
  public abstract String getColFamily();
}
