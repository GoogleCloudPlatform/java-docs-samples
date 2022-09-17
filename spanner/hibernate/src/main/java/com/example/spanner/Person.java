/*
 * Copyright 2019 Google Inc.
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

package com.example.spanner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;

/**
 * An example person entity.
 */
@Entity(name = "Person")
// [START spanner_hibernate_table_name]
@Table(name = "PersonsTable")
// [END spanner_hibernate_table_name]
public class Person {

  // [START spanner_hibernate_generated_ids]
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @JdbcTypeCode(java.sql.Types.VARCHAR)
  private UUID id;
  // [END spanner_hibernate_generated_ids]

  private String name;

  private String nickname;

  private String address;

  // An example of an entity relationship.
  @OneToMany(cascade = CascadeType.ALL)
  private List<Payment> payments = new ArrayList<>();

  public Person() {}

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getNickName() {
    return nickname;
  }

  public void setNickName(String nickname) {
    this.nickname = nickname;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public List<Payment> getPayments() {
    return payments;
  }

  public void addPayment(Payment payment) {
    this.payments.add(payment);
  }

  @Override
  public String toString() {
    return "Person{"
        + "\n id=" + id
        + "\n name='" + name + '\''
        + "\n nickname='" + nickname + '\''
        + "\n address='" + address + '\''
        + "\n total_payments=" + payments.stream().mapToLong(Payment::getAmount).sum()
        + "\n}";
  }
}
