package com.google.appengine.samples.memcache;

import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

import java.util.logging.Level;

/**
 * Example synchronous usage of App Engine Memcache.
 * SyncMemcache wraps a "slow" map with the memcache service.
 */
public class ThreadsafeMemcacheUpdater {

  /**
   * class wrapper for byte[] -> byte[] update function.
   */
  public abstract class Updater {

    /**
     * @param value byte[] the old value
     * @return byte[] the new value
     */
    public abstract byte[] update(byte[] value);

  }

  /**
   * Singleton App Engine Memcache service.
   */
  private static MemcacheService syncCache = null;

  /**
   * a Lock to ensure that syncCache is a threadsafe singleton.
   */
  private static final Object MEMCACHE_LOCK = new Object();

  /**
   * Empty constructor.
   */
  public ThreadsafeMemcacheUpdater() {
  }

  /**
   * @param key the String identifying which value to update
   * @param update an Update wrapper which represents the function to apply
   * @return boolean indicating if the value was successfully updated
   */
  public final boolean update(final String key, final Updater update) {
    MemcacheService.IdentifiableValue oldValue = syncCache.getIdentifiable(key);
    if (oldValue == null) {
      syncCache.put(key, update.update(null));
      return true;
    } else {
      return syncCache.putIfUntouched(key,
          oldValue,
          update.update((byte[]) oldValue.getValue()));
    }
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
