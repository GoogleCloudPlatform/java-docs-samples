package com.gcpsupport.samples;

// [START gcpsupport_starter]

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Objects;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.cloudsupport.v2beta.CloudSupport;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;

//starter code for cloud support API to initialize it
public class Starter {

    // Shared constants
    final static String CLOUD_SUPPORT_SCOPE = "https://www.googleapis.com/auth/cloudsupport";

    // TODO(developer): Replace this variable with the path 
    // to your service account private key file.
    final static String PRIVATE_KEY_JSON_PATH = "/<---path--->/key.json";

    public static void main(String[] args) {
        CloudSupport cloudSupport = getCloudSupportService();

        //with the CloudSupport object, you may call other methods of the Support API
        //for example, cloudSupport.cases().get("name of case").execute();
    }

    // helper method will return a CloudSupport object which is required for the
    // main API service to be used.
    private static CloudSupport getCloudSupportService() {

        try {

            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

            InputStream credentialsInputStream = new FileInputStream(new File(PRIVATE_KEY_JSON_PATH));
            
            GoogleCredentials credentials = ServiceAccountCredentials.fromStream(Objects.requireNonNull(credentialsInputStream))
                                            .createScoped(Collections.singletonList(CLOUD_SUPPORT_SCOPE));

            HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

            return new CloudSupport.Builder(httpTransport, jsonFactory, requestInitializer).build();

        } catch (IOException e) {
            System.out.println("IOException caught in getCloudSupportService()! \n" + e);

        } catch (GeneralSecurityException e) {
            System.out.println("GeneralSecurityException caught in getCloudSupportService! \n" + e);
        }

        return null;

    }
}

// [END gcpsupport_starter]