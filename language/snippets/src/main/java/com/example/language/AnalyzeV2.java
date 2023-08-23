/*
 * Copyright 2023 Google Inc.
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

package com.example.language;

import com.google.cloud.language.v2.AnalyzeEntitiesRequest;
import com.google.cloud.language.v2.AnalyzeEntitiesResponse;
import com.google.cloud.language.v2.AnalyzeEntitySentimentRequest;
import com.google.cloud.language.v2.AnalyzeEntitySentimentResponse;
import com.google.cloud.language.v2.AnalyzeSentimentResponse;
import com.google.cloud.language.v2.ClassificationCategory;
import com.google.cloud.language.v2.ClassifyTextRequest;
import com.google.cloud.language.v2.ClassifyTextResponse;
import com.google.cloud.language.v2.Document;
import com.google.cloud.language.v2.Document.Type;
import com.google.cloud.language.v2.EncodingType;
import com.google.cloud.language.v2.Entity;
import com.google.cloud.language.v2.EntityMention;
import com.google.cloud.language.v2.LanguageServiceClient;
import com.google.cloud.language.v2.Sentiment;
import com.google.cloud.language.v2.Token;
import java.util.List;
import java.util.Map;

/**
 * A sample application that uses the Natural Language API to perform entity, sentiment and syntax
 * analysis.
 */
public class Analyze {

