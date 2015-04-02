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

import com.google.appengine.api.memcache.AsyncMemcacheService;
import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.utils.FutureWrapper;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Level;

/**
 * Example asynchronous usage of App Engine Memcache.
 * AsyncMemcache wraps a "slow" map with the memcache service.
 */
public class AsyncMemcache {

  /**
   * the backing map for the memcache.
   */
  private Map<String, byte[]> map;

  /**
   * Singleton App Engine Memcache service.
   */
  private static AsyncMemcacheService asyncCache = null;

  /**
   * a Lock to ensure that asyncCache is a threadsafe singleton.
   */
  private static final Object MEMCACHE_LOCK = new Object();

  /**
   * @param slowMap a Map<String, byte[]> for which retrieval is quite expensive
   */
  public AsyncMemcache(final Map<String, byte[]> slowMap) {
    this.map = slowMap;
  }

  /**
   * @param key the String key used for lookup
   * @return a Future<byte[]> which can be used to retrieve the value
   **/
  public final Future<byte[]> get(final String key) {
    return new FutureWrapper<Object, byte[]>(asyncCache.get(key)) {

      @Override
      protected Throwable convertException(final Throwable arg0) {
        return arg0;
      }

      @Override
      protected byte[] wrap(final Object arg0) throws Exception {
        byte[] value = (byte[]) arg0;
        if (arg0 == null) {
          value = map.get(key);
          asyncCache.put(key, arg0);
        }
        return value;
      }
    };
  }
  /**
   * @return this instances singleton asyncCache service.
   */
  public final AsyncMemcacheService getService() {
    if (asyncCache == null) {
      synchronized (MEMCACHE_LOCK) {
        if (asyncCache == null) {
          asyncCache = MemcacheServiceFactory.getAsyncMemcacheService();
          asyncCache.setErrorHandler(
              ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
        }
      }
    }
    return asyncCache;
  }
}
