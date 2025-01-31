# Building a Session Management Service on Google Cloud using Valkey, Spring Boot, and PostgreSQL

Session management is a crucial part of modern web applications, ensuring that user interactions remain consistent and secure across multiple requests. This guide outlines how to create a session management system using Spring Boot, PostgreSQL, and Valkey (or Memorystore on GCP). By using a caching layer, the application can efficiently manage user sessions while reducing database load and ensuring scalability.

## Why Session Management Matters

- **Consistency:** Maintains state across user interactions.
- **Security:** Protects user data and prevents unauthorized access.
- **Performance:** Reduces database queries by caching active sessions.

## What You’ll Build

You’ll set up a session management service for storing shopping cart items that:

1. **Stores session data in PostgreSQL** for persistence.
2. **Uses Valkey (Memorystore)** as an in-memory cache for faster session lookups.
3. **Spring Boot Applications** Exposes RESTful API for creating, updating, deleting, and retrieving session items with auto expiry.
4. **Deploys on Google Cloud Platform (GCP)** using services like Cloud Run, Cloud SQL, and Memorystore.

By following this guide, you’ll implement a scalable and secure session management system.

## Architecture Overview

- **Spring Boot Application:** Manages session logic and provides APIs for interaction.
- **Valkey (In-Memory Cache):** Stores active session data for fast lookups.
- **PostgreSQL Database:** Acts as the persistent storage for session data.
- **Google Cloud Platform Services:** Hosts the application and its dependencies.

## Step-by-Step Guide

To begin, we are will generate an API with the following routes:

1. **Session Creation:** When a user logs in, a session is created and stored in the database cache with a Time-To-Live (TTL).
2. **Session Retrieval:** For every request, the session is checked in the cache. If not found, it is retrieved from the database and re-cached.
3. **Session Update:** When a session is updated (e.g., extended TTL), both the cache and database are updated.
4. **Session Invalidation:** When a user logs out or the session expires, it is removed from the cache and database.

### Creating a new application

The first step is to initialize a Spring Boot application. The [official guide](https://spring.io/guides/gs/spring-boot) demonstrates how to generate a new project using [Spring Initializer](https://start.spring.io/).

1. Choose `Maven` as the project type for this demonstration.
2. Select Sprint Boot version 3.4.1.
3. Complete the appropriate metadata.
4. Choose your preferred `Packing` for downloading.
5. Select `Java 17` for your Java version.
6. Finally, generate and extract the files.

#### Installing additional dependencies

Next, ensure the following dependencies have been added to your POM.xml file.

#### Jedis

Add the folowing snippet toconnect directly to the Memorystore for Valkey instance.

```xml
<!-- Jedis: Redis Java Client -->
<dependency>
   <groupId>redis.clients</groupId>
   <artifactId>jedis</artifactId>
   <version>4.3.0</version>
</dependency>
```

#### Jakarta

To ensure that our API routes are correctly validated. Add the following dependency.
This enables the use of annotations like `@NotNull` and `@Size` on classes to automatically enforce input constraints, reducing the need for manual validation logic.

```xml
<!-- Add Validation support-->
<dependency>
   <groupId>jakarta.validation</groupId>
   <artifactId>jakarta.validation-api</artifactId>
   <version>3.0.2</version>
</dependency>
```

### Connecting the Service layer

Next, add the following to route logic to the API.

#### Logging in a user

```java
  public String login(String username, String password) {
    // Authenticate user
    Optional<Integer> userId = accountRepository.authenticateUser(
      username,
      password
    );

    // No user found
    if (userId.isEmpty()) {
      return null;
    }

    // Generate token for the user
    String token = Utils.generateToken(Global.TOKEN_BYTE_LENGTH);

    try {
      jedis.set(token, username);
      jedis.expire(token, Global.TOKEN_EXPIRATION);
    } catch (Exception e) {
      throw new RuntimeException("Failed to store session token", e);
    }
```

### Logging out a user

```java
public void logout(String token) {
    try {
      jedis.del(token);
    } catch (Exception e) {
      throw new RuntimeException("Failed to delete session token", e);
    }
  }
```

### Getting the users shopping basket

```java
  @GetMapping
  public ResponseEntity<Map<String, String>> getBasket(
    HttpServletRequest request
  ) {
    String basketKey = getBasketKey(request);
    return ResponseEntity.ok(jedis.hgetAll(basketKey));
  }
```

#### Adding to the shopping basket

```java
  @PostMapping("/add")
  public ResponseEntity<String> addItem(
    @RequestParam String itemId,
    @RequestParam(defaultValue = "1") int quantity,
    HttpServletRequest request
  ) {
    String basketKey = getBasketKey(request);
    long newQty = jedis.hincrBy(basketKey, itemId, quantity);
    return ResponseEntity.ok("Quantity updated: " + newQty);
  }
```

#### Removing an item to the shopping basket

```java
  @PostMapping("/remove")
  public ResponseEntity<String> removeItem(
    @RequestParam String itemId,
    @RequestParam(defaultValue = "1") int quantity,
    HttpServletRequest request
  ) {
    String basketKey = getBasketKey(request);
    long newQty = jedis.hincrBy(basketKey, itemId, -quantity);
    if (newQty <= 0) {
      jedis.hdel(basketKey, itemId);
      return ResponseEntity.ok("Item removed");
    }
    return ResponseEntity.ok("Quantity updated: " + newQty);
  }

```

#### Clearing the shopping basket

```java
  @PostMapping("/clear")
  public ResponseEntity<String> clearBasket(HttpServletRequest request) {
    jedis.del(getBasketKey(request));
    return ResponseEntity.ok("Basket cleared");
  }
```

#### Getting the users session token

```java
private String getBasketKey(HttpServletRequest request) {
    String token = Utils.getTokenFromCookie(request.getCookies());
    return "basket:" + token;
  }
```

## Scaling and Optimization

As traffic increases, the architecture can scale horizontally:

- **Cloud Run** can automatically scale instances based on load.
- **Memorystore (Valkey)** can be sized or upgraded to handle more cached data or higher throughput.
- **Cloud SQL** can scale vertically or horizontally (with read replicas) as needed.

You can fine-tune cache expiration strategies (TTL values) and eviction policies, depending on your data access patterns.

## Conclusion

By implementing this session management system, you ensure high performance, scalability, and secure session handling. Leveraging caching with Valkey (Memorystore) significantly reduces database load while maintaining fast and reliable user experiences. Running it in Google Cloud extends these benefits further, providing managed services and easy scaling.

For more information, check out the [repository](https://github.com/GoogleCloudPlatform/java-docs-samples/tree/main/memorystore/valkey/session) for the full project details and follow the instructions to get started.
