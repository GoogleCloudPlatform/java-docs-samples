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

public class CreateBackupMessage {
  private String projectId;
  private String instanceId;
  private String tableId;
  private String clusterId;
  private int expireHours;

  public String getProjectId() {
    return projectId;
  }

  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getTableId() {
    return tableId;
  }

  public void setTableId(String tableId) {
    this.tableId = tableId;
  }

  public String getClusterId() {
    return clusterId;
  }

  public void setClusterId(String clusterId) {
    this.clusterId = clusterId;
  }

  public int getExpireHours() {
    return expireHours;
  }

  public void setExpireHours(int expireHours) {
    this.expireHours = expireHours;
  }

  @Override
  public String toString() {
    return "CreateBackupMessage [projectId=" + projectId + ", instanceId=" + instanceId
        + ", tableId=" + tableId + ", clusterId=" + clusterId
        + ", expireHours=" + expireHours + "]";
  }
}
