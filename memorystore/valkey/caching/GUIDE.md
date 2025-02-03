# Building a Caching Service on Google Cloud using Valkey, Spring Boot, and PostgreSQL

Modern applications need to deliver fast, responsive user experiences at scale.

In this guide, there are architectural concepts and deployment steps for creating a high-performance caching service on Google Cloud. Using a combination of Java, Spring Boot, PostgreSQL, and Valkey, you can reduce latency while also reducing the load on your database.

## Why Caching Matters

- **Speed & Latency:** Storing frequently requested data in memory avoids repeated round-trip queries to databases, while reducing response times.
- **Scalability:** By reducing the workload on your database, applications can serve data directly from memory, increasing the capacity for requests.

## What You’ll Build

You’ll set up a caching service that:

1. **Works with a PostgreSQL database** to store long-lived, persistent records.
2. **Incorporates Valkey** as a high-speed, in-memory cache, fronting the PostgreSQL database.
3. **Uses Spring Boot** to expose REST endpoints, providing a simple interface for reading, writing, and invalidating cached data.
4. **A solution that can be deployed to Google Cloud Platform (GCP)** for production, leveraging services like Cloud Run, Cloud SQL, and Memorystore.

By following this guide, you’ll have a reference architecture ready to adapt, test, and deploy to meet the performance needs of your application.

## Architecture Overview

- **Spring Boot Application:** Serves as the middle tier for responding to API calls. When a request is received, the API checks Valkey for cached results; if no entries are found, then the API will retrieve data from the PostgreSQL database and update the cache.
- **Valkey (In-Memory Cache):** A Redis-like memory store that keeps hot data ready to be served instantly.
- **PostgreSQL Database:** Your source of truth for all data. The cache reduces how often the app queries this database.
- **Google Cloud Infrastructure:** Deployed using Terraform, you can host the application on Cloud Run, store data in Cloud SQL for PostgreSQL, and leverage Memorystore for Valkey.

## Step-by-step Guide

To begin, we are will generate an API with the following routes:

_create_: Creating new items in the database, and adding items to the cache with a TTL value.
_retrieving_: For finding items in the cache, before falling back to the database if required.
_delete_: Removing items from both the database and cache layers.

### Creating a new application

The first step is to initialize a brand new Spring Boot application. The [official guide](https://spring.io/guides/gs/spring-boot) demonstrates how to generate a new project using [Spring Initializer](https://start.spring.io/).

1. Choose `Maven` as the project type for this demonstration.
2. Select Spring Boot version `3.4.1`.
3. Complete the appropriate metadata.
4. Choose your preferred `Packaging` for downloading.
5. Select `Java 17` for your Java version.
6. Finally, generate and extract the files.

### Installing additional dependencies

Next, ensure the following dependencies have been added to your POM.xml file.

#### Jedis

Add the following snippet to connect directly to the Memorystore for Valkey instance.

```xml
<!-- Jedis: Redis Java Client -->
<dependency>
   <groupId>redis.clients</groupId>
   <artifactId>jedis</artifactId>
   <version>4.3.0</version> <!-- Use the latest version -->
</dependency>
```

#### Jakarta

To ensure that our API routes are correctly validated, add the following dependency. This enables the use of annotations like `@NotNull` and `@Size` on classes to automatically enforce input constraints, reducing the need for manual validation logic.

```xml
<!-- Add validation support-->
<dependency>
   <groupId>jakarta.validation</groupId>
   <artifactId>jakarta.validation-api</artifactId>
   <version>3.0.2</version>
</dependency>
```

### Connecting our service layer

Next, add the following to route logic to the API.

#### Writing to the cache

Adding an entry will add the new item to the database. Once created, the ID return from the database will be updated to a new object with the entries attributes. This new object will be added to the Memorystore cache with the appropriate Time-to-live (TTL) value.

```java
/** Import the Jedis library */
import redis.clients.jedis.Jedis;

/** Creating an item with Time-to-live (TTL) */
public long create(Item item) {
    // Create the data in the database
    long itemId = itemsRepository.create(item);

    // Clone the item with the generated ID
    Item createdItem = new Item(
        itemId,
        item.getName(),
        item.getDescription(),
        item.getPrice());

    // Use try-catch to avoid returning the data if there's an error with the cache
    try {
      // Cache the data with the default TTL
      String idString = Long.toString(itemId);
      jedis.setex(idString, DEFAULT_TTL, createdItem.toJSONObject().toString());
    } catch (Exception e) {
      // If there's an error with the cache, log the error and continue
      System.err.println("Error with cache: " + e.getMessage());
    }

    return itemId;
}
```

#### Reading from the cache (retrieving values)

This method demonstrates how to efficiently retrieve data using a caching layer (Memorystore) to improve performance.

**Step 1**: This function searches the cache to see if an item exists, if found the cached itme is returned.
**Step2**: If the item is not found in the cache, it is retrieved from the database. If no record exists, then `null` is returned.
**Step3**: The database item is then turned into a string and cached in the datbase with the default TTL.
**Step4**: The database item is then returned.

```java
/** Import the Jedis library */
import redis.clients.jedis.Jedis;

/** Set a default value for Time-to-live(TTL) **/
public static final Long DEFAULT_TTL = 60 L;

/** Retrieve an item from the cache */
public Item get(long id) {
    String idString = Long.toString(id);

    // Use try-catch to avoid missing the database if there's an error with the
    // cache
    try {
      // Check if the data exists in the cache first
      String cachedValue = jedis.get(idString);
      if (cachedValue != null) {
        // Return the cached data
        Item cachedItem = Item.fromJSONString(cachedValue);
        cachedItem.setFromCache(true);
        return cachedItem;
      }
    } catch (Exception e) {
      // If there's an error with the cache, log the error and continue
      System.err.println("Error with cache: " + e.getMessage());
    }

    Optional<Item> item = itemsRepository.get(id);

    if (item.isEmpty()) {
      // If the data doesn't exist in the database, return null
      return null;
    }

    // Use try-catch to avoid missing returning the data if there's an error with
    // the cache
    try {
      // Cache result from the database with the default TTL
      jedis.setex(idString, DEFAULT_TTL, item.get().toJSONObject().toString());
    } catch (Exception e) {
      // If there's an error with the cache, log the error and continue
      System.err.println("Error with cache: " + e.getMessage());
    }

    return item.get();
}
```

#### Deleting from the cache (invalidating entries)

For deletions, an item is first removed the datbase. Following this, a check is performed to see if this item exists in Memorystore. If an item does exist, the entry is invalidated in the cache to maintain data consistency.

```java
/** Import the Jedis library */
import redis.clients.jedis.Jedis;

/** Delete an item from the database and cache */
public void delete(long id) {
    // Delete the data from database
    itemsRepository.delete(id);

    // Use try-catch to avoid missing the cache if there's an error with the cache
    try {
      // Also, delete the data from the cache if it exists
      String idString = Long.toString(id);
      long totalDeleted = jedis.del(idString);

      if (totalDeleted == 0) {
        throw new Exception("Item not found in cache");
      }
    } catch (Exception e) {
      // If there's an error with the cache, log the error and continue
      System.err.println("Error with cache: " + e.getMessage());
    }
}
```

## Scaling and Optimization

As traffic increases, the architecture can scale horizontally:

- **Cloud Run** can automatically scale instances based on load.
- **Memorystore (Valkey)** can be sized or upgraded to handle more cached data or higher throughput.
- **Cloud SQL** can scale vertically or horizontally (with read replicas) as needed.

You can fine-tune cache expiration strategies (TTL values) and eviction policies, depending on your data access patterns.

## Conclusion

By combining an in-memory store (Valkey) with a reliable database (PostgreSQL), all orchestrated by a Spring Boot application, you’ve built a caching solution that delivers high performance, reduces database load, and ensures an excellent user experience. Running it in Google Cloud extends these benefits further, providing managed services and easy scaling.

For more information, check out the [repository](https://github.com/GoogleCloudPlatform/java-docs-samples/tree/main/memorystore/valkey/caching) for the full project details and follow the instructions to get started.
