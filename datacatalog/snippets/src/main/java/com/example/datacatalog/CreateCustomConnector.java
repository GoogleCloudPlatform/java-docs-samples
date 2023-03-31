package com.example.datacatalog;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.datacatalog.v1.ColumnSchema;
import com.google.cloud.datacatalog.v1.DataCatalogClient;
import com.google.cloud.datacatalog.v1.DumpItem;
import com.google.cloud.datacatalog.v1.Entry;
import com.google.cloud.datacatalog.v1.EntryType;
import com.google.cloud.datacatalog.v1.ImportEntriesMetadata;
import com.google.cloud.datacatalog.v1.ImportEntriesRequest;
import com.google.cloud.datacatalog.v1.ImportEntriesResponse;
import com.google.cloud.datacatalog.v1.Schema;
import com.google.cloud.datacatalog.v1.SystemTimestamps;
import com.google.cloud.datacatalog.v1.Tag;
import com.google.cloud.datacatalog.v1.TagField;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.datacatalog.v1.TaggedEntry;
import com.google.common.collect.ImmutableList;
import com.google.longrunning.Operation;
import com.google.longrunning.OperationsClient;
import com.google.protobuf.util.Timestamps;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.Date;

// Sample to create a custom connector. A production-ready connector does the following:
// 1. Fetches metadata from a source system (for example, from an RDBMS).
// 2. Creates Dataplex metadata objects (Entries, Tags) based on the fetched data.
// 3. Writes them to Google Cloud Storage bucket
// 4. Calls ImportEntries() API of the Dataplex Catalog to initiate import process.

public class CreateCustomConnector {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException {
    // TODO(developer): Replace these variables before running the sample.
    // String projectId = "my-project";
    // String entryGroupId = "onprem_entry_group";
    // String gcsBucketName = "my_gcs_bucket";
    // String storageProjectId = "my-storage-project"; // can be the same as projectId where metadata will be stored; but does not have to be.

    String projectId = "qc-cloudsql-connector-devproj";
    String entryGroupId = "github_sample_eg";
    String gcsBucketName = "qc_public_sample_test";
    String storageProjectId = "qc-cloudsql-connector-devproj";

    // Use any available Dataplex Catalog region.
    String location = "us-central1";

    /* Use Spark context if you would like to run a connector on GCP as a Dataplex task.
    At the end of the application, stop the context.
    JavaSparkContext ctx = new JavaSparkContext(new SparkConf());
    < rest of the connector code.. >
    ctx.stop();
    */

    importEntriesViaCustomConnector(location, projectId, entryGroupId, storageProjectId, gcsBucketName);


  }

  public static void importEntriesViaCustomConnector(String location, String projectId, String entryGroupId, String storageProjectId, String gcsBucketName)
      throws IOException, ExecutionException, InterruptedException {

    // Showing how to fetch metadata from a source system is out of the scope of this sample. Comments in the method below provide some hints though.
    fetchMetadataFromSourceSystem();

    // Translate fetched metadata into Dataplex Entry format.
    DumpItem dumpItem = prepareDumpItem();

    // Write metadata in Dataplex format to an existing Google Cloud Storage bucket.
    writeMetadataToGscBucket(dumpItem, storageProjectId, gcsBucketName);

    // Call DataplexCatalog ImportEntries() API to import the dump.
    importEntriesToCatalog(projectId, location, entryGroupId, gcsBucketName);

  }

  private static void fetchMetadataFromSourceSystem() {
    /* Here is a general approach on example of MySQL database:

    String mySqlUrl = getArg("mysql_url", args);
    String mySqlUsername = getArg("mysql_username", args);
    String mySqlPassword = getArg("mysql_password", args); // don't really do this, use [Secret Manager](https://cloud.google.com/secret-manager) to keep the password safe

    Class.forName ("com.mysql.jdbc.Driver").newInstance ();
    Connection conn = DriverManager.getConnection (mySqlUrl, mySqlUsername, mySqlPassword);
    PreparedStatement ps = conn.prepareStatement(
        "SELECT table_schema, table_name, create_time, update_time FROM information_schema.tables");
    ResultSet rs = ps.executeQuery();
      while (rs.next()) {

      // add Entry basing on ResultSet to some buffer
      // ...
    }
      rs.close();
      conn.close();

     */
  }

