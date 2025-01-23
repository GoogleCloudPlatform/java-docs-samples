/*
 * Copyright 2025 Google LLC
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

package app;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import redis.clients.jedis.Jedis;

@ExtendWith(MockitoExtension.class)
class DataControllerTest {

  @Mock private ItemsRepository itemsRepository; // Mocked repository for database interactions

  @Mock private Jedis jedis; // Mocked Redis client for cache interactions

  private DataController dataController; // System under test

  @BeforeEach
  void setUp() {
    // Initialize the DataController with mocked dependencies before each test
    dataController = new DataController(itemsRepository, jedis);
  }

  // ----------------------------------------------------
  // get() tests
  // ----------------------------------------------------
  @Nested
  @DisplayName("Testing get() method")
  class GetTests {

    @Test
    @DisplayName("Should return item from cache if it exists in cache")
    void testGet_ItemInCache() {
      // Arrange: Item exists in cache
      long itemId = 1;
      String itemIdStr = Long.toString(itemId);
      String cachedData =
          "{\"id\":1,\"name\":\"Cached Item\",\"description\":\"Cached"
              + " description\",\"price\":10.5}";
      Item cachedItem = Item.fromJSONString(cachedData);

      given(jedis.exists(itemIdStr)).willReturn(true); // Cache contains the item
      given(jedis.get(itemIdStr)).willReturn(cachedData);

      // Act: Call the get() method
      Item result = dataController.get(itemId);

      // Assert: Verify cache is used, database is not queried, and correct item is returned
      verify(jedis).expire(itemIdStr, DataController.DEFAULT_TTL); // Extend TTL in cache
      verify(itemsRepository, never()).get(anyLong()); // Database should not be called
      assertEquals(cachedItem.getId(), result.getId());
      assertEquals(cachedItem.getName(), result.getName());
      assertEquals(true, result.isFromCache());
    }

    @Test
    @DisplayName("Should return item from database and cache it if not in cache")
    void testGet_ItemNotInCache() {
      // Arrange: Item is not in cache but exists in the database
      long itemId = 2;
      String itemIdStr = Long.toString(itemId);
      Item dbItem = new Item(2L, "Database Item", "From DB", 15.99);

      given(jedis.exists(itemIdStr)).willReturn(false); // Cache miss
      given(itemsRepository.get(itemId))
          .willReturn(Optional.of(dbItem)); // Database contains the item

      // Act: Call the get() method
      Item result = dataController.get(itemId);

      // Assert: Verify database usage, cache update, and correct item return
      verify(jedis).set(itemIdStr, dbItem.toJSONObject().toString()); // Add item to cache
      verify(jedis).expire(itemIdStr, DataController.DEFAULT_TTL); // Set TTL for cache
      assertEquals(dbItem.getId(), result.getId());
      assertEquals(dbItem.getName(), result.getName());
      assertEquals(false, result.isFromCache());
    }

    @Test
    @DisplayName("Should return null if item does not exist in cache or database")
    void testGet_ItemNotFound() {
      // Arrange: Item does not exist in cache or database
      long itemId = 3;
      String itemIdStr = Long.toString(itemId);

      given(jedis.exists(itemIdStr)).willReturn(false); // Cache miss
      given(itemsRepository.get(itemId)).willReturn(Optional.empty()); // Database miss

      // Act: Call the get() method
      Item result = dataController.get(itemId);

      // Assert: Verify no cache update and null return
      verify(jedis, never()).set(anyString(), anyString()); // Cache should not be updated
      assertNull(result);
    }
  }

  // ----------------------------------------------------
  // create() tests
  // ----------------------------------------------------
  @Nested
  @DisplayName("Testing create() method")
  class CreateTests {

    @Test
    @DisplayName("Should create item in cache and database")
    void testCreate() {
      // Arrange: Item to be created
      Item item = new Item("New Item", "New Description", 20.0);

      given(itemsRepository.create(item)).willReturn(0L); // Simulate database creation with ID 0

      // Act: Call the create() method
      long result = dataController.create(item);

      // Assert: Verify cache and database interactions
      Item expectedItem = new Item(0L, item.getName(), item.getDescription(), item.getPrice());
      verify(jedis)
          .set(Long.toString(result), expectedItem.toJSONObject().toString()); // Add item to cache
      verify(jedis).expire(Long.toString(result), DataController.DEFAULT_TTL); // Set TTL for cache
      assertEquals(0L, result); // Validate returned ID
    }
  }

  // ----------------------------------------------------
  // delete() tests
  // ----------------------------------------------------
  @Nested
  @DisplayName("Testing delete() method")
  class DeleteTests {

    @Test
    @DisplayName("Should delete item from both cache and database")
    void testDelete_ItemExists() {
      // Arrange: Item exists in cache
      long itemId = 6;
      String itemIdStr = Long.toString(itemId);

      given(jedis.exists(itemIdStr)).willReturn(true); // Cache contains the item

      // Act: Call the delete() method
      dataController.delete(itemId);

      // Assert: Verify deletion from both cache and database
      verify(itemsRepository).delete(itemId); // Delete from database
      verify(jedis).del(itemIdStr); // Delete from cache
    }

    @Test
    @DisplayName("Should only delete item from database if not in cache")
    void testDelete_ItemNotInCache() {
      // Arrange: Item does not exist in cache
      long itemId = 7;
      String itemIdStr = Long.toString(itemId);

      given(jedis.exists(itemIdStr)).willReturn(false); // Cache miss

      // Act: Call the delete() method
      dataController.delete(itemId);

      // Assert: Verify database deletion and no cache interaction
      verify(itemsRepository).delete(itemId); // Delete from database
      verify(jedis, never()).del(anyString()); // Cache should not be updated
    }
  }

  // ----------------------------------------------------
  // exists() tests
  // ----------------------------------------------------
  @Nested
  @DisplayName("Testing exists() method")
  class ExistsTests {

    @Test
    @DisplayName("Should return true if item exists in cache")
    void testExists_ItemInCache() {
      // Arrange: Item exists in cache
      long itemId = 8;
      String itemIdStr = Long.toString(itemId);

      given(jedis.exists(itemIdStr)).willReturn(true); // Cache contains the item

      // Act: Call the exists() method
      boolean result = dataController.exists(itemId);

      // Assert: Verify true result and no database call
      assertTrue(result);
      verify(itemsRepository, never()).exists(anyLong()); // Database should not be queried
    }

    @Test
    @DisplayName("Should return true if item exists in database")
    void testExists_ItemInDatabase() {
      // Arrange: Item is not in cache but exists in the database
      long itemId = 9;
      String itemIdStr = Long.toString(itemId);

      given(jedis.exists(itemIdStr)).willReturn(false); // Cache miss
      given(itemsRepository.exists(itemId)).willReturn(true); // Database contains the item

      // Act: Call the exists() method
      boolean result = dataController.exists(itemId);

      // Assert: Verify true result
      assertTrue(result);
    }

    @Test
    @DisplayName("Should return false if item does not exist in cache or database")
    void testExists_ItemNotFound() {
      // Arrange: Item does not exist in cache or database
      long itemId = 10;
      String itemIdStr = Long.toString(itemId);

      given(jedis.exists(itemIdStr)).willReturn(false); // Cache miss
      given(itemsRepository.exists(itemId)).willReturn(false); // Database miss

      // Act: Call the exists() method
      boolean result = dataController.exists(itemId);

      // Assert: Verify false result
      assertFalse(result);
    }
  }
}
