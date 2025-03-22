# Building a Leaderboard Service on Google Cloud using Valkey, Spring Boot, and PostgreSQL

Leaderboards are a useful way to display ranking data in applications. This guide explains how to create a scalable leaderboard system using Spring Boot, PostgreSQL, and Valkey (or Memorystore on GCP). By using a caching layer, developers can deliver real-time leaderboard rankings while reducing database load.

## Benefits of a Cached Leaderboard

- **Performance:** Leaderboards store ranking data in memory, enabling near-instantaneous retrieval of scores and rankings, reducing the time required to query and sort data from the database.
- **Scalability:** Cached leaderboards can handle a high volume of reads and writes, providing consistent update and response times.
- **Database Efficiency:** Caching frequently accessed leaderboard data minimizes the need for repetitive, and high volume database queries.

## What You’ll Build

You’ll set up a leaderboard service that:

1. **Stores leaderboard data in PostgreSQL** for persistence and historical analysis.
2. **Uses Valkey (Memorystore)** as an in-memory cache for updating scores and quick lookups.
3. **Spring Boot Applications** to expose RESTful APIs for adding scores, retrieving rankings, and filtering leaderboards.
4. **Deploys on Google Cloud Platform (GCP)** using services like Cloud Run, Cloud SQL, and Memorystore.

By following this guide, you’ll implement a high-performing, scalable leaderboard system.

## Architecture Overview

- **Spring Boot Application:** Manages leaderboard logic and provides APIs for interaction.
- **Valkey (In-Memory Cache):** Stores active leaderboard data for quick lookups.
- **PostgreSQL Database:** Acts as the persistent storage for leaderboard data.
- **Google Cloud Platform Services:** Hosts the application and its dependencies.

## Leaderboard Workflow

1. **Score Submission:** A user submits a score, which is added to the cache and database.
2. **Rank Retrieval:** By default rankings are returned, displaying data ordered by the highest scores.
3. **Rank Retrieval (Filtered):** Rankings are displayed based on any applied filters.

## Step-by-Step Guide

To begin, generate an API with the following routes:

_addScore_: Creates a new score.
_getScores_: Returns the leaderboard rankings.

### Creating a new application

The first step is to initialize a Spring Boot application. The [official guide](https://spring.io/guides/gs/spring-boot) demonstrates how to generate a new project using [Spring Initializer](https://start.spring.io/).

1. Choose `Maven` as the project type for this demonstration..
2. Select Sprint Boot version 3.4.1
3. Complete the appropriate metadata.
4. Choose your preferred `Packaging` for downloading.
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

To ensure that our api routes are correctly validated. Add the following dependency.
This dependency enables the use of annotations like `@NotNull` and `@Size` on classes to automatically enforce input constraints, reducing the need for manual validation logic.

```xml
<!-- Add Validation support-->
<dependency>
   <groupId>jakarta.validation</groupId>
   <artifactId>jakarta.validation-api</artifactId>
   <version>3.0.2</version>
</dependency>
```

### Connecting the Service Layer

Next, add the following to the routing logic in the API.

#### Initalizing the cache

To ensure data is always availabe in the caching layer, a request is made to the database to update the cache if it is empty.

```java
private boolean initializeCache() {
   if (this.jedis.zcard(Global.LEADERBOARD_ENTRIES_KEY) > 0) {
      return false;
   }

   List<LeaderboardEntry> entries = this.leaderboardRepository.getEntries();

   if (!entries.isEmpty()) {
      for (LeaderboardEntry entry : entries) {
            this.jedis.zadd(
                  Global.LEADERBOARD_ENTRIES_KEY, entry.getScore(), entry.getUsername());
      }
   }

   return true;
}

```

#### Fetching the leaderboard with zrangeWithScores (Ascending)

```java
if (!isDescending) {
   entries = new ArrayList<>(jedis.zrangeWithScores(cacheKey, position, maxPosition));
}
```

#### Fetching the leaderboard with zrevrangeWithScores (Descending)

```java
if (isDescending) {
   entries = new ArrayList<>(jedis.zrevrangeWithScores(cacheKey, position, maxPosition));
}
```

##### Fetching the leaderboard based on a user search using zrevrank

```java
if (username != null) {
   Long userRank = jedis.zrevrank(cacheKey, username);
   if (userRank != null) {
         position = userRank;
         maxPosition = userRank + pageSize - 1;

         return new LeaderboardResponse(
               getEntries(cacheKey, position, maxPosition, true), cacheStatus);
   }
}
```

##### Creating or updating a user score

```java
public void createOrUpdate(String username, Double score) {
   this.leaderboardRepository.update(username, score);
   this.jedis.zadd(Global.LEADERBOARD_ENTRIES_KEY, score, username);
}
```

## Scaling and Optimization

As traffic increases, the architecture can scale horizontally:

- **Cloud Run** can automatically scale instances based on load.
- **Memorystore (Valkey)** can be sized or upgraded to handle more cached data or higher throughput.
- **Cloud SQL** can scale vertically or horizontally (with read replicas) as needed.

You can fine-tune cache expiration strategies (TTL values) and eviction policies, depending on your data access patterns.

## Conclusion

By implementing this leaderboard system, you can easily display large amounts of sorted datasets, while also having the support to effectively filter the data. Leveraging caching with Valkey (Memorystore) significantly reduces database load while maintaining fast and reliable user experiences. Running it in Google Cloud extends these benefits further, providing managed services and easy scaling.

For more information check out the [repository](https://github.com/GoogleCloudPlatform/java-docs-samples/tree/main/memorystore/valkey/leaderboard) for the full project details and follow the instructions to get started.
