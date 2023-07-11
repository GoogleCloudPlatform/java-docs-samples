/*
 * Copyright 2022 Google LLC
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

package com.example.stitcher;

import com.google.cloud.video.stitcher.v1.CdnKey;
import com.google.cloud.video.stitcher.v1.ListCdnKeysRequest;
import com.google.cloud.video.stitcher.v1.ListLiveConfigsRequest;
import com.google.cloud.video.stitcher.v1.ListSlatesRequest;
import com.google.cloud.video.stitcher.v1.LiveConfig;
import com.google.cloud.video.stitcher.v1.LocationName;
import com.google.cloud.video.stitcher.v1.Slate;
import com.google.cloud.video.stitcher.v1.VideoStitcherServiceClient;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestUtils {

  public static final String LOCATION = "us-central1";
  public static final String SLATE_ID_PREFIX = "slate-";
  public static final String CDN_KEY_ID_PREFIX = "cdn-key-";
  public static final String LIVE_CONFIG_ID_PREFIX = "live-config-";

  public static final String HOSTNAME = "cdn.example.com";
  public static final String UPDATED_HOSTNAME = "updated.example.com";
  public static final String KEYNAME = "my-key"; // field in the CDN key
  public static final String CLOUD_CDN_PRIVATE_KEY = "VGhpcyBpcyBhIHRlc3Qgc3RyaW5nLg==";
  public static final String MEDIA_CDN_PRIVATE_KEY =
      "MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxzg5MDEyMzQ1Njc4OTAxMjM0NTY3DkwMTIzNA";
  public static final String AKAMAI_TOKEN_KEY = "VGhpcyBpcyBhIHRlc3Qgc3RyaW5nLg==";

  public static final String SLATE_URI =
      "https://storage.googleapis.com/cloud-samples-data/media/ForBiggerEscapes.mp4";
  public static final String LIVE_URI =
      "https://storage.googleapis.com/cloud-samples-data/media/hls-live/manifest.m3u8";
  // Single Inline Linear
  // (https://developers.google.com/interactive-media-ads/docs/sdks/html5/client-side/tags)
  public static final String LIVE_AD_TAG_URI =
      "https://pubads.g.doubleclick.net/gampad/ads?iu=/21775744923/external/single_ad_samples&sz=640x480&cust_params=sample_ct%3Dlinear&ciu_szs=300x250%2C728x90&gdfp_req=1&output=vast&unviewed_position_start=1&env=vp&impl=s&correlator=";
  public static final String VOD_URI =
      "https://storage.googleapis.com/cloud-samples-data/media/hls-vod/manifest.m3u8";
  // VMAP Pre-roll
  // (https://developers.google.com/interactive-media-ads/docs/sdks/html5/client-side/tags)
  public static final String VOD_AD_TAG_URI =
      "https://pubads.g.doubleclick.net/gampad/ads?iu=/21775744923/external/vmap_ad_samples&sz=640x480&cust_params=sample_ar%3Dpreonly&ciu_szs=300x250%2C728x90&gdfp_req=1&ad_rule=1&output=vmap&unviewed_position_start=1&env=vp&impl=s&correlator=";

  private static final int DELETION_THRESHOLD_TIME_HOURS_IN_SECONDS = 10800; // 3 hours

  // Clean up old test slates.
  public static void cleanStaleSlates(String projectId, String location) throws IOException {
    try (VideoStitcherServiceClient videoStitcherServiceClient =
        VideoStitcherServiceClient.create()) {
      ListSlatesRequest listSlatesRequest =
          ListSlatesRequest.newBuilder()
              .setParent(LocationName.of(projectId, location).toString())
              .build();

      VideoStitcherServiceClient.ListSlatesPagedResponse response =
          videoStitcherServiceClient.listSlates(listSlatesRequest);

      for (Slate slate : response.iterateAll()) {
        Matcher matcher = Pattern.compile(SLATE_ID_PREFIX).matcher(slate.getName());
        if (matcher.find()) {
          String createTime = slate.getName().substring(matcher.end()).trim();
          long createEpochSec = Long.parseLong(createTime);
          if (createEpochSec
              < Instant.now().getEpochSecond() - DELETION_THRESHOLD_TIME_HOURS_IN_SECONDS) {
            videoStitcherServiceClient.deleteSlateAsync(slate.getName()).get(2, TimeUnit.MINUTES);
          }
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  // Clean up old test CDN keys.
  public static void cleanStaleCdnKeys(String projectId, String location) throws IOException {
    try (VideoStitcherServiceClient videoStitcherServiceClient =
        VideoStitcherServiceClient.create()) {
      ListCdnKeysRequest listCdnKeysRequest =
          ListCdnKeysRequest.newBuilder()
              .setParent(LocationName.of(projectId, location).toString())
              .build();

      VideoStitcherServiceClient.ListCdnKeysPagedResponse response =
          videoStitcherServiceClient.listCdnKeys(listCdnKeysRequest);

      for (CdnKey cdnKey : response.iterateAll()) {
        Matcher matcher = Pattern.compile(CDN_KEY_ID_PREFIX).matcher(cdnKey.getName());
        if (matcher.find()) {
          String createTime = cdnKey.getName().substring(matcher.end()).trim();
          long createEpochSec = Long.parseLong(createTime);
          if (createEpochSec
              < Instant.now().getEpochSecond() - DELETION_THRESHOLD_TIME_HOURS_IN_SECONDS) {
            videoStitcherServiceClient.deleteCdnKeyAsync(cdnKey.getName()).get(2, TimeUnit.MINUTES);
          }
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  // Clean up old test live configs.
  public static void cleanStaleLiveConfigs(String projectId, String location) throws IOException {
    try (VideoStitcherServiceClient videoStitcherServiceClient =
        VideoStitcherServiceClient.create()) {
      ListLiveConfigsRequest listLiveConfigsRequest =
          ListLiveConfigsRequest.newBuilder()
              .setParent(LocationName.of(projectId, location).toString())
              .build();

      VideoStitcherServiceClient.ListLiveConfigsPagedResponse response =
          videoStitcherServiceClient.listLiveConfigs(listLiveConfigsRequest);

      for (LiveConfig liveConfig : response.iterateAll()) {
        Matcher matcher = Pattern.compile(LIVE_CONFIG_ID_PREFIX).matcher(liveConfig.getName());
        if (matcher.find()) {
          String createTime = liveConfig.getName().substring(matcher.end()).trim();
          long createEpochSec = Long.parseLong(createTime);
          if (createEpochSec
              < Instant.now().getEpochSecond() - DELETION_THRESHOLD_TIME_HOURS_IN_SECONDS) {
            videoStitcherServiceClient.deleteLiveConfigAsync(liveConfig.getName())
                .get(2, TimeUnit.MINUTES);
          }
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  // Finds the play URI in the given output.
  public static String getPlayUri(String output) {
    Matcher uriMatcher = Pattern.compile("Play URI: (.*)").matcher(output);
    String playUri = null;
    if (uriMatcher.find()) {
      playUri = uriMatcher.group(1);
    }
    return playUri;
  }

  // Connects to the play URI and returns the renditions information.
  public static String getRenditions(String playUri) throws IOException {
    URL url = new URL(playUri);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");
    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    String line;
    String renditions = null;
    while ((line = reader.readLine()) != null) {
      if (line.startsWith("renditions/")) {
        renditions = line;
        break;
      }
    }
    reader.close();
    return renditions;
  }

  // Connects to the renditions URI. This emulates a media player connecting to the API.
  public static void connectToRenditionsUrl(String renditionsUri) throws IOException {
    URL url = new URL(renditionsUri);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");
    connection.connect();
    connection.getInputStream();
  }

  // Get a slate ID that includes a creation timestamp. Add some randomness in case tests are run
  // in parallel.
  public static String getSlateId() {
    return String.format(
        "test-%s-%s%s",
        UUID.randomUUID().toString().substring(0, 15),
        SLATE_ID_PREFIX,
        Instant.now().getEpochSecond());
  }

  // Get a CDN key ID that includes a creation timestamp. Add some randomness in case tests are run
  // in parallel.
  public static String getCdnKeyId() {
    return String.format(
        "test-%s-%s%s",
        UUID.randomUUID().toString().substring(0, 15),
        CDN_KEY_ID_PREFIX,
        Instant.now().getEpochSecond());
  }

  // Get a live config ID that includes a creation timestamp. Add some randomness in case tests are
  // run in parallel.
  public static String getLiveConfigId() {
    return String.format(
        "test-%s-%s%s",
        UUID.randomUUID().toString().substring(0, 15),
        LIVE_CONFIG_ID_PREFIX,
        Instant.now().getEpochSecond());
  }
}
