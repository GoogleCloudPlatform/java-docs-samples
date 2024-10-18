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

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.Assert.assertNotNull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.google.cloud.securitycentermanagement.v1.SecurityHealthAnalyticsCustomModule;

import io.opentelemetry.api.internal.StringUtils;

@RunWith(JUnit4.class)
public class SecurityHealthAnalyticsCustomModuleTest {

	private static final String ORGANIZATION_ID = System.getenv("ORGANIZATION_ID");
	private static final String LOCATION = "global";
	private static final String CUSTOM_MODULE_DISPLAY_NAME = "custom_module_test";
	private static ByteArrayOutputStream stdOut;

	// Check if the required environment variables are set.
	public static void requireEnvVar(String envVarName) {
		assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
				.that(System.getenv(envVarName)).isNotEmpty();
	}

	@BeforeClass
	public static void setUp() {
		final PrintStream out = System.out;
		stdOut = new ByteArrayOutputStream();
		System.setOut(new PrintStream(stdOut));

		requireEnvVar("ORGANIZATION_ID");

		stdOut = null;
		System.setOut(out);
	}

	@AfterClass
	public static void cleanUp() {
		stdOut = null;
		System.setOut(null);
	}

	@Before
	public void beforeEach() {
		stdOut = new ByteArrayOutputStream();
		System.setOut(new PrintStream(stdOut));
	}

	@After
	public void afterEach() {
		stdOut = null;
		System.setOut(null);
	}

	// extractCustomModuleID extracts the custom module Id from the full name
	public static String extractCustomModuleId(String customModuleFullName) {
		if (!StringUtils.isNullOrEmpty(customModuleFullName)) {
			String[] result = customModuleFullName.split("/");
			if (result.length > 0) {
				return result[result.length - 1];
			}
		}
		return "";
	}

	// createCustomModule method is for creating the custom module
	public static SecurityHealthAnalyticsCustomModule createCustomModule(String parent, String customModuleDisplayName)
			throws IOException {
		if (!StringUtils.isNullOrEmpty(parent) && !StringUtils.isNullOrEmpty(customModuleDisplayName)) {
			SecurityHealthAnalyticsCustomModule response = CreateSecurityHealthAnalyticsCustomModule
					.createSecurityHealthAnalyticsCustomModule(parent, customModuleDisplayName);
			return response;
		}
		return null;
	}

	// deleteCustomModule method is for deleting the custom module
	public static void deleteCustomModule(String parent, String customModuleId) throws IOException {
		if (!StringUtils.isNullOrEmpty(parent) && !StringUtils.isNullOrEmpty(customModuleId)) {
			DeleteSecurityHealthAnalyticsCustomModule.deleteSecurityHealthAnalyticsCustomModule(parent, customModuleId);
		}
	}

	@Test
	public void testCreateSecurityHealthAnalyticsCustomModule() throws IOException {

		String parent = String.format("organizations/%s/locations/%s", ORGANIZATION_ID, LOCATION);

		// creating the custom module
		SecurityHealthAnalyticsCustomModule response = CreateSecurityHealthAnalyticsCustomModule
				.createSecurityHealthAnalyticsCustomModule(parent, CUSTOM_MODULE_DISPLAY_NAME);

		// assert that response is not null
		assertNotNull(response);

		// assert that created module display name is matching with the name passed
		assertThat(response.getDisplayName()).isEqualTo(CUSTOM_MODULE_DISPLAY_NAME);

		// extracting the custom module id from the full name
		String customModuleId = extractCustomModuleId(response.getName());

		// deleting the custom module for cleanup
		deleteCustomModule(parent, customModuleId);
	}

	@Test
	public void testDeleteSecurityHealthAnalyticsCustomModule() throws IOException {

		String parent = String.format("organizations/%s/locations/%s", ORGANIZATION_ID, LOCATION);

		// create the custom module
		SecurityHealthAnalyticsCustomModule response = createCustomModule(parent, CUSTOM_MODULE_DISPLAY_NAME);

		// extracting the custom module id from the full name
		String customModuleId = extractCustomModuleId(response.getName());

		// delete the custom module with the custom module id
		DeleteSecurityHealthAnalyticsCustomModule.deleteSecurityHealthAnalyticsCustomModule(parent, customModuleId);

		// assert that std output is matching with the string passed
		assertThat(stdOut.toString()).contains("SecurityHealthAnalyticsCustomModule deleted : " + customModuleId);
	}
}