  /** Detects entities,sentiment and syntax in a document using the Natural Language API. */
  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      System.err.println("Usage:");
      System.err.printf(
          "\tjava %s \"command\" \"text to analyze\"\n", Analyze.class.getCanonicalName());
      System.exit(1);
    }
    String command = args[0];
    String text = args[1];

    if (command.equals("classify")) {
      if (text.startsWith("gs://")) {
        classifyFile(text);
      } else {
        classifyText(text);
      }
    } else if (command.equals("entities")) {
      if (text.startsWith("gs://")) {
        analyzeEntitiesFile(text);
      } else {
        analyzeEntitiesText(text);
      }
    } else if (command.equals("sentiment")) {
      if (text.startsWith("gs://")) {
        analyzeSentimentFile(text);
      } else {
        analyzeSentimentText(text);
      }
    } else if (command.equals("syntax")) {
      if (text.startsWith("gs://")) {
        analyzeSyntaxFile(text);
      } else {
        analyzeSyntaxText(text);
      }
    } else if (command.equals("entities-sentiment")) {
      if (text.startsWith("gs://")) {
        entitySentimentFile(text);
      } else {
        entitySentimentText(text);
      }
    }
  }

  /** Identifies entities in the string {@code text}. */
  public static void analyzeEntitiesText(String text) throws Exception {
    // [START language_entities_text]
    // Instantiate the Language client com.google.cloud.language.v1.LanguageServiceClient
    try (LanguageServiceClient language = LanguageServiceClient.create()) {
      Document doc = Document.newBuilder().setContent(text).setType(Type.PLAIN_TEXT).build();
      AnalyzeEntitiesRequest request =
          AnalyzeEntitiesRequest.newBuilder()
              .setDocument(doc)
              .setEncodingType(EncodingType.UTF16)
              .build();

      AnalyzeEntitiesResponse response = language.analyzeEntities(request);

      // Print the response
      for (Entity entity : response.getEntitiesList()) {
        System.out.printf("Entity: %s", entity.getName());
        System.out.println("Metadata: ");
        for (Map.Entry<String, String> entry : entity.getMetadataMap().entrySet()) {
          System.out.printf("%s : %s", entry.getKey(), entry.getValue());
        }
        for (EntityMention mention : entity.getMentionsList()) {
          System.out.printf("Begin offset: %d\n", mention.getText().getBeginOffset());
          System.out.printf("Content: %s\n", mention.getText().getContent());
          System.out.printf("Type: %s\n\n", mention.getType());
          System.out.printf("Probability: %s\n\n", mention.getProbability());
        }
      }
    }
    // [END language_entities_text]
  }

  /** Identifies entities in the contents of the object at the given GCS {@code path}. */
  public static void analyzeEntitiesFile(String gcsUri) throws Exception {
    // [START language_entities_gcs]
    // Instantiate the Language client com.google.cloud.language.v1.LanguageServiceClient
    try (LanguageServiceClient language = LanguageServiceClient.create()) {
      // Set the GCS Content URI path to the file to be analyzed
      Document doc =
          Document.newBuilder().setGcsContentUri(gcsUri).setType(Type.PLAIN_TEXT).build();
      AnalyzeEntitiesRequest request =
          AnalyzeEntitiesRequest.newBuilder()
              .setDocument(doc)
              .setEncodingType(EncodingType.UTF16)
              .build();

      AnalyzeEntitiesResponse response = language.analyzeEntities(request);

      // Print the response
      for (Entity entity : response.getEntitiesList()) {
        System.out.printf("Entity: %s\n", entity.getName());
        System.out.println("Metadata: ");
        for (Map.Entry<String, String> entry : entity.getMetadataMap().entrySet()) {
          System.out.printf("%s : %s", entry.getKey(), entry.getValue());
        }
        for (EntityMention mention : entity.getMentionsList()) {
          System.out.printf("Begin offset: %d\n", mention.getText().getBeginOffset());
          System.out.printf("Content: %s\n", mention.getText().getContent());
          System.out.printf("Type: %s\n\n", mention.getType());
          System.out.printf("Probability: %s\n\n", mention.getProbability());
        }
      }
    }
    // [END language_entities_gcs]
  }

  /** Identifies the sentiment in the string {@code text}. */
  public static Sentiment analyzeSentimentText(String text) throws Exception {
    // [START language_sentiment_text]
    // Instantiate the Language client com.google.cloud.language.v1.LanguageServiceClient
    try (LanguageServiceClient language = LanguageServiceClient.create()) {
      Document doc = Document.newBuilder().setContent(text).setType(Type.PLAIN_TEXT).build();
      AnalyzeSentimentResponse response = language.analyzeSentiment(doc);
      Sentiment sentiment = response.getDocumentSentiment();
      if (sentiment == null) {
        System.out.println("No sentiment found");
      } else {
        System.out.printf("Sentiment magnitude: %.3f\n", sentiment.getMagnitude());
        System.out.printf("Sentiment score: %.3f\n", sentiment.getScore());
      }
      return sentiment;
    }
    // [END language_sentiment_text]
  }

  /** Gets {@link Sentiment} from the contents of the GCS hosted file. */
  public static Sentiment analyzeSentimentFile(String gcsUri) throws Exception {
    // [START language_sentiment_gcs]
    // Instantiate the Language client com.google.cloud.language.v1.LanguageServiceClient
    try (LanguageServiceClient language = LanguageServiceClient.create()) {
      Document doc =
          Document.newBuilder().setGcsContentUri(gcsUri).setType(Type.PLAIN_TEXT).build();
      AnalyzeSentimentResponse response = language.analyzeSentiment(doc);
      Sentiment sentiment = response.getDocumentSentiment();
      if (sentiment == null) {
        System.out.println("No sentiment found");
      } else {
        System.out.printf("Sentiment magnitude : %.3f\n", sentiment.getMagnitude());
        System.out.printf("Sentiment score : %.3f\n", sentiment.getScore());
      }
      return sentiment;
    }
    // [END language_sentiment_gcs]
  }

  /** Detects categories in text using the Language Beta API. */
  public static void classifyText(String text) throws Exception {
    // [START language_classify_text]
    // Instantiate the Language client com.google.cloud.language.v1.LanguageServiceClient
    try (LanguageServiceClient language = LanguageServiceClient.create()) {
      // Set content to the text string
      Document doc = Document.newBuilder().setContent(text).setType(Type.PLAIN_TEXT).build();
      ClassifyTextRequest request = ClassifyTextRequest.newBuilder().setDocument(doc).build();
      // Detect categories in the given text
      ClassifyTextResponse response = language.classifyText(request);

      for (ClassificationCategory category : response.getCategoriesList()) {
        System.out.printf(
            "Category name : %s, Confidence : %.3f\n",
            category.getName(), category.getConfidence());
      }
    }
    // [END language_classify_text]
  }

  /** Detects categories in a GCS hosted file using the Language Beta API. */
  public static void classifyFile(String gcsUri) throws Exception {
    // [START language_classify_gcs]
    // Instantiate the Language client com.google.cloud.language.v1.LanguageServiceClient
    try (LanguageServiceClient language = LanguageServiceClient.create()) {
      // Set the GCS content URI path
      Document doc =
          Document.newBuilder().setGcsContentUri(gcsUri).setType(Type.PLAIN_TEXT).build();
      ClassifyTextRequest request = ClassifyTextRequest.newBuilder().setDocument(doc).build();
      // Detect categories in the given file
      ClassifyTextResponse response = language.classifyText(request);

      for (ClassificationCategory category : response.getCategoriesList()) {
        System.out.printf(
            "Category name : %s, Confidence : %.3f\n",
            category.getName(), category.getConfidence());
      }
    }
    // [END language_classify_gcs]
  }
}
