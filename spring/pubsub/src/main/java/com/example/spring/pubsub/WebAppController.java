/*
 *  Copyright 2017 original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.example.spring.pubsub;

import com.example.spring.pubsub.PubsubApplication.PubsubOutboundGateway;
import com.google.pubsub.v1.Subscription;
import com.google.pubsub.v1.SubscriptionName;
import com.google.pubsub.v1.Topic;
import com.google.pubsub.v1.TopicName;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gcp.pubsub.PubsubAdmin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
public class WebAppController {

  @Autowired
  private PubsubOutboundGateway messagingGateway;

  @Autowired
  private PubsubAdmin admin;

  /**
   * Lists every topic in the project.
   *
   * @return a list of the names of every topic in the project
   */
  @GetMapping("/listTopics")
  public List<String> listTopics() {
    return admin
        .listTopics()
        .stream()
        .map(Topic::getNameAsTopicName)
        .map(TopicName::getTopic)
        .collect(Collectors.toList());
  }

  /**
   * Lists every subscription in the project.
   *
   * @return a list of the names of every subscription in the project
   */
  @GetMapping("/listSubscriptions")
  public List<String> listSubscriptions() {
    return admin
        .listSubscriptions()
        .stream()
        .map(Subscription::getNameAsSubscriptionName)
        .map(SubscriptionName::getSubscription)
        .collect(Collectors.toList());
  }

  /**
   * Posts a message to a Google Cloud Pub/Sub topic, through Spring's messaging gateway, and
   * redirects the user to the home page.
   *
   * @param message the message posted to the Pub/Sub topic
   */
  @PostMapping("/postMessage")
  public RedirectView addMessage(@RequestParam("message") String message) {
    messagingGateway.sendToPubsub(message);
    return new RedirectView("/");
  }

  /**
   * Creates a new topic on Google Cloud Pub/Sub, through Spring's Pub/Sub admin class, and
   * redirects the user to the home page.
   *
   * @param topicName the name of the new topic
   */
  @PostMapping("/newTopic")
  public RedirectView newTopic(@RequestParam("name") String topicName) {
    admin.createTopic(topicName);
    return new RedirectView("/");
  }

  /**
   * Creates a new subscription on Google Cloud Pub/Sub, through Spring's Pub/Sub admin class, and
   * redirects the user to the home page.
   *
   * @param topicName the name of the new subscription
   */
  @PostMapping("/newSubscription")
  public RedirectView newSubscription(
      @RequestParam("name") String subscriptionName, @RequestParam("topic") String topicName) {
    admin.createSubscription(subscriptionName, topicName);
    return new RedirectView("/");
  }
}
