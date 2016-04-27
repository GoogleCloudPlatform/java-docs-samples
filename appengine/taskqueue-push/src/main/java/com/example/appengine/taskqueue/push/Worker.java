package com.example.appengine.taskqueue.push;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//  [START worker]
// The Worker servlet should be mapped to the "/worker" URL.
public class Worker extends HttpServlet {
  private static final Logger log = Logger.getLogger(Worker.class.getName());

  protected void doPost(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
    String key = request.getParameter("key");

    // Do something with key.
    // [START_EXCLUDE]
    log.info("Worker is processing " + key);
    // [END_EXCLUDE]
    
  }
}
// [END worker]
