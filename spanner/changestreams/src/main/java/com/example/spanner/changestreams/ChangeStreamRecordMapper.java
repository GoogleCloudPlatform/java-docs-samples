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

package com.example.spanner.changestreams;

import com.example.spanner.changestreams.model.ChangeStreamRecord;
import com.example.spanner.changestreams.model.ChildPartition;
import com.example.spanner.changestreams.model.ChildPartitionsRecord;
import com.example.spanner.changestreams.model.ColumnType;
import com.example.spanner.changestreams.model.DataChangeRecord;
import com.example.spanner.changestreams.model.HeartbeatRecord;
import com.example.spanner.changestreams.model.Mod;
import com.example.spanner.changestreams.model.ModType;
import com.example.spanner.changestreams.model.TypeCode;
import com.example.spanner.changestreams.model.ValueCaptureType;
import com.google.cloud.Timestamp;
import com.google.cloud.spanner.Struct;
import com.google.cloud.spanner.Type;
import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ChangeStreamRecordMapper converts a Struct returned from Change Streams API into a well-defined
 * model, which could be one of DataChangeRecord, ChildPartitionsRecord or HeartbeatRecord.
 */
public class ChangeStreamRecordMapper {

  public List<ChangeStreamRecord> toChangeStreamRecords(Struct row) {
    return row.getStructList(0).stream()
      .flatMap(struct -> toChangeStreamRecord(struct))
      .collect(Collectors.toList());
  }

  private Stream<ChangeStreamRecord> toChangeStreamRecord(Struct row) {
    final Stream<DataChangeRecord> dataChangeRecords =
        row.getStructList("data_change_record").stream()
        .filter(this::isNonNullDataChangeRecord)
        .map(struct -> toDataChangeRecord(struct));

    final Stream<HeartbeatRecord> heartbeatRecords =
        row.getStructList("heartbeat_record").stream()
        .filter(this::isNonNullHeartbeatRecord)
        .map(struct -> toHeartbeatRecord(struct));

    final Stream<ChildPartitionsRecord> childPartitionsRecords =
        row.getStructList("child_partitions_record").stream()
          .filter(this::isNonNullChildPartitionsRecord)
          .map(struct -> toChildPartitionsRecord(struct));

    return Stream.concat(
      Stream.concat(dataChangeRecords, heartbeatRecords), childPartitionsRecords);
  }

  private boolean isNonNullDataChangeRecord(Struct row) {
    return !row.isNull("commit_timestamp");
  }

  private boolean isNonNullHeartbeatRecord(Struct row) {
    return !row.isNull("timestamp");
  }

  private boolean isNonNullChildPartitionsRecord(Struct row) {
    return !row.isNull("start_timestamp");
  }

  private DataChangeRecord toDataChangeRecord(Struct row) {
    final Timestamp commitTimestamp = row.getTimestamp("commit_timestamp");
    return new DataChangeRecord(
      commitTimestamp,
      row.getString("server_transaction_id"),
      row.getBoolean("is_last_record_in_transaction_in_partition"),
      row.getString("record_sequence"),
      row.getString("table_name"),
      row.getStructList("column_types").stream()
        .map(this::columnTypeFrom)
        .collect(Collectors.toList()),
      row.getStructList("mods").stream().map(this::modFrom).collect(Collectors.toList()),
      ModType.valueOf(row.getString("mod_type")),
      ValueCaptureType.valueOf(row.getString("value_capture_type")),
      row.getLong("number_of_records_in_transaction"),
      row.getLong("number_of_partitions_in_transaction"));
  }

  private HeartbeatRecord toHeartbeatRecord(Struct row) {
    final Timestamp timestamp = row.getTimestamp("timestamp");
    return new HeartbeatRecord(timestamp);
  }

  private ChildPartitionsRecord toChildPartitionsRecord(Struct row) {
    final Timestamp startTimestamp = row.getTimestamp("start_timestamp");
    return new ChildPartitionsRecord(
      startTimestamp,
      row.getString("record_sequence"),
      row.getStructList("child_partitions").stream()
        .map(struct -> childPartitionFrom(struct))
        .collect(Collectors.toList()));
  }

  private ColumnType columnTypeFrom(Struct struct) {
    // TODO: Move to type struct.getJson when backend is fully migrated
    final String type = getJsonString(struct, "type");
    return new ColumnType(
      struct.getString("name"),
        new TypeCode(type),
      struct.getBoolean("is_primary_key"),
      struct.getLong("ordinal_position"));
  }

  private Mod modFrom(Struct struct) {
    // TODO: Move to keys struct.getJson when backend is fully migrated
    final String keys = getJsonString(struct, "keys");
    // TODO: Move to oldValues struct.getJson when backend is fully migrated
    final String oldValues =
        struct.isNull("old_values") ? null : getJsonString(struct, "old_values");
    // TODO: Move to newValues struct.getJson when backend is fully migrated
    final String newValues =
        struct.isNull("new_values") ? null : getJsonString(struct, "new_values");
    return new Mod(keys, oldValues, newValues);
  }

  private ChildPartition childPartitionFrom(Struct struct) {
    final HashSet<String> parentTokens =
        Sets.newHashSet(struct.getStringList("parent_partition_tokens"));
    return new ChildPartition(struct.getString("token"), parentTokens);
  }

  // TODO: Remove when backend is fully migrated to JSON.
  private String getJsonString(Struct struct, String columnName) {
    if (struct.getColumnType(columnName).equals(Type.json())) {
      return struct.getJson(columnName);
    } else if (struct.getColumnType(columnName).equals(Type.string())) {
      return struct.getString(columnName);
    } else {
      throw new IllegalArgumentException("Can not extract string from value " + columnName);
    }
  }
}
