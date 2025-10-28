/*
 * Copyright 2016 Google LLC
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

package com.example.appengine;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.Assert.fail;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.KeyRange;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests to demonstrate App Engine Datastore entities. */
@RunWith(JUnit4.class)
public class EntitiesTest {

  // Set no eventual consistency, that way queries return all results.
  // https://cloud.google.com/appengine/docs/java/tools/localunittesting
  // #Java_Writing_High_Replication_Datastore_tests
  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          new LocalDatastoreServiceTestConfig()
              .setDefaultHighRepJobPolicyUnappliedJobPercentage(0));

  private DatastoreService datastore;

  @Before
  public void setUp() {
    helper.setUp();
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void kindExample_writesEntity() throws Exception {
    // [START gae_java21_datastore_kind_example]
    Entity employee = new Entity("Employee", "asalieri");
    employee.setProperty("firstName", "Antonio");
    employee.setProperty("lastName", "Salieri");
    employee.setProperty("hireDate", new Date());
    employee.setProperty("attendedHrTraining", true);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(employee);
    // [END gae_java21_datastore_kind_example]

    Entity got = datastore.get(employee.getKey());
    assertWithMessage("got.firstName")
        .that((String) got.getProperty("firstName"))
        .isEqualTo("Antonio");
    assertWithMessage("got.lastName")
        .that((String) got.getProperty("lastName"))
        .isEqualTo("Salieri");
    assertWithMessage("got.hireDate").that((Date) got.getProperty("hireDate")).isNotNull();
    assertWithMessage("got.attendedHrTraining")
        .that((boolean) got.getProperty("attendedHrTraining"))
        .isTrue();
  }

  @Test
  public void identifiers_keyName_setsKeyName() throws Exception {
    // [START gae_java21_datastore_identifiers_1]
    Entity employee = new Entity("Employee", "asalieri");
    // [END gae_java21_datastore_identifiers_1]
    datastore.put(employee);

    assertWithMessage("key name").that(employee.getKey().getName()).isEqualTo("asalieri");
  }

  @Test
  public void identifiers_autoId_setsUnallocatedId() throws Exception {
    KeyRange keys = datastore.allocateIds("Employee", 1);
    long usedId = keys.getStart().getId();

    // [START gae_java21_datastore_identifiers_2]
    Entity employee = new Entity("Employee");
    // [END gae_java21_datastore_identifiers_2]
    datastore.put(employee);

    assertWithMessage("key id").that(employee.getKey().getId()).isNotEqualTo(usedId);
  }

  @Test
  public void parent_withinEntityConstructor_setsParent() throws Exception {
    // [START gae_java21_datastore_parent_1]
    Entity employee = new Entity("Employee");
    datastore.put(employee);

    Entity address = new Entity("Address", employee.getKey());
    datastore.put(address);
    // [END gae_java21_datastore_parent_1]

    assertWithMessage("address parent").that(address.getParent()).isEqualTo(employee.getKey());
  }

  @Test
  public void parent_withKeyName_setsKeyName() throws Exception {
    Entity employee = new Entity("Employee");
    datastore.put(employee);

    // [START gae_java21_datastore_parent_2]
    Entity address = new Entity("Address", "addr1", employee.getKey());
    // [END gae_java21_datastore_parent_2]
    datastore.put(address);

    assertWithMessage("address key name").that(address.getKey().getName()).isEqualTo("addr1");
  }

  @Test
  public void datastoreServiceFactory_returnsDatastoreService() throws Exception {
    // [START gae_java21_datastore_working_with_entities]
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    // [END gae_java21_datastore_working_with_entities]
    assertWithMessage("datastore").that(datastore).isNotNull();
  }

  @Test
  public void creatingAnEntity_withKeyName_writesEntity() throws Exception {
    // [START gae_java21_datastore_creating_an_entity_1]
    Entity employee = new Entity("Employee", "asalieri");
    // Set the entity properties.
    // ...
    datastore.put(employee);
    // [END gae_java21_datastore_creating_an_entity_1]

    assertWithMessage("employee key name").that(employee.getKey().getName()).isEqualTo("asalieri");
  }

  private Key writeEmptyEmployee() {
    // [START gae_java21_datastore_creating_an_entity_2]
    Entity employee = new Entity("Employee");
    // Set the entity properties.
    // ...
    datastore.put(employee);
    // [END gae_java21_datastore_creating_an_entity_2]
    return employee.getKey();
  }

  @Test
  public void creatingAnEntity_withoutKeyName_writesEntity() throws Exception {
    Key employeeKey = writeEmptyEmployee();
    // [START gae_java21_datastore_retrieving_an_entity]
    // Key employeeKey = ...;
    Entity employee = datastore.get(employeeKey);
    // [END gae_java21_datastore_retrieving_an_entity]

    assertWithMessage("retrieved key ID")
        .that(employee.getKey().getId())
        .isEqualTo(employeeKey.getId());
  }

  @Test
  public void deletingAnEntity_deletesAnEntity() throws Exception {
    Entity employee = new Entity("Employee", "asalieri");
    datastore.put(employee);

    Key employeeKey = KeyFactory.createKey("Employee", "asalieri");
    // [START gae_java21_datastore_deleting_an_entity]
    // Key employeeKey = ...;
    datastore.delete(employeeKey);
    // [END gae_java21_datastore_deleting_an_entity]

    try {
      Entity got = datastore.get(employeeKey);
      fail("Expected EntityNotFoundException");
    } catch (EntityNotFoundException expected) {
      assertWithMessage("exception key name")
          .that(expected.getKey().getName())
          .isEqualTo("asalieri");
    }
  }

  @Test
  public void repeatedProperties_storesList() throws Exception {
    // [START gae_java21_datastore_repeated_properties]
    Entity employee = new Entity("Employee");
    ArrayList<String> favoriteFruit = new ArrayList<>();
    favoriteFruit.add("Pear");
    favoriteFruit.add("Apple");
    employee.setProperty("favoriteFruit", favoriteFruit);
    datastore.put(employee);

    // Sometime later
    employee = datastore.get(employee.getKey());
    @SuppressWarnings("unchecked") // Cast can't verify generic type.
    ArrayList<String> retrievedFruits = (ArrayList<String>) employee.getProperty("favoriteFruit");
    // [END gae_java21_datastore_repeated_properties]

    assertThat(retrievedFruits).containsExactlyElementsIn(favoriteFruit).inOrder();
  }

  // CHECKSTYLE.OFF: VariableDeclarationUsageDistance
  @SuppressWarnings("VariableDeclarationUsageDistance")
  @Test
  public void embeddedEntity_fromEmbedded_embedsProperties() throws Exception {
    Entity employee = new Entity("Employee");
    // [START gae_java21_datastore_embedded_entities_1]
    // Entity employee = ...;
    EmbeddedEntity embeddedContactInfo = new EmbeddedEntity();

    embeddedContactInfo.setProperty("homeAddress", "123 Fake St, Made, UP 45678");
    embeddedContactInfo.setProperty("phoneNumber", "555-555-5555");
    embeddedContactInfo.setProperty("emailAddress", "test@example.com");

    employee.setProperty("contactInfo", embeddedContactInfo);
    // [END gae_java21_datastore_embedded_entities_1]
    datastore.put(employee);

    Entity gotEmployee = datastore.get(employee.getKey());
    EmbeddedEntity got = (EmbeddedEntity) gotEmployee.getProperty("contactInfo");
    assertWithMessage("got.homeAddress")
        .that((String) got.getProperty("homeAddress"))
        .isEqualTo("123 Fake St, Made, UP 45678");
  }
  // CHECKSTYLE.ON: VariableDeclarationUsageDistance

  private Key putEmployeeWithContactInfo(Entity contactInfo) {
    Entity employee = new Entity("Employee");
    // [START gae_java21_datastore_embedded_entities_2]
    // Entity employee = ...;
    // Entity contactInfo = ...;
    EmbeddedEntity embeddedContactInfo = new EmbeddedEntity();

    embeddedContactInfo.setKey(contactInfo.getKey()); // Optional, used so we can recover original.
    embeddedContactInfo.setPropertiesFrom(contactInfo);

    employee.setProperty("contactInfo", embeddedContactInfo);
    // [END gae_java21_datastore_embedded_entities_2]
    datastore.put(employee);
    return employee.getKey();
  }

  @Test
  public void embeddedEntity_fromExisting_canRecover() throws Exception {
    Entity initialContactInfo = new Entity("Contact");
    initialContactInfo.setProperty("homeAddress", "123 Fake St, Made, UP 45678");
    initialContactInfo.setProperty("phoneNumber", "555-555-5555");
    initialContactInfo.setProperty("emailAddress", "test@example.com");
    datastore.put(initialContactInfo);
    Key employeeKey = putEmployeeWithContactInfo(initialContactInfo);

    // [START gae_java21_datastore_embedded_entities_3]
    Entity employee = datastore.get(employeeKey);
    EmbeddedEntity embeddedContactInfo = (EmbeddedEntity) employee.getProperty("contactInfo");

    Key infoKey = embeddedContactInfo.getKey();
    Entity contactInfo = new Entity(infoKey);
    contactInfo.setPropertiesFrom(embeddedContactInfo);
    // [END gae_java21_datastore_embedded_entities_3]
    datastore.put(contactInfo);

    Entity got = datastore.get(infoKey);
    assertThat(got.getKey()).isEqualTo(initialContactInfo.getKey());
    assertWithMessage("got.homeAddress")
        .that((String) got.getProperty("homeAddress"))
        .isEqualTo("123 Fake St, Made, UP 45678");
  }

  @Test
  public void batchOperations_putsEntities() {
    // [START gae_java21_datastore_gae_batch_operations]
    Entity employee1 = new Entity("Employee");
    Entity employee2 = new Entity("Employee");
    Entity employee3 = new Entity("Employee");
    // [START_EXCLUDE]
    employee1.setProperty("firstName", "Bill");
    employee2.setProperty("firstName", "Jane");
    employee3.setProperty("firstName", "Alex");
    // [END_EXCLUDE]

    List<Entity> employees = Arrays.asList(employee1, employee2, employee3);
    datastore.put(employees);
    // [END gae_java21_datastore_gae_batch_operations]

    Map<Key, Entity> got =
        datastore.get(Arrays.asList(employee1.getKey(), employee2.getKey(), employee3.getKey()));
    assertWithMessage("employee1.firstName")
        .that((String) got.get(employee1.getKey()).getProperty("firstName"))
        .isEqualTo("Bill");
    assertWithMessage("employee2.firstName")
        .that((String) got.get(employee2.getKey()).getProperty("firstName"))
        .isEqualTo("Jane");
    assertWithMessage("employee3.firstName")
        .that((String) got.get(employee3.getKey()).getProperty("firstName"))
        .isEqualTo("Alex");
  }

  @Test
  public void createKey_makesKey() {
    // [START gae_java21_datastore_generating_keys_1]
    Key k1 = KeyFactory.createKey("Person", "GreatGrandpa");
    Key k2 = KeyFactory.createKey("Person", 74219);
    // [END gae_java21_datastore_generating_keys_1]

    assertThat(k1).isNotNull();
    assertThat(k2).isNotNull();
  }

  @Test
  public void keyFactoryBuilder_makeKeyWithParents() {
    Key greatKey = KeyFactory.createKey("Person", "GreatGrandpa");
    Key grandKey = KeyFactory.createKey(greatKey, "Person", "Grandpa");
    Key dadKey = KeyFactory.createKey(grandKey, "Person", "Dad");
    Key meKey = KeyFactory.createKey(dadKey, "Person", "Me");

    // [START gae_java21_datastore_generating_keys_2]
    Key k =
        new KeyFactory.Builder("Person", "GreatGrandpa")
            .addChild("Person", "Grandpa")
            .addChild("Person", "Dad")
            .addChild("Person", "Me")
            .getKey();
    // [END gae_java21_datastore_generating_keys_2]

    assertThat(k).isEqualTo(meKey);
  }

  @Test
  public void keyToString_getsPerson() throws Exception {
    Entity p = new Entity("Person");
    p.setProperty("relationship", "Me");
    datastore.put(p);
    Key k = p.getKey();

    // [START gae_java21_datastore_generating_keys_3]
    String personKeyStr = KeyFactory.keyToString(k);

    // Some time later (for example, after using personKeyStr in a link).
    Key personKey = KeyFactory.stringToKey(personKeyStr);
    Entity person = datastore.get(personKey);
    // [END gae_java21_datastore_generating_keys_3]

    assertThat(personKey).isEqualTo(k);
    assertWithMessage("person.relationship")
        .that((String) person.getProperty("relationship"))
        .isEqualTo("Me");
  }
}
