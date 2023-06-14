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

package com.example.cloudrun;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.codec.CodecCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.CodecConfigurer;

import io.cloudevents.spring.http.CloudEventHttpUtils;
import io.cloudevents.spring.webflux.CloudEventHttpMessageReader;
import io.cloudevents.spring.webflux.CloudEventHttpMessageWriter;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import io.cloudevents.CloudEvent;


@SpringBootApplication
@RestController
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

	/**
	 * Configure an HTTP reader and writer so that we can process CloudEvents over
     * HTTP via Spring Webflux.
	 */
	@Configuration
	public static class CloudEventHandlerConfiguration implements CodecCustomizer {

		@Override
		public void customize(CodecConfigurer configurer) {
			configurer.customCodecs().register(new CloudEventHttpMessageReader());
			configurer.customCodecs().register(new CloudEventHttpMessageWriter());
		}

	}

  @PostMapping("/")
  ResponseEntity<String> handlehttp(@RequestBody String body, @RequestHeader HttpHeaders headers) throws Exception {
	CloudEvent cloudEvent;
	try {
	cloudEvent = CloudEventHttpUtils.fromHttp(headers).withData(body.getBytes()).build();
	} 
	catch(Exception e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
	}

    // CloudEvent information
    System.out.println("Id: " + cloudEvent.getId());
    System.out.println("Source: " + cloudEvent.getSource());
    System.out.println("Type: " + cloudEvent.getType());

    // String json = new String(cloudEvent.getData().toBytes());
    // LogEntryData.Builder builder = LogEntryData.newBuilder();
    // JsonFormat.parser().merge(json, builder);
    // LogEntryData data = builder.build();

    // // Audit log data
    // logger.info("ProtoPayload: " + data.getProtoPayload());
    // logger.info("ServiceName: " + data.getProtoPayload().getServiceName());
    // logger.info("MethodName: " + data.getProtoPayload().getMethodName());
    // logger.info("ResourceName: " + data.getProtoPayload().getResourceName());

    return ResponseEntity.ok().body("okay");
  }
//   @PostMapping("/")
  ResponseEntity<String> handleCloudEvent(@RequestBody CloudEvent cloudEvent) throws Exception {

    // CloudEvent information
    System.out.println("Id: " + cloudEvent.getId());
    System.out.println("Source: " + cloudEvent.getSource());
    System.out.println("Type: " + cloudEvent.getType());

    // String json = new String(cloudEvent.getData().toBytes());
    // LogEntryData.Builder builder = LogEntryData.newBuilder();
    // JsonFormat.parser().merge(json, builder);
    // LogEntryData data = builder.build();

    // // Audit log data
    // logger.info("ProtoPayload: " + data.getProtoPayload());
    // logger.info("ServiceName: " + data.getProtoPayload().getServiceName());
    // logger.info("MethodName: " + data.getProtoPayload().getMethodName());
    // logger.info("ResourceName: " + data.getProtoPayload().getResourceName());

    return ResponseEntity.ok().body("okay");
  }
}
// [END eventarc_storage_server]
