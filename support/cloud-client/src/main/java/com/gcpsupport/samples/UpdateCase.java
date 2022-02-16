package com.gcpsupport.samples;

// [START gcpsupport_update_cases]

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Objects;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.cloudsupport.v2beta.CloudSupport;
import com.google.api.services.cloudsupport.v2beta.model.CloudSupportCase;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.api.client.json.JsonObjectParser;


// sample code to update a support case using support API
public class UpdateCase {

    // Shared constants
    final static String CLOUD_SUPPORT_SCOPE = "https://www.googleapis.com/auth/cloudsupport";

    // TODO(developer): Replace this variable with the path 
    // to your service account private key file.
    final static String PRIVATE_KEY_JSON_PATH = "/<---path--->/key.json";

    public static void main(String[] args) {

        // TODO(developer): Create a json object with your new case and put path here
        String updatedCasePath = "/<---path--->/updateCase.json"; 

        // TODO(developer): Replace this variable with your project id
        String PARENT_RESOURCE = "projects/<---project id--->";
        updateCase(PARENT_RESOURCE + "/cases/<---case id--->", updatedCasePath);


    }

    // helper method will return a CloudSupport object which is required for the
    // main API service to be used.
    private static CloudSupport getCloudSupportService() {

        try {

            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

            InputStream credentialsInputStream = new FileInputStream(new File(PRIVATE_KEY_JSON_PATH));
            
            GoogleCredentials credentials = ServiceAccountCredentials
                                            .fromStream(Objects.requireNonNull(credentialsInputStream))
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

    // helper method to get a CloudSupportCase object
    private static CloudSupportCase getCloudSupportCaseJsonObject(String jsonPathName) throws IOException {
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        JsonObjectParser parser = new JsonObjectParser(jsonFactory);
        InputStream stream = new FileInputStream(new File(jsonPathName));
        Reader reader = new InputStreamReader(stream, "UTF-8");
        return parser.parseAndClose(reader, CloudSupportCase.class);
    }

    // update one case
    public static void updateCase(String nameOfCase, String updatedCasePath) {

        try {

            CloudSupport supportService = getCloudSupportService();

            CloudSupportCase updateCase = getCloudSupportCaseJsonObject(updatedCasePath);

            CloudSupportCase updateCaseResponse = supportService.cases().patch(nameOfCase, updateCase).execute();

            System.out.println("Updated case object is: " + updateCaseResponse + "\n\n\n");

        } catch (IOException e) {
            System.out.println("IOException caught in updateCase()! \n" + e);
        }

    }
}

// [END gcpsupport_update_cases]