  private static DumpItem prepareDumpItem() {
    // Prepare Dataplex Entry based on metadata fetched form the source system.

    Schema schema = Schema.newBuilder()
        .addColumns(ColumnSchema.newBuilder().setColumn("ID").setType("LONGINT"))
        .addColumns(ColumnSchema.newBuilder().setColumn("NAME").setType("VARCHAR(20)"))
        .build();
    SystemTimestamps timestamps = SystemTimestamps.newBuilder()
        .setCreateTime(Timestamps.fromDate(new Date()))
        .setUpdateTime(Timestamps.fromDate(new Date()))
        .build();
    Entry entry = Entry.newBuilder()
        .setFullyQualifiedName("my_system:my_db.my_table")
        .setUserSpecifiedSystem("My database system")
        .setType(EntryType.TABLE)
        .setSourceSystemTimestamps(timestamps)
        .setDisplayName("My database table")
        .setSchema(schema)
        .build();

    // If some metadata is not easily modelled by Dataplex Entries, use Tags to ingest it.
    Tag tag1 = Tag.newBuilder()
        .setTemplate("projects/myproject/locations/us-central1/tagTemplates/existingTemplate")
        .putFields("field", TagField.newBuilder().setStringValue("tag1_value").build())
        .build();
    Tag tag2 = Tag.newBuilder()
        .setTemplate("projects/myproject/locations/us-central1/tagTemplates/otherExistingTemplate")
        .putFields("field", TagField.newBuilder().setStringValue("tag2_value").build())
        .setColumn("column")
        .build();

    // Tags that should be deleted from the Dataplex
    Tag absentTag = Tag.newBuilder()
        .setTemplate("projects/myproject/locations/us-central1/tagTemplates/existingTemplate")
        .setColumn("column2")
        .build();

    // Build a container for the metadata
    return DumpItem.newBuilder()
        .setTaggedEntry(
            TaggedEntry.newBuilder()
                // Add an entry
                .setV1Entry(entry)
                // Add tags to be created / updated
                .addAllPresentTags(ImmutableList.of(tag1, tag2))
                // Add tags to be deleted
                .addAllAbsentTags(ImmutableList.of(absentTag))
                .build())
        .build();
  }

  private static void writeMetadataToGscBucket(DumpItem dumpItem, String storageProjectId, String gcsBucketName)
      throws IOException {
    // Use Google Cloud Storage API to write metadata dump.
    // When you write real production load, you would want to shard the dump into multiple files for faster processing.
    // Contents of all the files within specified bucket will be ingested.
    Storage storage = StorageOptions.newBuilder().setProjectId(storageProjectId).build().getService();

    /* Dump files should use standard protobuf binary wire format to store Entries in file.

    Alternatively, the entire byte[] containing the wire encoding of delimited DumpItems in a single dump file can be Mime Base64 encoded. To indicate files where that is the case, please change the extension of the file from .wire to .txt.
    Note, that whole file needs to be encoded at once, instead of each DumpItem being encoded separately, and concatenated.
    For example:

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    dumpItem1.writeDelimitedTo(baos);
    dumpItem2.writeDelimitedTo(baos);
    byte[] protobufWireFormatBytes = baos.toByteArray();
    String base64EncodedStr = Base64.getMimeEncoder().encodeToString(protobufWireFormatBytes);
     */
    String gcsPath = "gs://" + gcsBucketName + "/output/" + "output_0001.wire";
    BlobId blobId = BlobId.fromGsUtilUri(gcsPath);
    BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

    ByteArrayOutputStream encodedEntries = new ByteArrayOutputStream();
    // DumpItems must be delimited, so that when system reads the file, it can tell them apart.
    // For instance, in java you can use the writeDelimitedTo method.
    dumpItem.writeDelimitedTo(encodedEntries);
    storage.create(blobInfo, encodedEntries.toByteArray());
  }

  private static void importEntriesToCatalog(String projectId, String location, String entryGroupName, String gcsBucketName)
      throws ExecutionException, InterruptedException, IOException {



    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (DataCatalogClient dataCatalogClient = DataCatalogClient.create()) {

      // Specify which EntryGroup the entries should be ingested to.
      String parent = String.format(
          "projects/%s/locations/%s/entryGroups/%s", projectId, location, entryGroupName);

      // Specify valid path to the dump stored in Google Cloud Storage
      String pathToDump = "gs://" + gcsBucketName + "/";

      // Send ImportEntries request to the Dataplex Catalog. ImportEntries is an async procedure, and it returns a long-running operation that a client can query.
      OperationFuture<ImportEntriesResponse, ImportEntriesMetadata> importEntriesFuture =
          dataCatalogClient.importEntriesAsync(ImportEntriesRequest.newBuilder()
              .setParent(parent)
              .setGcsBucketPath(pathToDump)
              .build());

      // Get a name of the long-running operation.
      String operationName = importEntriesFuture.getName();

      // Get an operation client to be able to query an operation.
      OperationsClient operationsClient = dataCatalogClient.getOperationsClient();

      // Query an operation to learn about the state of import.
      Operation longRunningOperation = operationsClient.getOperation(operationName);
      ImportEntriesMetadata importEntriesMetadata = ImportEntriesMetadata.parseFrom(longRunningOperation.getMetadata().getValue());

      System.out.printf("Long-running operation is created with name: %s \n", operationName);
      System.out.printf("Long-running operation metadata details: %s", importEntriesMetadata);

    }
  }
}
