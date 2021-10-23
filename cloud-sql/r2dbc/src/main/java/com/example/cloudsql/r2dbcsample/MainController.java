/*
 * Copyright 2020 Google LLC
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

package com.example.cloudsql.r2dbcsample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.thymeleaf.spring5.context.webflux.IReactiveDataDriverContextVariable;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;

@Controller
public class MainController {

  @Autowired
  private VoteRepository voteRepository;

  @RequestMapping("/")
  public String index(final Model model) {
    IReactiveDataDriverContextVariable votes =
        new ReactiveDataDriverContextVariable(voteRepository.findAll(), 1);
    model.addAttribute("votes", votes);

    model.addAttribute("tabCount", voteRepository.countWhereCandidateEquals("TABS"));
    model.addAttribute("spaceCount", voteRepository.countWhereCandidateEquals("SPACES"));

    return "index";
  }
}
