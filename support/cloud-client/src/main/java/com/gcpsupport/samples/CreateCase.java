package com.gcpsupport.samples;

// [START gcpsupport_create_case]

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Objects;
import java.util.Map;

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
import com.google.api.services.cloudsupport.v2beta.model.SearchCaseClassificationsResponse;


// sample code to create a support case using support API
public class CreateCase {

    // Shared constants
    final static String CLOUD_SUPPORT_SCOPE = "https://www.googleapis.com/auth/cloudsupport";

    // TODO(developer): Replace this variable with the path 
    // to your service account private key file.
    final static String PRIVATE_KEY_JSON_PATH = "/<---path--->/key.json";

    public static void main(String[] args) {
       
        // Before creating a new case, list all valid classifications of a case with
        // listValidClassifications() first to get a valid classification.
        // A valid classification is required for creating a new case.
        listValidClassifcations();

        // TODO(developer): Create a json object with your new case and put path here
        String createCasePath = "/<---path--->/createCase.json"; 

        // TODO(developer): Replace this variable with your project id
        String PARENT_RESOURCE = "projects/<---project id--->";
        createCase(PARENT_RESOURCE, createCasePath);

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

    // this helper method is used for createCase()
    private static void listValidClassifcations() {

        try {
            CloudSupport supportService = getCloudSupportService();

            SearchCaseClassificationsResponse request = supportService.caseClassifications().search().execute();

            for (Map.Entry<String, Object> entry : request.entrySet()) {
                System.out.println("Key = " + entry.getKey() +
                        ", Value = " + entry.getValue());
            }

        } catch (IOException e) {
            System.out.println("IOException caught in listValidClassifications()! \n" + e);
        }

    }

    // create a new case
    public static void createCase(String parentResource, String newCasePath) {

        try {

            CloudSupport supportService = getCloudSupportService();

            CloudSupportCase newContent = getCloudSupportCaseJsonObject(newCasePath);

            CloudSupportCase newCaseResponse = supportService.cases().create(parentResource, newContent).execute();

            System.out.println("Created case's name is: " + newCaseResponse.getName());

        } catch (IOException e) {
            System.out.println("IOException caught in createCase()! \n" + e);
        }

    }
}

// [END gcpsupport_create_case]