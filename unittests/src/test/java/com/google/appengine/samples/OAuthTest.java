/*
 * Copyright 2015 Google Inc. All Rights Reserved.
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

package com.google.appengine.samples;

// [START OAuthTest]

import static org.junit.Assert.assertEquals;

import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.oauth.OAuthService;
import com.google.appengine.api.oauth.OAuthServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OAuthTest {
  private static final String OAUTH_CONSUMER_KEY = "notexample.com";
  private static final String OAUTH_EMAIL = "bozo@clown.com";
  private static final String OAUTH_USER_ID = "bozo";
  private static final String OAUTH_AUTH_DOMAIN = "clown.com";
  private static final boolean OAUTH_IS_ADMIN = true;

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalUserServiceTestConfig()
          .setOAuthConsumerKey(OAUTH_CONSUMER_KEY)
          .setOAuthEmail(OAUTH_EMAIL)
          .setOAuthUserId(OAUTH_USER_ID)
          .setOAuthAuthDomain(OAUTH_AUTH_DOMAIN)
          .setOAuthIsAdmin(OAUTH_IS_ADMIN));

  @Before
  public void setUp() {
    helper.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void testConfig() throws OAuthRequestException {
    OAuthService oauthService = OAuthServiceFactory.getOAuthService();
    assertEquals(OAUTH_CONSUMER_KEY, oauthService.getOAuthConsumerKey());
    assertEquals(new User(OAUTH_EMAIL, OAUTH_AUTH_DOMAIN, OAUTH_USER_ID),
        oauthService.getCurrentUser());
    assertEquals(OAUTH_IS_ADMIN, oauthService.isUserAdmin());
  }
}

// [END OAuthTest]
