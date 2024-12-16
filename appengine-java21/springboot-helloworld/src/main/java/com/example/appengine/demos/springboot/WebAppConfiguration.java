/*
 * Copyright 2024 Google Inc.
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
package com.example.appengine.demos.springboot;

import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

@Configuration
@EnableWebSecurity
public class WebAppConfiguration {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests((auth) -> auth
            .requestMatchers(new AntPathRequestMatcher("/admin/**")).hasRole("ADMIN")
            .anyRequest().permitAll()
        );
        http.formLogin(AbstractAuthenticationFilterConfigurer::permitAll);

        // Require HTTPS. Note doesn't work on DevAppServer over HTTP because done by string not security constraint.
        // http.requiresChannel(channelCustomizer -> channelCustomizer.anyRequest().requiresSecure());

        return http.build();
    }

    @Bean
    public ServletContextInitializer initializer() {
        return servletContext -> {
            servletContext.addListener(new ServletContextListener() {
                @Override
                public void contextInitialized(ServletContextEvent sce)
                {
                    System.err.println("contextInitialized: " + sce);
                }
            });
        };
    }
    }  
