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

    session.save(person);
    session.getTransaction().commit();

    List<Person> personsInTable =
        session.createQuery("from Person", Person.class).list();

    System.out.println(
        String.format("There are %d persons saved in the table:", personsInTable.size()));

    for (Person personInTable : personsInTable) {
      System.out.println(personInTable);
    }
  }
}
