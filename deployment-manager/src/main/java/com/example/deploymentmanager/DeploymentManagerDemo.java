/*
 * Copyright 2018 Google Inc.
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

        private static String CONFIG_YAML = "yaml/deployment-manager-config.yaml";
        private static String DEPLOYMENT = "my-deployment-1";
    
       
        public static void main(String args[]) throws GeneralSecurityException, IOException {
    
    
            String GOOGLE_PROJECT_ID = System.getenv("GOOGLE_PROJECT_ID");
            if (GOOGLE_PROJECT_ID == null || GOOGLE_PROJECT_ID.compareTo("") == 0) {
                System.out.println("GOOGLE_PROJECT_ID is not set");
                System.exit(-1);
            }
    
            // best practice is to use application default credential. If inside a GCE instance it is set automatically.
            GoogleCredential credential = GoogleCredential.getApplicationDefault();
    
            // to load from somewhere else use this
            // GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream(path_to_key));    
    
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    
    
            DeploymentManager dm2 = new DeploymentManager.Builder(httpTransport, jsonFactory, credential)
                    .setApplicationName("New_Application")
                    .build();
    
            Deployment deploy = new Deployment();
            // Optional  we want some labels: Map of labels; provided by the client when the resource is created or updated.
            List<DeploymentLabelEntry> labels = new ArrayList<DeploymentLabelEntry>();
    
    
            labels.add(new DeploymentLabelEntry()
                    .setKey("test-label-1")
                    .setValue("good-fella"));
    
            deploy.setLabels(labels);
    
    
            // load the config file contents and setup target conrfiguration
            ConfigFile config = new ConfigFile()
                    .setContent(readYaml());
    
    
            TargetConfiguration target = new TargetConfiguration()
                    .setConfig(config);
    
            deploy.setTarget(target);
            deploy.setName(DEPLOYMENT);
    
    
            List<Deployment> deployments =
                    dm2.deployments()
                            .list(GOOGLE_PROJECT_ID)
                            .execute()
                            .getDeployments();
    
            Deployment foundDeployment=null;
            if (deployments != null) {
                System.out.println("Existing Deployments: ");
                for (Deployment deployment : deployments) {
                    System.out.println(deployment.getName());
                    if (deployment.getName().compareTo(DEPLOYMENT) == 0) {
                        foundDeployment=deployment;
                    }
                }
            }
    
            // Alternatively look the deployment directly;
            //   Deployment foundDeployment = dm2.deployments().get(PROJECT_ID,DEPLOYMENT).execute();
    
            if (foundDeployment != null) {
                System.out.println("Updating Existing Deployment "+deploy.getName());
    
                // replace Configuration with new one
                foundDeployment.setTarget(target);
                dm2.deployments()
                        .update(GOOGLE_PROJECT_ID, DEPLOYMENT, foundDeployment)
                        .execute();
            } else {
                System.out.println("Creating New Deployment "+deploy.getName());
                dm2.deployments()
                        .insert(GOOGLE_PROJECT_ID, deploy)
                        .execute();
            }
        }
    
        private static String readYaml() throws IOException {
            InputStream is = new FileInputStream(CONFIG_YAML);
            BufferedReader buf = new BufferedReader(new InputStreamReader(is));
            String line = buf.readLine();
            StringBuilder sb = new StringBuilder();
            while(line != null){ sb.append(line).append("\n"); line = buf.readLine(); }
            return  sb.toString();
    
        }
}
