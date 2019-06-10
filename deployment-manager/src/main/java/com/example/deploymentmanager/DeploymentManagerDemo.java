/*
 * Copyright 2019 Google LLC
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

package com.example.deploymentmanager;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.deploymentmanager.DeploymentManager;
import com.google.api.services.deploymentmanager.model.*;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeploymentManagerDemo {

  private static String
          GOOGLE_PROJECT_ID,
          DEPLOYMENT_NAME,
          CONFIG_YAML = "yaml/deployment-manager-config.yaml";
  
  private static DeploymentManager _dm = null;

  public static void main(String args[]) {
    init();
    runDemo();
    // cleanup(); // Optionally delete resources.
  }

  /*
   Initialize Google api.
  */
  public static void init() {

    GOOGLE_PROJECT_ID = System.getenv("GOOGLE_PROJECT_ID");
    if (GOOGLE_PROJECT_ID == null || GOOGLE_PROJECT_ID.compareTo("") == 0) {
      System.out.println("GOOGLE_PROJECT_ID environment variable is not set");
      System.exit(-1);
    }

    DEPLOYMENT_NAME = System.getenv("DEPLOYMENT_NAME");
    if (DEPLOYMENT_NAME == null || DEPLOYMENT_NAME.compareTo("") == 0) {
      System.out.println("DEPLOYMENT_NAME environment variable is not set");
      System.exit(-1);
    }

    try {
      // Use the "Application Default Credentials" to authenticate the application. For more detail, see:
      // https://cloud.google.com/docs/authentication/production
      GoogleCredential credential = GoogleCredential.getApplicationDefault();

      // To load an application key from a file use:
      // GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream(path_to_key));

      HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
      JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
      _dm = new DeploymentManager.Builder(httpTransport, jsonFactory, credential)
              .setApplicationName("New_Application")
              .build();
    } catch (IOException ex) {
      System.out.println("Could not load default application credentials. " + ex.getMessage());
      System.exit(-1);
    } catch (GeneralSecurityException ex) {
      System.out.println("Could not initialize Trusted Transport. " + ex.getMessage());
      System.exit(-1);
    }
  }
  /* 
   Delete the deployment.
  */
  public static void cleanup() {
    try {
      Operation response;
      response = _dm.deployments()
                    .delete(DEPLOYMENT_NAME,GOOGLE_PROJECT_ID)
                    .execute();

    } catch (IOException ex) {
      System.out.println("Could not delete deployment. " + ex.getMessage());
      System.exit(-1);
    }
  }


  public static void runDemo() {   
    // Grab a list of existing deployments.
    List<Deployment> deployments = null;
    try {
      DeploymentsListResponse response;
      response = _dm.deployments()
              .list(GOOGLE_PROJECT_ID)
              .execute();

      deployments = response.getDeployments();
    } catch (IOException ex) {
      System.out.println("Could not get deployments. " + ex.getMessage());
      System.exit(-1);
    }

    // Grab the existing deployment by DEPLOYMENT_NAME. If not found we will insert a new one.
    boolean updateDeployment = false;
    Deployment deploymentToInsertOrUpdate = new Deployment();
    if (deployments != null) {
      System.out.println("Existing Deployments: ");
      for (Deployment deployment : deployments) {
        System.out.println(deployment.getName());
        if (deployment.getName().compareTo(DEPLOYMENT_NAME) == 0) {
          deploymentToInsertOrUpdate = deployment;
          updateDeployment = true;
        }
      }
    }

    // Alternatively look the deployment directly;
    // Deployment foundDeployment = dm2.deployments().get(PROJECT_ID,DEPLOYMENT).execute();

    // Optional we want some labels: Map of labels; provided by the client when the
    // resource is created or updated.
    List<DeploymentLabelEntry> labels = new ArrayList<DeploymentLabelEntry>();
    labels.add(new DeploymentLabelEntry()
            .setKey("test-label-1")
            .setValue("good-fella"));

    // Note, if this is an Update, existing labels will be overwritten.
    deploymentToInsertOrUpdate.setLabels(labels);

    try {
      // Load the config file contents and setup target configuration.
      ConfigFile config = new ConfigFile().setContent(readYaml());

      TargetConfiguration target = new TargetConfiguration().setConfig(config);
      deploymentToInsertOrUpdate.setTarget(target);
      deploymentToInsertOrUpdate.setName(DEPLOYMENT_NAME);

    } catch (IOException ex) {
      System.out.println("Could not load deployment manager config file: '" + CONFIG_YAML + "'. " + ex.getMessage());
      System.exit(-1);
    }

    try {
      if (updateDeployment) {
        System.out.println("Updating Existing Deployment " + deploymentToInsertOrUpdate.getName());
        _dm.deployments().update(GOOGLE_PROJECT_ID, DEPLOYMENT_NAME, deploymentToInsertOrUpdate).execute();
      } else {
        System.out.println("Creating New Deployment " + deploymentToInsertOrUpdate.getName());
        _dm.deployments().insert(GOOGLE_PROJECT_ID, deploymentToInsertOrUpdate).execute();
      }
    } catch (IOException ex) {
      System.out.println("Could not insert/update deployment. " + ex.getMessage());
      System.exit(-1);
    }
  }

  private static String readYaml() throws IOException {
    InputStream is = new FileInputStream(CONFIG_YAML);
    BufferedReader buf = new BufferedReader(new InputStreamReader(is));
    String line = buf.readLine();
    StringBuilder sb = new StringBuilder();
    while (line != null) {
      sb.append(line).append("\n");
      line = buf.readLine();
    }
    return sb.toString();

  }
}
