# Building a Caching Service on Google Cloud using Valkey, Spring Boot, and PostgreSQL

ValKey is a globally distributed, in-memory key-value store offered as part of Google Cloud Platform (GCP). It's ideal for use cases requiring extremely low latency and high throughput, such as caching. This guide explores how to use ValKey for caching on GCP, covering benefits, basic operations, and key concepts like Time-to-Live (TTL).

## Why Cache with ValKey?

Caching frequently accessed data can significantly improve application performance and reduce backend load. Here's why ValKey is a strong choice for caching on GCP:

- **Low Latency**: ValKey's in-memory storage provides extremely fast read and write speeds, minimizing latency for cached data retrieval.
- **High Throughput**: ValKey is designed to handle a massive number of requests per second, making it suitable for high-traffic applications.
- **Global Distribution**: With its global presence, ValKey can serve cached data from locations close to your users, further reducing latency.
- **Scalability**: ValKey automatically scales to accommodate your needs, ensuring consistent performance even under heavy load.
- **Integration with GCP**: ValKey seamlessly integrates with other GCP services, simplifying deployment and management.

## Common Caching Use Cases

Caching has many advantages including but not limited to:

- **HTTP Request Caching**: Store responses from frequently accessed APIs or web pages in ValKey to accelerate page load times and reduce server load.
- **Session Management**: Cache user session data in ValKey for fast access and improved user experience.
- **Database Query Caching**: Store results of frequently executed database queries in ValKey to offload your database and improve response times.

## ValKey Operations for Caching

This use-case demonstrates how to apply a Caching solution using Valkey for MemoryStore in `Java` using the Jedis client.

## Application Design

This application will be based around a sasic CRUD API that will enable users to create, retrieve and delete items in a cache or database.

The structure comprises of 3 distinct layers:

**_API Layer_**: This is our Java based API which will define routes for each action.
**Caching Layer**: Using Valkey for MemoryStore, this will involve a low latency approach for retrieving data that has been stored once it has retrieved from the database.
**Database Layer**: Our main datasource and source of truth. This Google Coud SQL database will store all of the items that have been creared.

//Add Diagram of archesecture here

## Step-by-step Guide

To begin, we are will generate an API with the following routes:

_create_: For creating new items.
_delete_: For creating deleting items.
_create_: For creating new items.
_create_: For creating new items.

### Creating a new application

The first step is to initialize a brand new Spring Boot application. The [offical guide](https://spring.io/guides/gs/spring-boot) demonstrates how to generate a new project using [Spring Initializer](https://start.spring.io/).

1. Select `Maven` as the project type for this demonstration..
2. Select Sprint Boot version 3.4.1
3. Complete the appropriate metadata.
4. Choose your preffered Packing for downloading
5. Select `Java 17` for your Java version.
6. Finally, generate and extract the files.

### Installing additional dependencies

Next, ensure the following dependencies have been added to your POM.xml file.

#### Jedis

Add the folowing snippet toconnect directly to the Memorystore for Valkey instance.

```xml
        <!-- Jedis: Redis Java Client -->
        <dependency>
            <groupId>redis.clients</groupId>
            <artifactId>jedis</artifactId>
            <version>4.3.0</version> <!-- Use the latest version -->
        </dependency>
```

#### Jakarta

To ensure that our api routes are correctly validated. Add the following dependency:

```xml
   <!-- Add Validation support-->
   <dependency>
      <groupId>jakarta.validation</groupId>
      <artifactId>jakarta.validation-api</artifactId>
      <version>3.0.2</version>
   </dependency>
```

### Connecting our Service layer

Next, add the following to route logic to the API.

### Writing to the cache

Adding an entry will add the new item to the database. Once created, the id return from the database will be updated to a new object with the entries attributes. This new object will be added to the Memorystore cach with the appropirate Time-to-live (TTL) value.

```java

/** Import the Jedis library */
import redis.clients.jedis.Jedis;

/** Creating an item with Time-to-live (TTL) */
public long create(Item item) {
   /** Save the item in the database */
   long itemId = itemsRepository.create(item);

   /** Create a new object with the saved database id */
   Item createdItem = new Item(
   itemId,
   item.getName(),
   item.getDescription(),
   item.getPrice()
   );

   /** Cache the data in Memorystore for valkey with the Time-to-live value **/
   String idString = Long.toString(itemId);
   jedis.set(idString, createdItem.toJSONObject().toString());
   jedis.expire(idString, DEFAULT_TTL);

   /** Return the item id */
   return itemId;
}
```

### Reading from the Cache (Retrieving Values)

This method demonstrates how to efficiently retrieve data using a caching layer (Memorystore) to improve performance. The function first checks the cache for the requested item and handles its time-to-live (TTL) appropriately. If the item is not found in the cache, it fetches the data from the database, caches it, and then returns the result.

```java
/** Import the Jedis library */
import redis.clients.jedis.Jedis;

public Item get(long id) {
   /** Ensure that the item id is a string for retirval from Memorystore */
    String idString = Long.toString(id);

    /* Check if the item exists in  the cache */
    if (jedis.exists(idString)) {
      // If the data exists in the cache extend the TTL
      jedis.expire(idString, DEFAULT_TTL);

      // Return the cached data
      Item cachedItem = Item.fromJSONString(jedis.get(idString));
      cachedItem.setFromCache(true);
      return cachedItem;
    }

   /** Search for the item in the database */
   Optional<Item> item = itemsRepository.get(id);

    /** Check if a record has been found, If the data doesn't exist in the database, return null */
    if (item.isEmpty()) {
      return null;
    }

    // Cache result from the database with the default TTL
    jedis.set(idString, item.get().toJSONObject().toString());
    jedis.expire(idString, DEFAULT_TTL);

    return item.get();
  }
```

### Deleting from the Cache (Invalidating Entries)

For deletions, an item is first removed the datbase. Foollowing this, a check is perfromed to see if this item exists in Memorystore. If an item does exist, the entry is invalidated in the cache to maintain data consistency.

```java
   /** Import the Jedis library */
   import redis.clients.jedis.Jedis;

   public void delete(long id) {
      // Delete the data from database
      itemsRepository.delete(id);

      // Also, delete the data from the cache if it exists
      String idString = Long.toString(id);
      if (jedis.exists(idString)) {
        jedis.del(idString);
      }
   }
```

## Conclusion

By combining an in-memory store (Valkey) with a reliable database (PostgreSQL), all orchestrated by a Spring Boot application, you can build a caching solution that delivers high performance, reduces database load, and ensures an excellent user experience. Running it in Google Cloud extends these benefits further, providing managed services and easy scaling.

For more information check out the [repository](https://github.com/GoogleCloudPlatform/java-docs-samples/tree/main/memorystore/valkey/caching)) for the full project details and follow the instructions to get started:
