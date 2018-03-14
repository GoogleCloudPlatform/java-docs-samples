/*
 * Copyright 2017 Google Inc.
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

package com.example.dlp;

import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.privacy.dlp.v2.InfoTypeDescription;
import com.google.privacy.dlp.v2.ListInfoTypesRequest;
import com.google.privacy.dlp.v2.ListInfoTypesResponse;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Metadata {

  private static void listInfoTypes(String filter, String languageCode) throws Exception {
    // [START dlp_list_info_types]
    // Instantiate a DLP client
    try (DlpServiceClient dlpClient = DlpServiceClient.create()) {
      // The category of info types to list, e.g. category = 'GOVERNMENT';
      // Optional BCP-47 language code for localized info type friendly names, e.g. 'en-US'
      // filter supported_by=INSPECT
      ListInfoTypesRequest listInfoTypesRequest =
          ListInfoTypesRequest.newBuilder().setFilter(filter).setLanguageCode(languageCode).build();
      ListInfoTypesResponse infoTypesResponse = dlpClient.listInfoTypes(listInfoTypesRequest);
      List<InfoTypeDescription> infoTypeDescriptions = infoTypesResponse.getInfoTypesList();
      for (InfoTypeDescription infoTypeDescription : infoTypeDescriptions) {
        System.out.println("Name : " + infoTypeDescription.getName());
        System.out.println("Display name : " + infoTypeDescription.getDisplayName());
      }
    }
    // [END dlp_list_info_types]
  }

  // TODO
  // private static void listRootCategories(String languageCode) throws Exception {
  //   // [START dlp_list_categories]
  //   // Instantiate a DLP client
  //   try (DlpServiceClient dlpClient = DlpServiceClient.create()) {
  //     // The BCP-47 language code to use, e.g. 'en-US'
  //     // languageCode = 'en-US'
  //     ListRootCategoriesResponse rootCategoriesResponse =
  //         dlpClient.listRootCategories(languageCode);
  //     for (CategoryDescription categoryDescription : rootCategoriesResponse.getCategoriesList()) {
  //       System.out.println("Name : " + categoryDescription.getName());
  //       System.out.println("Display name : " + categoryDescription.getDisplayName());
  //     }
  //   }
  //  // [END dlp_list_categories]
  // }

  /** Retrieve infoTypes. */
  public static void main(String[] args) throws Exception {
    Options options = new Options();
    Option languageCodeOption = Option.builder("language").hasArg(true).required(true).build();
    options.addOption(languageCodeOption);

    Option filterOption = Option.builder("filter").hasArg(true).required(false).build();
    options.addOption(filterOption);

    CommandLineParser parser = new DefaultParser();
    HelpFormatter formatter = new HelpFormatter();
    CommandLine cmd;
    try {
      cmd = parser.parse(options, args);
    } catch (ParseException e) {
      System.out.println(e.getMessage());
      formatter.printHelp(Metadata.class.getName(), options);
      System.exit(1);
      return;
    }
    String languageCode = cmd.getOptionValue(languageCodeOption.getOpt(), "en-US");
    String filter = cmd.getOptionValue(filterOption.getOpt(), "");

    listInfoTypes(languageCode, filter);
  }
}
