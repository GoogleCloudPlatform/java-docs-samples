package com.google.cloud.language.automl.entity.extraction.samples;

// [START automl_natural_language_entity_get_model_evaluation]
import com.google.cloud.automl.v1beta1.AutoMlClient;
import com.google.cloud.automl.v1beta1.ModelEvaluation;
import com.google.cloud.automl.v1beta1.ModelEvaluationName;
import com.google.cloud.automl.v1beta1.TextExtractionEvaluationMetrics;
import com.google.cloud.automl.v1beta1.TextExtractionEvaluationMetrics.ConfidenceMetricsEntry;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class GetModelEvaluation {

  public static void getModelEvaluation(
      String projectId, String computeRegion, String modelId, String modelEvaluationId)
      throws IOException {
    // String projectId = "YOUR_PROJECT_ID";
    // String computeRegion = "YOUR_PROJECT_ID";
    // String modelId = "YOUR_MODEL_ID";
    // String modelEvaluationId = "YOUR_MODEL_EVALUATION_ID";

    // Instantiates a client
    AutoMlClient client = AutoMlClient.create();

    // Get the full path of the model evaluation.
    ModelEvaluationName modelEvaluationFullId =
        ModelEvaluationName.of(projectId, computeRegion, modelId, modelEvaluationId);

    // Get complete detail of the model evaluation.
    ModelEvaluation response = client.getModelEvaluation(modelEvaluationFullId);

    // Display the model evaluations information.
    System.out.println(String.format("Model evaluation name: %s", response.getName()));
    System.out.println(
        String.format(
            "Model evaluation Id: %s",
            response.getName().split("/")[response.getName().split("/").length - 1]));
    System.out.println(
        String.format("Model evaluation annotation spec Id: %s", response.getAnnotationSpecId()));
    System.out.println(
        String.format("Model evaluation example count: %s", response.getEvaluatedExampleCount()));
    System.out.println(
        String.format("Model evaluation display name: %s", response.getDisplayName()));
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    String createTime =
        dateFormat.format(new java.util.Date(response.getCreateTime().getSeconds() * 1000));
    System.out.println(String.format("Model evaluation create time: %s", createTime));

    System.out.println("Text extraction evaluation metrics:");
    TextExtractionEvaluationMetrics textExtractionMetrics =
        response.getTextExtractionEvaluationMetrics();
    List<ConfidenceMetricsEntry> confidenceMetricsEntries =
        textExtractionMetrics.getConfidenceMetricsEntriesList();

    // Showing text extraction evaluation metrics
    System.out.println("\tConfidence metrics entries:");
    for (ConfidenceMetricsEntry confidenceMetricsEntry : confidenceMetricsEntries) {
      System.out.println(
          String.format(
              "\t\tModel confidence threshold: %.2f ",
              confidenceMetricsEntry.getConfidenceThreshold()));
      System.out.println(
          String.format("\t\tModel recall: %.2f ", confidenceMetricsEntry.getRecall() * 100) + '%');
      System.out.println(
          String.format("\t\tModel precision: %.2f ", confidenceMetricsEntry.getPrecision() * 100)
              + '%');
      System.out.println(
          String.format("\t\tModel f1 score: %.2f ", confidenceMetricsEntry.getF1Score() * 100)
              + '%'
              + '\n');
    }
  }
}
// [END automl_natural_language_entity_get_model_evaluation]
