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

import java.time.OffsetDateTime;
import java.util.Map;

// Represents an object within Cloud Storage
// https://cloud.google.com/storage/docs/json_api/v1/objects
@lombok.Data
public class StorageObjectData {
    private String bucket;
    private String cacheControl;
    private Long componentCount;
    private String contentDisposition;
    private String contentEncoding;
    private String contentLanguage;
    private String contentType;
    private String crc32C;
    private CustomerEncryption customerEncryption;
    private String etag;
    private Boolean eventBasedHold;
    private Long generation;
    private String id;
    private String kind;
    private String kmsKeyName;
    private String md5Hash;
    private String mediaLink;
    private Map<String, String> metadata;
    private Long metageneration;
    private String name;
    private OffsetDateTime retentionExpirationTime;
    private String selfLink;
    private Long size;
    private String storageClass;
    private Boolean temporaryHold;
    private OffsetDateTime timeCreated;
    private OffsetDateTime timeDeleted;
    private OffsetDateTime timeStorageClassUpdated;
    private OffsetDateTime updated;

    @lombok.Data
    public class CustomerEncryption {
        private String encryptionAlgorithm;
        private String keySha256;
    }
}
