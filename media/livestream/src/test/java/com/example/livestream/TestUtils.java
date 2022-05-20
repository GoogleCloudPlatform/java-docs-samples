package com.example.livestream;

import com.google.cloud.video.livestream.v1.Input;
import com.google.cloud.video.livestream.v1.InputName;
import com.google.cloud.video.livestream.v1.LivestreamServiceClient;
import java.io.IOException;
import java.time.Instant;

public class TestUtils {

  int DELETION_THRESHOLD_TIME_HOURS_IN_SECONDS = 10800; // 3 hours

  public boolean isInputStale(String projectID, String location, String inputID)
      throws IOException {
    try (LivestreamServiceClient livestreamServiceClient = LivestreamServiceClient.create()) {

      InputName name = InputName.of(projectID, location, inputID);
      Input response = livestreamServiceClient.getInput(name);

      return response.getCreateTime().getSeconds()
          < Instant.now().getEpochSecond() - DELETION_THRESHOLD_TIME_HOURS_IN_SECONDS;
    }
  }
}
