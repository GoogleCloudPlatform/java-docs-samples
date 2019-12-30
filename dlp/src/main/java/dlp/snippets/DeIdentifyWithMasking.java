package dlp.snippets;

// [START dlp_deidentify_masking]

import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.privacy.dlp.v2.CharacterMaskConfig;
import com.google.privacy.dlp.v2.ContentItem;
import com.google.privacy.dlp.v2.DeidentifyConfig;
import com.google.privacy.dlp.v2.DeidentifyContentRequest;
import com.google.privacy.dlp.v2.DeidentifyContentResponse;
import com.google.privacy.dlp.v2.InfoType;
import com.google.privacy.dlp.v2.InfoTypeTransformations;
import com.google.privacy.dlp.v2.InfoTypeTransformations.InfoTypeTransformation;
import com.google.privacy.dlp.v2.InspectConfig;
import com.google.privacy.dlp.v2.PrimitiveTransformation;
import com.google.privacy.dlp.v2.ProjectName;
import com.google.privacy.dlp.v2.ReplaceWithInfoTypeConfig;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DeIdentifyWithMasking {

  public static void deIdentifyWithMasking() throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String textToDeIdentify = "I'm Gary and my email is gary@example.com";
    deIdentifyWithMasking(projectId, textToDeIdentify);
  }

  public static void deIdentifyWithMasking(String projectId, String textToDeIdentify)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (DlpServiceClient dlp = DlpServiceClient.create()) {

      // Specify what content you want the service to DeIdentify
      ContentItem contentItem = ContentItem.newBuilder().setValue(textToDeIdentify).build();

      // Specify the type of info the inspection will look for.
      // See https://cloud.google.com/dlp/docs/infotypes-reference for complete list of info types
      List<InfoType> infoTypes =
          Stream.of("US_SOCIAL_SECURITY_NUMBER")
              .map(it -> InfoType.newBuilder().setName(it).build())
              .collect(Collectors.toList());

      InspectConfig inspectConfig = InspectConfig.newBuilder().addAllInfoTypes(infoTypes).build();

      // Specify how the info from the inspection should be masked.
      CharacterMaskConfig characterMaskConfig =
          CharacterMaskConfig.newBuilder()
              .setMaskingCharacter("X") // Character to replace the found info with
              .setNumberToMask(5) // How many characters should be masked
              .build();
      PrimitiveTransformation primitiveTransformation =
          PrimitiveTransformation.newBuilder()
              .setReplaceWithInfoTypeConfig(ReplaceWithInfoTypeConfig.getDefaultInstance())
              .build();
      InfoTypeTransformation infoTypeTransformation =
          InfoTypeTransformation.newBuilder()
              .setPrimitiveTransformation(primitiveTransformation)
              .build();
      InfoTypeTransformations transformations =
          InfoTypeTransformations.newBuilder().addTransformations(infoTypeTransformation).build();

      DeidentifyConfig deidentifyConfig =
          DeidentifyConfig.newBuilder().setInfoTypeTransformations(transformations).build();

      // Combine configurations into a request for the service.
      DeidentifyContentRequest request =
          DeidentifyContentRequest.newBuilder()
              .setParent(ProjectName.of(projectId).toString())
              .setItem(contentItem)
              .setInspectConfig(inspectConfig)
              .setDeidentifyConfig(deidentifyConfig)
              .build();

      // Send the request and receive response from the service
      DeidentifyContentResponse response = dlp.deidentifyContent(request);

      // Print the results
      response.getOverview();
      System.out.println("Text after masking: " + response.getItem().getValue());
    }
  }
}
// [END dlp_deidentify_masking]
