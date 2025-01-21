/*
 * Copyright 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package aiplatform;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.cloud.aiplatform.v1.EndpointName;
import com.google.cloud.aiplatform.v1.PredictionServiceClient;
import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

public class Gemma2ParametersTest {

  static PredictionServiceClient mockGpuPredictionServiceClient;
  static PredictionServiceClient mockTpuPredictionServiceClient;
  private static final String INSTANCE_GPU = "{ \"inputs\": \"Why is the sky blue?\"}";
  private static final String INSTANCE_TPU = "{ \"prompt\": \"Why is the sky blue?\"}";

  @Test
  public void parametersTest() throws InvalidProtocolBufferException {
    // Mock GPU and TPU PredictionServiceClient and its response
    mockGpuPredictionServiceClient = Mockito.mock(PredictionServiceClient.class);
    mockTpuPredictionServiceClient = Mockito.mock(PredictionServiceClient.class);

    Value.Builder instanceValueGpu = Value.newBuilder();
    JsonFormat.parser().merge(INSTANCE_GPU, instanceValueGpu);
    List<Value> instancesGpu = new ArrayList<>();
    instancesGpu.add(instanceValueGpu.build());

    Value.Builder instanceValueTpu = Value.newBuilder();
    JsonFormat.parser().merge(INSTANCE_TPU, instanceValueTpu);
    List<Value> instancesTpu = new ArrayList<>();
    instancesTpu.add(instanceValueTpu.build());

    Map<String, Object> paramsMap = new HashMap<>();
    paramsMap.put("temperature", 0.9);
    paramsMap.put("maxOutputTokens", 1024);
    paramsMap.put("topP", 1.0);
    paramsMap.put("topK", 1);
    Value parameters = mapToValue(paramsMap);

    Mockito.when(mockGpuPredictionServiceClient.predict(
        Mockito.any(EndpointName.class),
        Mockito.any(List.class),
        Mockito.any(Value.class)))
        .thenAnswer(invocation ->
            mockGpuResponse(instancesGpu, parameters));

    Mockito.when(mockTpuPredictionServiceClient.predict(
        Mockito.any(EndpointName.class),
        Mockito.any(List.class),
        Mockito.any(Value.class)))
        .thenAnswer(invocation ->
            mockTpuResponse(instancesTpu, parameters));
  }

  public static Answer<?> mockGpuResponse(List<Value> instances, Value parameter) {

    assertTrue(instances.get(0).getStructValue().getFieldsMap().containsKey("inputs"));
    assertTrue(parameter.getStructValue().containsFields("temperature"));
    assertTrue(parameter.getStructValue().containsFields("maxOutputTokens"));
    assertTrue(parameter.getStructValue().containsFields("topP"));
    assertTrue(parameter.getStructValue().containsFields("topK"));
    return null;
  }

  public static Answer<?> mockTpuResponse(List<Value> instances, Value parameter) {

    assertTrue(instances.get(0).getStructValue().getFieldsMap().containsKey("prompt"));
    assertTrue(parameter.getStructValue().containsFields("temperature"));
    assertTrue(parameter.getStructValue().containsFields("maxOutputTokens"));
    assertTrue(parameter.getStructValue().containsFields("topP"));
    assertTrue(parameter.getStructValue().containsFields("topK"));
    return null;
  }

  private static Value mapToValue(Map<String, Object> map) throws InvalidProtocolBufferException {
    Gson gson = new Gson();
    String json = gson.toJson(map);
    Value.Builder builder = Value.newBuilder();
    JsonFormat.parser().merge(json, builder);
    return builder.build();
  }
}

