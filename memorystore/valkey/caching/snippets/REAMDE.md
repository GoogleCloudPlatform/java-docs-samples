# Caching Samples

This folder contains a samples in form of stand-alone snippets that demonstrate useful scenarios.

## Prerequiites

Ensure the following setup is in place.

### Java

You must have java installed locally on your machinee. Run `java --version` to check if this is available.

### MemoryStore for Valkey Instance

A working instance of memorystore for valkey must be available. You can run the [Valkey CLI](https://valkey.io/topics/cli/) for a local instance, or create an instance through the [GCP Platform](https://console.cloud.google.com/memorystore/valkey/instances?).

## Running the sample code

Each example contains instructions on any prerequiite configuration.

## Compile the app through Maven (optional)

```bash
mvn compile
```

## Run the sample code

```bash
mvn exec:java -Dexec.mainClass=MemorystoreTTLItem //Replace the main class as needed
```
