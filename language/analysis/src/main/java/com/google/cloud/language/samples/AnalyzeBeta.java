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

package com.google.cloud.language.samples;


import com.google.cloud.language.v1beta2.AnalyzeEntitySentimentRequest;
import com.google.cloud.language.v1beta2.AnalyzeEntitySentimentResponse;
import com.google.cloud.language.v1beta2.AnalyzeSentimentResponse;
import com.google.cloud.language.v1beta2.Document;
import com.google.cloud.language.v1beta2.Document.Type;
import com.google.cloud.language.v1beta2.EncodingType;
import com.google.cloud.language.v1beta2.Entity;
import com.google.cloud.language.v1beta2.EntityMention;
import com.google.cloud.language.v1beta2.LanguageServiceClient;
import com.google.cloud.language.v1beta2.Sentiment;
import com.google.protobuf.Descriptors;

import java.io.IOException;
import java.io.PrintStream;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;

/**
 * A sample application that uses the Natural Language API to perform
 * entity, sentiment and syntax analysis.
 */
public class AnalyzeBeta {
  private final LanguageServiceClient languageApi;

  /**
   * Constructs a {@link Analyze} which connects to the Cloud Natural Language API.
   */
  public AnalyzeBeta(LanguageServiceClient languageApi) {
    this.languageApi = languageApi;
  }


  /**
   * Detects entities,sentiment and syntax in a document using the Natural Language API.
   */
  public static void main(String[] args) throws IOException, GeneralSecurityException {
    if (args.length < 2 || args.length > 4) {
      System.err.println("Usage:");
      System.err.printf(
          "\tjava %s \"command\" \"text to analyze\" \"language\" \n",
          Analyze.class.getCanonicalName());
      System.exit(1);
    }
    String command = args[0];
    String text = args[1];
    String lang = null;
    if (args.length > 2) {
      lang = args[2];
    }

    AnalyzeBeta app = new AnalyzeBeta(LanguageServiceClient.create());

    if (command.equals("entities-sentiment")) {
      if (text.startsWith("gs://")) {
        printEntities(System.out, app.entitySentimentFile(text));
      } else {
        printEntities(System.out, app.entitySentimentText(text));
      }
    } else if (command.equals("sentiment")) {
      printSentiment(System.out, app.analyzeSentimentText(text, lang));
    }
  }

  /**
   * Print the Sentiment {@code sentiment}.
   */
  public static void printSentiment(PrintStream out, Sentiment sentiment) {
    if (sentiment == null) {
      out.println("No sentiment found");
      return;
    }
    out.println("Found sentiment.");
    out.printf("\tMagnitude: %.3f\n", sentiment.getMagnitude());
    out.printf("\tScore: %.3f\n", sentiment.getScore());
  }

  /**
   * Print a list of {@code entities}.
   */
  public static void printEntities(PrintStream out, List<Entity> entities) {
    if (entities == null || entities.size() == 0) {
      out.println("No entities found.");
      return;
    }
    out.printf("Found %d entit%s.\n", entities.size(), entities.size() == 1 ? "y" : "ies");
    for (Entity entity : entities) {
      out.printf("----\n\"%s\"\n", entity.getName());
      out.printf("\tSalience: %.3f\n", entity.getSalience());
      out.printf("\tSentiment Magnitude: %.3f\n", entity.getSentiment().getMagnitude());
      out.printf("\tSentiment Score: %.3f\n", entity.getSentiment().getScore());
      out.printf("\tType: %s\n", entity.getType());
      if (entity.getMetadataMap() != null) {
        for (Map.Entry<String, String> metadata : entity.getMetadataMap().entrySet()) {
          out.printf("\tMetadata: %s = %s\n", metadata.getKey(), metadata.getValue());
        }
      }
      if (entity.getMentionsList() != null) {
        for (EntityMention mention : entity.getMentionsList()) {
          for (Map.Entry<Descriptors.FieldDescriptor, Object> mentionSetMember :
              mention.getAllFields().entrySet()) {
            out.printf("\tMention: %s = %s\n", mentionSetMember.getKey(),
                mentionSetMember.getValue());
          }
        }
      }
    }
  }

  /**
   * Gets {@link Sentiment} from the string {@code text}.
   */
  public Sentiment analyzeSentimentText(String text, String lang) throws IOException {
    // NL autodetects the language
    Document doc;
    if (lang != null) {
      doc = Document.newBuilder()
          .setLanguage(lang)
          .setContent(text).setType(Type.PLAIN_TEXT)
          .build();
    } else {
      doc = Document.newBuilder()
          .setContent(text).setType(Type.PLAIN_TEXT)
          .build();
    }
    AnalyzeSentimentResponse response = languageApi.analyzeSentiment(doc);
    return response.getDocumentSentiment();
  }

  /**
   * Gets {@link Entity}s from the string {@code text} with sentiment.
   */
  public List<Entity> entitySentimentText(String text) throws IOException {
    Document doc = Document.newBuilder()
            .setContent(text).setType(Type.PLAIN_TEXT).build();
    AnalyzeEntitySentimentRequest request = AnalyzeEntitySentimentRequest.newBuilder()
            .setDocument(doc)
            .setEncodingType(EncodingType.UTF16).build();
    AnalyzeEntitySentimentResponse response = languageApi.analyzeEntitySentiment(request);
    return response.getEntitiesList();
  }

  /**
   * Gets {@link Entity}s from the contents of the object at the given GCS {@code path}
   * with sentiment.
   */
  public List<Entity> entitySentimentFile(String path) throws IOException {
    Document doc = Document.newBuilder()
            .setGcsContentUri(path).setType(Type.PLAIN_TEXT).build();
    AnalyzeEntitySentimentRequest request = AnalyzeEntitySentimentRequest.newBuilder()
            .setDocument(doc)
            .setEncodingType(EncodingType.UTF16).build();
    AnalyzeEntitySentimentResponse response = languageApi.analyzeEntitySentiment(request);
    return response.getEntitiesList();
  }
}
