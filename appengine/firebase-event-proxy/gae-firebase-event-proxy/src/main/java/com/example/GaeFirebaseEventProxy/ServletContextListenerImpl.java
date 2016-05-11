package com.example.GaeFirebaseEventProxy;

import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ServletContextListenerImpl implements ServletContextListener {

  private static final Logger log = Logger.getLogger(ServletContextListener.class.getName());

  @Override
  public void contextInitialized(ServletContextEvent event) {
    log.info("Starting ....");
    FirebaseEventProxy proxy = new FirebaseEventProxy();
    proxy.start();
  }

  @Override
  public void contextDestroyed(ServletContextEvent event) {
    // App Engine does not currently invoke this method.
  }
}
