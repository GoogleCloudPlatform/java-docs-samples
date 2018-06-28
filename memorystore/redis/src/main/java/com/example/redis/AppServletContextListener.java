/*
 * Copyright 2018 Google LLC
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
// [START memorystore_web_listener]

package com.example.redis;

import java.io.IOException;
import java.util.Properties;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@WebListener
public class AppServletContextListener implements ServletContextListener {

  private Properties config = new Properties();

  private JedisPool createJedisPool() throws IOException {
    String host;
    Integer port;
    config.load(
        Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("application.properties"));
    host = config.getProperty("redis.host");
    port = Integer.valueOf(config.getProperty("redis.port", "6379"));

    JedisPoolConfig poolConfig = new JedisPoolConfig();
    // Default : 8, consider how many concurrent connections into Redis you will need under load
    poolConfig.setMaxTotal(128);

    return new JedisPool(poolConfig, host, port);
  }

  @Override
  public void contextDestroyed(ServletContextEvent event) {
    JedisPool jedisPool = (JedisPool) event.getServletContext().getAttribute("jedisPool");
    if (jedisPool != null) {
      jedisPool.destroy();
      event.getServletContext().setAttribute("jedisPool", null);
    }
  }

  // Run this before web application is started
  @Override
  public void contextInitialized(ServletContextEvent event) {
    JedisPool jedisPool = (JedisPool) event.getServletContext().getAttribute("jedisPool");
    if (jedisPool == null) {
      try {
        jedisPool = createJedisPool();
        event.getServletContext().setAttribute("jedisPool", jedisPool);
      } catch (IOException e) {
        // handle exception
      }
    }
  }
}
// [END memorystore_web_listener]
