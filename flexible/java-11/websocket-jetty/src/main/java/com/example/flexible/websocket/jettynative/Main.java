/*
 * Copyright 2023 Google LLC
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

package com.example.flexible.websocket.jettynative;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import org.apache.tomcat.util.scan.StandardJarScanFilter;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.apache.jsp.JettyJasperInitializer;
import org.eclipse.jetty.jsp.JettyJspServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;

/**
 * Starts up the server, including a DefaultServlet that handles static files, and any servlet
 * classes annotated with the @WebServlet annotation.
 */
public class Main {

  public static void main(String[] args) throws Exception {

    // Create a server that listens on port 8080.
    Server server = new Server(8080);
    WebAppContext webAppContext = new WebAppContext();
    server.setHandler(webAppContext);

    // Load static content from inside the jar file.
    URL webAppDir = Main.class.getClassLoader().getResource("WEB-INF/");
    System.out.println(webAppDir);
    webAppContext.setResourceBase(webAppDir.toURI().toString());

    // Enable annotations so the server sees classes annotated with @WebServlet.
    webAppContext.setConfigurations(
        new Configuration[] {
          new AnnotationConfiguration(), new WebInfConfiguration(),
        });

    webAppContext.setAttribute(
        "org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
        ".*/target/classes/|.*\\.jar");
    enableEmbeddedJspSupport(webAppContext);

    ServletHolder holderAltMapping = new ServletHolder();
    holderAltMapping.setName("index.jsp");
    holderAltMapping.setForcedPath("/index.jsp");
    webAppContext.addServlet(holderAltMapping, "/");

    // Start the server! 🚀
    server.start();
    System.out.println("Server started!");

    // Keep the main thread alive while the server is running.
    server.join();
  }

  private static void enableEmbeddedJspSupport(ServletContextHandler servletContextHandler)
      throws IOException {
    // Establish Scratch directory for the servlet context (used by JSP compilation)
    File tempDir = new File(System.getProperty("java.io.tmpdir"));
    File scratchDir = new File(tempDir.toString(), "embedded-jetty-jsp");

    if (!scratchDir.exists()) {
      if (!scratchDir.mkdirs()) {
        throw new IOException("Unable to create scratch directory: " + scratchDir);
      }
    }
    servletContextHandler.setAttribute("javax.servlet.context.tempdir", scratchDir);

    // Set Classloader of Context to be sane (needed for JSTL)
    // JSP requires a non-System classloader, this simply wraps the
    // embedded System classloader in a way that makes it suitable
    // for JSP to use
    ClassLoader jspClassLoader = new URLClassLoader(new URL[0], Main.class.getClassLoader());
    servletContextHandler.setClassLoader(jspClassLoader);

    // Manually call JettyJasperInitializer on context startup
    servletContextHandler.addBean(new JspStarter(servletContextHandler));

    // Create / Register JSP Servlet (must be named "jsp" per spec)
    ServletHolder holderJsp = new ServletHolder("jsp", JettyJspServlet.class);
    holderJsp.setInitOrder(0);
    holderJsp.setInitParameter("logVerbosityLevel", "DEBUG");
    holderJsp.setInitParameter("fork", "false");
    holderJsp.setInitParameter("xpoweredBy", "false");
    holderJsp.setInitParameter("compilerTargetVM", "1.8");
    holderJsp.setInitParameter("compilerSourceVM", "1.8");
    holderJsp.setInitParameter("keepgenerated", "true");
    servletContextHandler.addServlet(holderJsp, "*.jsp");
  }

  /**
   * JspStarter for embedded ServletContextHandlers
   *
   * <p>This is added as a bean that is a jetty LifeCycle on the ServletContextHandler. This bean's
   * doStart method will be called as the ServletContextHandler starts, and will call the
   * ServletContainerInitializer for the jsp engine.
   */
  public static class JspStarter extends AbstractLifeCycle
      implements ServletContextHandler.ServletContainerInitializerCaller {
    JettyJasperInitializer sci;
    ServletContextHandler context;

    public JspStarter(ServletContextHandler context) {
      this.sci = new JettyJasperInitializer();
      this.context = context;
      String skip = "apache-*,ecj-*,jetty-*,asm-*,javax.servlet-*"
          + "javax.annotation-*,taglibs-standard-spec-*,*.jar";
      StandardJarScanner jarScanner = new StandardJarScanner();
      StandardJarScanFilter jarScanFilter = new StandardJarScanFilter();
      jarScanFilter.setTldSkip(skip);
      jarScanner.setJarScanFilter(jarScanFilter);
      this.context.setAttribute("org.apache.tomcat.JarScanner", jarScanner);
    }

    @Override
    protected void doStart() throws Exception {
      ClassLoader old = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(context.getClassLoader());
      try {
        sci.onStartup(null, context.getServletContext());
        super.doStart();
      } finally {
        Thread.currentThread().setContextClassLoader(old);
      }
    }
  }
}
