/*
 * Copyright 2021 Google LLC
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

package cloudsql.tink;

// [START cloud_sql_mysql_initialize_aead]

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.KmsClients;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.AeadKeyTemplates;
import com.google.crypto.tink.integration.gcpkms.GcpKmsClient;
import com.google.crypto.tink.proto.KeyTemplate;
import java.security.GeneralSecurityException;

public class CloudKmsEnvelopeAead {

  public Aead envAead;

  public CloudKmsEnvelopeAead(String kmsUri) throws GeneralSecurityException {
    AeadConfig.register();
    envAead = getEnvelopeAead(kmsUri);
  }

  private static Aead getEnvelopeAead(String kmsUri) throws GeneralSecurityException {
    // Generate a new envelope key template, then generate key material.
    KeyTemplate kmsEnvKeyTemplate = AeadKeyTemplates
        .createKmsEnvelopeAeadKeyTemplate(kmsUri, AeadKeyTemplates.AES128_GCM);
    KeysetHandle keysetHandle = KeysetHandle.generateNew(kmsEnvKeyTemplate);

    // Register the KMS client.
    KmsClients.add(new GcpKmsClient()
        .withDefaultCredentials());

    // Create envelope AEAD primitive from keysetHandle
    return keysetHandle.getPrimitive(Aead.class);
  }
}
// [END cloud_sql_mysql_initialize_aead]