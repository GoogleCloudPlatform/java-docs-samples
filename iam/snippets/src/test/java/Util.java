/* Copyright 2025 Google LLC
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

import com.google.cloud.iam.admin.v1.IAMClient;
import com.google.iam.admin.v1.CreateServiceAccountKeyRequest;
import com.google.iam.admin.v1.CreateServiceAccountRequest;
import com.google.iam.admin.v1.DeleteServiceAccountKeyRequest;
import com.google.iam.admin.v1.DeleteServiceAccountRequest;
import com.google.iam.admin.v1.DisableServiceAccountRequest;
import com.google.iam.admin.v1.GetServiceAccountKeyRequest;
import com.google.iam.admin.v1.KeyName;
import com.google.iam.admin.v1.ListServiceAccountKeysRequest;
import com.google.iam.admin.v1.ProjectName;
import com.google.iam.admin.v1.ServiceAccount;
import com.google.iam.admin.v1.ServiceAccountKey;
import com.google.iam.admin.v1.ServiceAccountName;
import java.io.IOException;
import java.util.List;

public class Util {
  public static ServiceAccount setUpTest_createServiceAccount(
      String projectId, String serviceAccountName) throws IOException, InterruptedException {

    ServiceAccount serviceAccount =
        ServiceAccount.newBuilder().setDisplayName("service-account-test").build();
    CreateServiceAccountRequest request =
        CreateServiceAccountRequest.newBuilder()
            .setName(ProjectName.of(projectId).toString())
            .setAccountId(serviceAccountName)
            .setServiceAccount(serviceAccount)
            .build();
    try (IAMClient iamClient = IAMClient.create()) {
      serviceAccount = iamClient.createServiceAccount(request);
    }
    awaitForServiceAccountCreation(projectId, serviceAccountName);
    return serviceAccount;
  }

  public static void setUpTest_disableServiceAccount(String projectId, String serviceAccountName)
      throws IOException {
    String email = String.format("%s@%s.iam.gserviceaccount.com", serviceAccountName, projectId);

    try (IAMClient iamClient = IAMClient.create()) {
      iamClient.disableServiceAccount(
          DisableServiceAccountRequest.newBuilder()
              .setName(String.format("projects/%s/serviceAccounts/%s", projectId, email))
              .build());
    }
  }

  public static void tearDownTest_deleteServiceAccount(String projectId, String serviceAccountName)
      throws IOException {
    try (IAMClient client = IAMClient.create()) {
      String accountName = ServiceAccountName.of(projectId, serviceAccountName).toString();
      String accountEmail = String.format("%s@%s.iam.gserviceaccount.com", accountName, projectId);
      DeleteServiceAccountRequest request =
          DeleteServiceAccountRequest.newBuilder().setName(accountEmail).build();
      client.deleteServiceAccount(request);
    }
  }

  public static IAMClient.ListServiceAccountsPagedResponse test_listServiceAccounts(
      String projectId) throws IOException {
    try (IAMClient iamClient = IAMClient.create()) {
      return iamClient.listServiceAccounts(String.format("projects/%s", projectId));
    }
  }

  public static ServiceAccount test_getServiceAccount(String projectId, String serviceAccountName)
      throws IOException {
    String email = String.format("%s@%s.iam.gserviceaccount.com", serviceAccountName, projectId);
    String accountFullName = String.format("projects/%s/serviceAccounts/%s", projectId, email);
    try (IAMClient iamClient = IAMClient.create()) {
      return iamClient.getServiceAccount(accountFullName);
    }
  }

  public static ServiceAccountKey setUpTest_createServiceAccountKey(
      String projectId, String serviceAccountName) throws IOException, InterruptedException {
    awaitForServiceAccountCreation(projectId, serviceAccountName);
    String email = String.format("%s@%s.iam.gserviceaccount.com", serviceAccountName, projectId);
    try (IAMClient iamClient = IAMClient.create()) {
      CreateServiceAccountKeyRequest req =
          CreateServiceAccountKeyRequest.newBuilder()
              .setName(String.format("projects/%s/serviceAccounts/%s", projectId, email))
              .build();
      ServiceAccountKey createdKey = iamClient.createServiceAccountKey(req);
      String serviceAccountKeyId = getServiceAccountKeyIdFromKey(createdKey);
      awaitForServiceAccountKeyCreation(projectId, serviceAccountName, serviceAccountKeyId);

      return createdKey;
    }
  }

  public static void setUpTest_disableServiceAccountKey(
      String projectId, String serviceAccountName, String serviceAccountKeyId) throws IOException {
    String email = String.format("%s@%s.iam.gserviceaccount.com", serviceAccountName, projectId);
    String name =
        String.format(
            "projects/%s/serviceAccounts/%s/keys/%s", projectId, email, serviceAccountKeyId);
    try (IAMClient iamClient = IAMClient.create()) {
      iamClient.disableServiceAccountKey(name);
    }
  }

  public static String getServiceAccountKeyIdFromKey(ServiceAccountKey key) {
    return key.getName().substring(key.getName().lastIndexOf("/") + 1).trim();
  }

  public static void tearDownTest_deleteServiceAccountKey(
      String projectId, String serviceAccountName, String serviceAccountKeyId) throws IOException {
    String accountEmail =
        String.format("%s@%s.iam.gserviceaccount.com", serviceAccountName, projectId);
    String name = KeyName.of(projectId, accountEmail, serviceAccountKeyId).toString();

    DeleteServiceAccountKeyRequest request =
        DeleteServiceAccountKeyRequest.newBuilder().setName(name).build();

    try (IAMClient iamClient = IAMClient.create()) {
      iamClient.deleteServiceAccountKey(request);
    }
  }

  public static List<ServiceAccountKey> test_listServiceAccountKeys(
      String projectId, String serviceAccountName) throws IOException {
    String email = String.format("%s@%s.iam.gserviceaccount.com", serviceAccountName, projectId);
    ListServiceAccountKeysRequest request =
        ListServiceAccountKeysRequest.newBuilder()
            .setName(String.format("projects/%s/serviceAccounts/%s", projectId, email))
            .build();

    try (IAMClient iamClient = IAMClient.create()) {
      return iamClient.listServiceAccountKeys(request).getKeysList();
    }
  }

  public static ServiceAccountKey test_getServiceAccountKey(
      String projectId, String serviceAccountName, String serviceAccountKeyId) throws IOException {
    String email = String.format("%s@%s.iam.gserviceaccount.com", serviceAccountName, projectId);
    String name =
        String.format(
            "projects/%s/serviceAccounts/%s/keys/%s", projectId, email, serviceAccountKeyId);
    try (IAMClient iamClient = IAMClient.create()) {
      return iamClient.getServiceAccountKey(
          GetServiceAccountKeyRequest.newBuilder().setName(name).build());
    }
  }

  private static void awaitForServiceAccountCreation(String projectId, String serviceAccountName)
      throws InterruptedException {
    boolean isAccountCreated = false;
    long time = 1000;
    long timeLimit = 60000;
    while (!isAccountCreated) {
      try {
        test_getServiceAccount(projectId, serviceAccountName);
        isAccountCreated = true;
      } catch (Exception e) {
        Thread.sleep(time);
        time *= 2;
        if (time > timeLimit) {
          break;
        }
      }
    }
  }

  private static void awaitForServiceAccountKeyCreation(
      String projectId, String serviceAccountName, String serviceAccountKeyId)
      throws InterruptedException {
    boolean isAccountCreated = false;
    long time = 1000;
    long timeLimit = 60000;
    while (!isAccountCreated) {
      try {
        test_getServiceAccountKey(projectId, serviceAccountName, serviceAccountKeyId);
        isAccountCreated = true;
      } catch (Exception e) {
        if (time > timeLimit) {
          break;
        }
        Thread.sleep(time);
        time *= 2;
      }
    }
  }
}
