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

// [START cloud_sql_sqlserver_cse_key]

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.KmsClient;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.AeadKeyTemplates;
import com.google.crypto.tink.aead.KmsEnvelopeAead;
import com.google.crypto.tink.integration.gcpkms.GcpKmsClient;
import java.security.GeneralSecurityException;

public class CloudKmsEnvelopeAead {

  public static Aead get(String kmsUri) throws GeneralSecurityException {
    AeadConfig.register();

    // Create a new KMS Client
    KmsClient client = new GcpKmsClient().withDefaultCredentials();

    // Create an AEAD primitive using the Cloud KMS key
    Aead gcpAead = client.getAead(kmsUri);

    // Create an envelope AEAD primitive
    return new KmsEnvelopeAead(AeadKeyTemplates.AES128_GCM, gcpAead);
  }
}
// [END cloud_sql_sqlserver_cse_key]