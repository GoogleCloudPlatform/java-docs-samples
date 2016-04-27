package com.example.appengine.taskqueue.push;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// [START enqueue]
// The Enqueue servlet should be mapped to the "/enqueue" URL.
public class Enqueue extends HttpServlet {
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
    String key = request.getParameter("key");

    // Add the task to the default queue.
    Queue queue = QueueFactory.getDefaultQueue();
    queue.add(TaskOptions.Builder.withUrl("/worker").param("key", key));

    response.sendRedirect("/");
  }
}
// [END enqueue]
