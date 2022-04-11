// Copyright 2022 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package compute.windows.windowsinstances;
// [START compute_get_instance_serial_port]

import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.SerialPortOutput;
import java.io.IOException;

public class GetInstanceSerialPort {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    // projectId - ID or number of the project you want to use.
    String projectId = "your-google-cloud-project-id";

    // zone - Name of the zone you want to check, for example: us-west3-b
    String zone = "europe-central2-b";

    // instanceName - Name of the instance you want to check.
    String instanceName = "instance-name";

    getInstanceSerialPort(projectId, zone, instanceName);
  }

  // Prints an instance serial port output.
  public static void getInstanceSerialPort(String projectId, String zone, String instanceName)
      throws IOException {

    try (InstancesClient instancesClient = InstancesClient.create()) {

      SerialPortOutput serialPortOutput = instancesClient.getSerialPortOutput(projectId, zone,
          instanceName);

      System.out.printf("Output from instance serial port %s", serialPortOutput.getContents());
    }
  }
}
// [END compute_get_instance_serial_port]