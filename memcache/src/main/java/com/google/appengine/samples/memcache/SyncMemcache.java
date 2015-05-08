/* Copyright 2015 Google Inc.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.google.appengine.samples.memcache;

import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

import java.util.Map;
import java.util.logging.Level;

/**
 * Example synchronous usage of App Engine Memcache.
 * SyncMemcache wraps a "slow" map with the memcache service.
 */
public class SyncMemcache {

  /**
   * the backing map for the memcache.
   */
  private Map<String, byte[]> map;

  /**
   * Singleton App Engine Memcache service.
   */
  private static MemcacheService syncCache = null;

  /**
   * a Lock to ensure that syncCache is a threadsafe singleton.
   */
  private static final Object MEMCACHE_LOCK = new Object();

  /**
   * @param slowMap a Map<String, byte[]> for which retrieval is quite expensive
   */

  /**
   * @param slowMap a Map<String, byte[]> for which retrieval is quite expensive
   */
  public SyncMemcache(final Map<String, byte[]> slowMap) {
    this.map = slowMap;
  }

  /**
   * @param key the String key used for lookup
   * @return a byte[] representing the stored value
   **/
  public final byte[] get(final String key) {
    byte[] value = (byte[]) syncCache.get(key);
    if (value == null) {
      value = map.get(key);
      syncCache.put(key, value);
    }
    return value;
  }

  /**
   * @return this instances singleton syncCache service.
   */
  public final MemcacheService getService() {
    if (syncCache == null) {
      synchronized (MEMCACHE_LOCK) {
        if (syncCache == null) {
          syncCache = MemcacheServiceFactory.getMemcacheService();
          syncCache.setErrorHandler(
              ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
        }
      }
    }
    return syncCache;
  }
}
