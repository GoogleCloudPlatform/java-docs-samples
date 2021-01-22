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

package com.example.cloud.bigtable.scheduledbackups.resource;

//CHECKSTYLE OFF: AbbreviationAsWordInName
public class CreateBackupRequestHTTP {
  //CHECKSTYLE ON: AbbreviationAsWordInName
  private String sourceTable;
  private String expireTime;

  public String getSourceTable() {
    return sourceTable;
  }

  public void setSourceTable(String sourceTable) {
    this.sourceTable = sourceTable;
  }

  public String getExpireTime() {
    return expireTime;
  }

  public void setExpireTime(String expireTime) {
    this.expireTime = expireTime;
  }

  @Override
  public String toString() {
    return "CreateBackupRequest [sourceTable=" + sourceTable + ", expireTime=" + expireTime + "]";
  }
}
