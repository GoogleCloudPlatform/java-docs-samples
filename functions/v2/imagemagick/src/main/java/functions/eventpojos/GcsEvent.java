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

package functions.eventpojos;

import java.io.Serializable;
import java.util.Date;

// [START functions_helloworld_gcs_event]
public class GcsEvent implements Serializable {
  // Cloud Functions uses GSON to populate this object.
  // Field types/names are specified by Cloud Functions
  // Changing them may break your code!
  private String bucket;
  private String name;
  private String metageneration;
  private Date timeCreated;
  private Date updated;

  public String getBucket() {
    return bucket;
  }

  public void setBucket(String bucket) {
    this.bucket = bucket;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getMetageneration() {
    return metageneration;
  }

  public void setMetageneration(String metageneration) {
    this.metageneration = metageneration;
  }

  public Date getTimeCreated() {
    return timeCreated;
  }

  public void setTimeCreated(Date timeCreated) {
    this.timeCreated = timeCreated;
  }

  public Date getUpdated() {
    return updated;
  }

  public void setUpdated(Date updated) {
    this.updated = updated;
  }
}
// [END functions_helloworld_gcs_event]
