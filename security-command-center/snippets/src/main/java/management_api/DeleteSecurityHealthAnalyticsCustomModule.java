/*
 * Copyright 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package management_api;

import java.io.IOException;

import com.google.cloud.securitycentermanagement.v1.DeleteSecurityHealthAnalyticsCustomModuleRequest;
import com.google.cloud.securitycentermanagement.v1.SecurityCenterManagementClient;

//[START securitycenter_management_api_delete_security_health_analytics_custom_module]

public class DeleteSecurityHealthAnalyticsCustomModule {

	public static void main(String[] args) throws IOException {

		// parent: Use any one of the following options:
		// - organizations/{organization_id}/locations/{location_id}
		// - folders/{folder_id}/locations/{location_id}
		// - projects/{project_id}/locations/{location_id}
		String parent = String.format("organizations/%s/locations/%s", "organization_id", "global");

		// custom module id, replace it with your custom module ID
		String customModuleId = "custom_module_id";

		deleteSecurityHealthAnalyticsCustomModule(parent, customModuleId);

	}

	// Delete a custom module with custom module Id under the security health
	// analytics service.
	public static void deleteSecurityHealthAnalyticsCustomModule(String parent, String customModuleId)
			throws IOException {

		// Initialize client that will be used to send requests. This client only needs
		// to be created
		// once, and can be reused for multiple requests.
		try (SecurityCenterManagementClient client = SecurityCenterManagementClient.create()) {
			String name = String.format("%s/securityHealthAnalyticsCustomModules/%s", parent, customModuleId);

			// create the request
			DeleteSecurityHealthAnalyticsCustomModuleRequest request = DeleteSecurityHealthAnalyticsCustomModuleRequest
					.newBuilder().setName(name).build();

			// calls the API
			client.deleteSecurityHealthAnalyticsCustomModule(request);

			System.out.printf("SecurityHealthAnalyticsCustomModule deleted : %s", customModuleId);
		}
	}
}
//[END securitycenter_management_api_delete_security_health_analytics_custom_module]