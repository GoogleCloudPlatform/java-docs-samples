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

import java.util.List;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

/**
 * An example Hibernate application using the Google Cloud Spanner Dialect for Hibernate ORM.
 */
public class HibernateSampleApplication {

  /**
   * Main method that runs a simple console application that saves a {@link Person} entity and then
   * retrieves it to print to the console.
   */
  public static void main(String[] args) {

    // Create Hibernate environment objects.
    StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
        .configure()
        .build();
    SessionFactory sessionFactory = new MetadataSources(registry).buildMetadata()
        .buildSessionFactory();
    Session session = sessionFactory.openSession();

    // Save an entity into Spanner Table.
    savePerson(session);

    session.close();
  }

  /**
   * Saves a {@link Person} entity into a Spanner table.
   */
  public static void savePerson(Session session) {
    session.beginTransaction();

    WireTransferPayment payment1 = new WireTransferPayment();
    payment1.setWireId("1234ab");
    payment1.setAmount(200L);

    CreditCardPayment payment2 = new CreditCardPayment();
    payment2.setCreditCardId("creditcardId");
    payment2.setAmount(600L);

    Person person = new Person();
    person.setName("person");
    person.setNickName("purson");
    person.setAddress("address");

    person.addPayment(payment1);
    person.addPayment(payment2);

    session.persist(person);
    session.getTransaction().commit();

    List<Person> personsInTable =
        session.createQuery("from Person", Person.class).list();

    System.out.printf("There are %d persons saved in the table:%n", personsInTable.size());

    for (Person personInTable : personsInTable) {
      System.out.println(personInTable);
    }
  }
}
