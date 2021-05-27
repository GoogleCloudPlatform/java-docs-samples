/*
 * Copyright 2021 Google Inc.
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

package com.example.spanner.r2dbc;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class WebController {

  @Autowired
  R2dbcEntityTemplate r2dbcEntityTemplate;


  @PostMapping("createTable")
  public Mono<String> createTable() {
    return r2dbcEntityTemplate.getDatabaseClient()
        .sql("CREATE TABLE NAMES "
            + "(UUID STRING(36), NAME STRING(60) NOT NULL) "
            + "PRIMARY KEY (UUID)")
        .fetch()
        .rowsUpdated()
        .map(numRows -> "table NAMES created successfully")
        .onErrorResume(error -> Mono.just("table creation failed: " + error.getMessage()));
  }

  @PostMapping("dropTable")
  public Mono<String> dropTable() {
    return r2dbcEntityTemplate.getDatabaseClient().sql("DROP TABLE NAMES")
        .fetch().rowsUpdated().map(numRows -> "table NAMES dropped successfully")
        .onErrorResume(error -> Mono.just("table deletion failed: " + error.getMessage()));
  }

  @GetMapping("listRows")
  public Flux<Name> listRows() {
    return r2dbcEntityTemplate.select(Name.class)
        .all();
  }

  @PostMapping("addRow")
  public Mono<String> addRow(@RequestBody String newName) {
    return r2dbcEntityTemplate.insert(new Name(UUID.randomUUID().toString(), newName))
      .map(numRows -> "row inserted successfully")
        .onErrorResume(error -> Mono.just("row insertion failed: " + error.getMessage()));
  }

  @PostMapping("deleteRow")
  public Mono<String> deleteRow(@RequestBody String uuid) {
    return r2dbcEntityTemplate.delete(Name.class).matching(query(where("uuid").is(uuid)))
        .all()
        .map(numDeleted -> numDeleted > 0 ? "row deleted successfully" : "row did not exist")
        .onErrorResume(error -> Mono.just("row deletion failed: " + error.getMessage()));
  }
}
