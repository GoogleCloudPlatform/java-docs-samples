import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.io.compress.Compression.Algorithm;
import org.apache.hadoop.hbase.util.Bytes;

public class Example {

  // private static final String TABLE_NAME = "MY_TABLE_NAME_TOO";
  private static final String TABLE_NAME = "my-table";
  private static final String CF_DEFAULT = "DEFAULT_COLUMN_FAMILY";
  private static final byte[] COLUMN_FAMILY_NAME = Bytes.toBytes("cf1");

  public static void createOrOverwrite(Admin admin, HTableDescriptor table) throws IOException {
    if (admin.tableExists(table.getTableName())) {
      admin.disableTable(table.getTableName());
      admin.deleteTable(table.getTableName());
    }
    admin.createTable(table);
  }

  public static void createSchemaTables(Configuration config) throws IOException {
    try (Connection connection = ConnectionFactory.createConnection(config);
        Admin admin = connection.getAdmin()) {

      HTableDescriptor table = new HTableDescriptor(TableName.valueOf(TABLE_NAME));
      table.addFamily(new HColumnDescriptor(CF_DEFAULT).setCompressionType(Algorithm.NONE));

      System.out.print("Creating table. ");
      createOrOverwrite(admin, table);
      System.out.println(" Done.");
    }
  }

  public static void modifySchema(Configuration config) throws IOException {
    try (Connection connection = ConnectionFactory.createConnection(config);
        Admin admin = connection.getAdmin()) {

      TableName tableName = TableName.valueOf(TABLE_NAME);
      if (!admin.tableExists(tableName)) {
        System.out.println("Table does not exist.");
        System.exit(-1);
      }

      HTableDescriptor table = admin.getTableDescriptor(tableName);

      // Update existing table
      HColumnDescriptor newColumn = new HColumnDescriptor("NEWCF");
      newColumn.setCompactionCompressionType(Algorithm.GZ);
      newColumn.setMaxVersions(HConstants.ALL_VERSIONS);
      admin.addColumn(tableName, newColumn);

      // Update existing column family
      HColumnDescriptor existingColumn = new HColumnDescriptor(CF_DEFAULT);
      existingColumn.setCompactionCompressionType(Algorithm.GZ);
      existingColumn.setMaxVersions(HConstants.ALL_VERSIONS);
      table.modifyFamily(existingColumn);
      admin.modifyTable(tableName, table);

      // Disable an existing table
      admin.disableTable(tableName);

      // Delete an existing column family
      admin.deleteColumn(tableName, CF_DEFAULT.getBytes("UTF-8"));

      // Delete a table (Need to be disabled first)
      admin.deleteTable(tableName);
    }
  }

  public static void main(String... args) throws IOException {
    Configuration config = HBaseConfiguration.create();
    // config.set("hbase.zookeeper.quorum", "35.196.241.97");
    // config.set("hbase.master", "35.196.241.97:60000");
    // config.set("hbase.zookeeper.property.clientPort","2181");

    System.out.println("connecting");
    try (Connection connection = ConnectionFactory.createConnection(config);
        Admin admin = connection.getAdmin()) {
      System.out.println("connected");
      System.out.println("snapshotenabled?" + admin.isSnapshotCleanupEnabled());
      TableName tableName = TableName.valueOf(TABLE_NAME);

      Table table = connection.getTable(tableName);
      byte[] rowkey = Bytes.toBytes("r1");

      if (!admin.tableExists(tableName)) {
        System.out.println(tableName + " Does Not Exist");
        return;
      }

      System.out.println(tableName + " exists!");

      long timestamp = System.currentTimeMillis();
      byte[] one = new byte[] {0, 0, 0, 0, 0, 0, 0, 1};

      List<Put> puts = new ArrayList<Put>();
      puts.add(new Put(Bytes.toBytes("tablet#a0b81f74#20190501")));
      puts.add(new Put(Bytes.toBytes("tablet#a0b81f74#20190502")));

      puts.get(0)
          .addColumn(
              COLUMN_FAMILY_NAME,
              Bytes.toBytes("os_build"),
              timestamp,
              Bytes.toBytes("12155.0.0-rc1"));

      puts.get(1)
          .addColumn(
              COLUMN_FAMILY_NAME,
              Bytes.toBytes("os_build"),
              timestamp,
              Bytes.toBytes("12145.0.0-rc6"));

      table.put(puts);


      Scan rangeQuery = new Scan();

      ResultScanner rows = table.getScanner(rangeQuery);

      for (Result row : rows) {
        printRow(row);
      }
    }

    System.exit(0);
    // config.set("hbase.zookeeper.quorum", "35.196.241.97:");
    // config.set("hbase.masters", "35.196.241.97:16010");
    // Add any necessary configuration files (hbase-site.xml, core-site.xml)
    // config.addResource(new Path(System.getenv("HBASE_CONF_DIR"), "hbase-site.xml"));
    // config.addResource(new Path(System.getenv("HADOOP_CONF_DIR"), "core-site.xml"));
    // createSchemaTables(config);
    // modifySchema(config);
  }
  // public static void printResult(Result result) {
  //   System.out.println("Row: ");
  //   for (Cell cell : result.rawCells()) {
  //     byte[] family = CellUtil.cloneFamily(cell);
  //     byte[] column = CellUtil.cloneQualifier(cell);
  //     byte[] value = CellUtil.cloneValue(cell);
  //     System.out.println("\t" + Bytes.toString(family) + ":" + Bytes.toString(column) + " = " + Bytes.toString(value));
  //   }
  // }


  private static void printRow(Result row) {
    System.out.printf("Reading data for %s%n", Bytes.toString(row.getRow()));
    String colFamily = "";
    for (Cell cell : row.rawCells()) {
      String currentFamily = Bytes.toString(CellUtil.cloneFamily(cell));
      if (!currentFamily.equals(colFamily)) {
        colFamily = currentFamily;
        System.out.printf("Column Family %s%n", colFamily);
      }
      System.out.printf(
          "\t%s: %s @%s%n",
          Bytes.toString(CellUtil.cloneQualifier(cell)),
          Bytes.toString(CellUtil.cloneValue(cell)),
          cell.getTimestamp());
    }
    System.out.println();
  }
}
