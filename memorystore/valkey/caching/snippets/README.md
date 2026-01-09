# Caching Samples

This folder contains a samples in form of stand-alone snippets that demonstrate useful scenarios.

## Prerequisites

Ensure the following setup is in place.

### Java

You must have java installed locally on your machinee. Run `java --version` to check if this is available.

### Memorystore for Valkey Instance

A working instance of Memorystore for Valkey must be available. You can run the [Valkey CLI](https://valkey.io/topics/cluster-tutorial/#create-a-valkey-cluster) for a local instance, or create an instance through the [GCP Platform](https://console.cloud.google.com/memorystore/valkey/instances?).

To setup a live instance, create a new Memorystore instance through the GCloud CLI using the following

```bash
gcloud redis instances create myinstance --size=2 --region=LOCATION --redis-version=redis_6_x
```

Altrernativley, run a local instance through the Valkey CLI

```bash
valkey-cli
```

## Running the sample code

Each example contains instructions on any prerequisite configuration.

## Compile the app through Maven (optional)

```bash
mvn compile
```

## Run the sample code

```bash
mvn exec:java -Dexec.mainClass=MemorystoreTtlItem #Replace the main class as needed
```
