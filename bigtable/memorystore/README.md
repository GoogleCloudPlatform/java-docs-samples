# Google Cloud Bigtable Memorystore

This code shows how to use Cloud Bigtable with a caching solution hosted on Memorystore.


## Setup

1. [Create a Bigtable instance](https://cloud.google.com/bigtable/docs/creating-instance)

1. Set your variables
    ```
    BIGTABLE_PROJECT=YOUR-PROJECT-ID OR $GOOGLE_CLOUD_PROJECT
    INSTANCE_ID=YOUR-INSTANCE-ID
    TABLE_ID=mobile-time-series # Example table for sample  
    ```

1. Create a table with one row

    ```
    echo project = $BIGTABLE_PROJECT > ~/.cbtrc
    echo instance = $INSTANCE_ID >> ~/.cbtrc
    
    cbt createtable $TABLE_ID "families=stats_summary" 
    cbt set mobile-time-series phone#4c410523#20190501 stats_summary:os_build=PQ2A.190405.003 stats_summary:os_name=android
    ```

## Memcached

1. Set up a Memcached instance locally or hosted on [Memorystore](https://cloud.google.com/memorystore/docs/memcached/memcached-overview).

1. Get the Memcached host IP and set that as a variable.

    ```
    MEMCACHED_DISCOVERY_ENDPOINT="0.0.0.0"
    ```

1. Run the code. If you are trying to connect to Memcached on Memorystore, you will
need to run this code within the same [VPC network](https://cloud.google.com/vpc/docs/vpc)
to connect. The easiest way to do this is by creating a Compute VM on the same network,
SSHing to that and running the code from there.

    ```
    mvn compile exec:java -Dexec.mainClass=Memcached \
    -DbigtableProjectId=$PROJECT_ID \
    -DbigtableInstanceId=$INSTANCE_ID \
    -DbigtableTableId=$TABLE_ID \
    -DmemcachedDiscoveryEndpoint=$MEMCACHED_DISCOVERY_ENDPOINT
    ```

    The first time you run the program, it will fetch the data from Bigtable.
    If you run it again (within the cache limit), it will fetch the data from Memcached. 