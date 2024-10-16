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
import static org.mockito.Mockito.mock;

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
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.google.cloud.securitycentermanagement.v1.CreateSecurityHealthAnalyticsCustomModuleRequest;
import com.google.cloud.securitycentermanagement.v1.CustomConfig;
import com.google.cloud.securitycentermanagement.v1.SecurityCenterManagementClient;
import com.google.cloud.securitycentermanagement.v1.SecurityHealthAnalyticsCustomModule;
import com.google.cloud.securitycentermanagement.v1.CustomConfig.ResourceSelector;
import com.google.cloud.securitycentermanagement.v1.CustomConfig.Severity;
import com.google.cloud.securitycentermanagement.v1.SecurityHealthAnalyticsCustomModule.EnablementState;
import com.google.type.Expr;

@RunWith(JUnit4.class)
public class SecurityHealthAnalyticsCustomModuleTest {

	private static final String ORGANIZATION_ID = "organization_id";
	private static final String LOCATION = "global";
	private static final String customModuleDisplayName = "custom_module_display_name";
	private static ByteArrayOutputStream stdOut;

	@BeforeClass
	public static void setUp() {
		final PrintStream out = System.out;
		stdOut = new ByteArrayOutputStream();
		System.setOut(new PrintStream(stdOut));
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

	@Test
	public void testCreateSecurityHealthAnalyticsCustomModule() throws IOException {
		// Mocking and test data setup.
		SecurityCenterManagementClient client = mock(SecurityCenterManagementClient.class);
		try (MockedStatic<SecurityCenterManagementClient> clientMock = Mockito
				.mockStatic(SecurityCenterManagementClient.class)) {
			clientMock.when(SecurityCenterManagementClient::create).thenReturn(client);

			String parent = String.format("organizations/%s/locations/%s", ORGANIZATION_ID, LOCATION);
			String name = String.format("%s/securityHealthAnalyticsCustomModules/%s", parent, "custom_module");

			Expr expr = Expr.newBuilder()
					.setExpression("has(resource.rotationPeriod) && (resource.rotationPeriod > duration('2592000s'))")
					.build();

			ResourceSelector resourceSelector = ResourceSelector.newBuilder()
					.addResourceTypes("cloudkms.googleapis.com/CryptoKey").build();

			// Building CustomConfig
			CustomConfig customConfig = CustomConfig.newBuilder().setPredicate(expr)
					.setResourceSelector(resourceSelector).setSeverity(Severity.MEDIUM)
					.setDescription("add your description here").setRecommendation("add your recommendation here").build();

			// Building SecurityHealthAnalyticsCustomModule
			SecurityHealthAnalyticsCustomModule expectedResult = SecurityHealthAnalyticsCustomModule.newBuilder()
					.setName(name).setDisplayName(customModuleDisplayName).setEnablementState(EnablementState.ENABLED)
					.setCustomConfig(customConfig).build();

			// Building CreateSecurityHealthAnalyticsCustomModuleRequest
			CreateSecurityHealthAnalyticsCustomModuleRequest request = CreateSecurityHealthAnalyticsCustomModuleRequest
					.newBuilder().setParent(parent).setSecurityHealthAnalyticsCustomModule(expectedResult).build();

			// Mocking createSecurityHealthAnalyticsCustomModule
			Mockito.when(client.createSecurityHealthAnalyticsCustomModule(request)).thenReturn(expectedResult);

			// Calling createSecurityHealthAnalyticsCustomModule
			SecurityHealthAnalyticsCustomModule response = CreateSecurityHealthAnalyticsCustomModule
					.createSecurityHealthAnalyticsCustomModule(parent, customModuleDisplayName);

			// Verifying createSecurityHealthAnalyticsCustomModule was called
			Mockito.verify(client).createSecurityHealthAnalyticsCustomModule(request);

			// Asserts the created SecurityHealthAnalyticsCustomModule matches the expected
			// request.
			assertThat(response).isEqualTo(expectedResult);
		}
	}
}
