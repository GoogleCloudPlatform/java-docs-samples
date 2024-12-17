/*
 * Copyright 2024 Google LLC
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

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.google.appengine.api.datastore.DatastoreService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author ludo
 */
@Controller
@Slf4j
public class HelloworldController {

    /** Do something with the app engine datastore... */
    private final DatastoreService datastoreService;

    /**
     *
     * @param datastoreService
     */
    @Autowired
    public HelloworldController(DatastoreService datastoreService) {
        this.datastoreService = Objects.requireNonNull(datastoreService);
    }

    /**
     * Home page -> {@literal /WEB-INF/jsp/index.jsp}
     * 
     * @return home page view.
     */
    @RequestMapping("/")
    public ModelAndView home() {
        LOGGER.info("Loading home page...");
        return new ModelAndView("index");
    }

    /**
     * Sample JSON/HTTP service.
     * 
     * @return a list of aliens.
     */
    @RequestMapping(value = "/aliens", method = RequestMethod.GET)
    @ResponseBody
    public AliensResponse aliens() {
        LOGGER.info("Returning a static list of aliens...");
        return AliensResponse.of(Arrays.asList(Alien.of("E.T.", "Home"), Alien.of("Marvin the Martian", "Mars")));
    }

    /**
     * /admin endpoint handler
     * @param request Servlet request
     * @return
     */
    @RequestMapping(value = "/admin", method = RequestMethod.GET)
    @ResponseBody
    public AliensResponse admin(final HttpServletRequest request) {
        LOGGER.info("Returning the admin info...");
        Principal userPrincipal = request.getUserPrincipal();
        return AliensResponse.of(
            Collections.singletonList(Alien.of("userPrincipal", userPrincipal == null ? "null" : userPrincipal.toString()))
        );
    }

    @Data
    @AllArgsConstructor(staticName = "of")
    static class AliensResponse {
        @NonNull
        private final List<Alien> aliens;
    }

    @Data
    @AllArgsConstructor(staticName = "of")
    // Only used by Jackson through reflection
    @NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
    static class Alien {
        @NonNull
        @NotEmpty
        private final String name;
        @NonNull
        @NotEmpty
        private final String home;
    }

}
