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

import java.util.List;
import java.util.logging.Logger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;

import io.cloudevents.spring.http.CloudEventHttpUtils;
import io.cloudevents.spring.mvc.CloudEventHttpMessageConverter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.google.events.cloud.storage.v1.StorageObjectData;
import com.google.protobuf.util.JsonFormat;

import io.cloudevents.CloudEvent;
import io.cloudevents.rw.CloudEventRWException;

@SpringBootApplication
@RestController
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Configuration
	public static class CloudEventHandlerConfiguration implements WebMvcConfigurer {

		@Override
		public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
			converters.add(0, new CloudEventHttpMessageConverter());
		}

	}

	@RequestMapping(value = "/", method = RequestMethod.POST, consumes = "application/json")
	ResponseEntity<String> handleCloudEvent(@RequestBody CloudEvent cloudEvent) throws Exception {

		// CloudEvent information
		System.out.println("Id: " + cloudEvent.getId());
		System.out.println("Source: " + cloudEvent.getSource());
		System.out.println("Type: " + cloudEvent.getType());

		String json = new String(cloudEvent.getData().toBytes());
		StorageObjectData.Builder builder = StorageObjectData.newBuilder();
		JsonFormat.parser().merge(json, builder);
		StorageObjectData data = builder.build();

		StringBuilder mb = new StringBuilder();
		mb.append(
			String.format("Cloud Storage object changed: %s/%s modified at %s\n",
			data.getBucket(), data.getName(), data.getUpdated()));

		return ResponseEntity.ok().body(mb.toString());
	}

	// Handle Errors by mapping Exceptions to HTTP Responses.
	@ExceptionHandler({ IllegalStateException.class, CloudEventRWException.class })
	@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Invalid CloudEvent received")
	public void noop() {
	}
}
// [END eventarc_storage_server]
