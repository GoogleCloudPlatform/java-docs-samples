package com.example.appengine.pubsub;

import com.google.cloud.pubsub.Message;
import com.google.cloud.pubsub.PubSub;
import com.google.cloud.pubsub.PubSubOptions;
import com.google.cloud.pubsub.Topic;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "PubSubPublish", value = "/publish")
public class PubSubPublish extends HttpServlet {
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException,
      ServletException {
    // Load environment variables
    final String topicName = System.getenv("PUBSUB_TOPIC");
    final String payload = req.getParameter("payload");

    // Create a PubSub Service and get Topic
    PubSub pubsub = PubSubOptions.getDefaultInstance().getService();
    Topic topic = pubsub.getTopic(topicName);

    // Publish Message
    topic.publish(Message.of(payload));

    // redirect "/"
    resp.sendRedirect("/");
  }
}
