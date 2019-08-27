/*
 * Copyright 2019 Google LLC
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA
 */

package com.example.spanner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.hibernate.annotations.Type;

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
  @Type(type = "uuid-char")
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
