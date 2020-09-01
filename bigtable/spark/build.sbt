name := "bigtable-spark-samples"

version := "0.1"

scalaVersion := "2.11.12"

// Versions to match Dataproc
val sparkVersion = "2.4.5"
val hbaseVersion = "2.2.3"
val bigtableVersion = "1.15.0"
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-sql" % sparkVersion % Provided,
  "org.apache.hbase.connectors.spark" % "hbase-spark" % "1.0.0" % Provided,
  "com.google.cloud.bigtable" % "bigtable-hbase-2.x-hadoop" % bigtableVersion
)

// Extra dependency for command line option parsing
// https://github.com/scopt/scopt
libraryDependencies += "com.github.scopt" %% "scopt" % "4.0.0-RC2"

val scalatestVersion = "3.2.0"
libraryDependencies += "org.scalactic" %% "scalactic" % scalatestVersion
libraryDependencies += "org.scalatest" %% "scalatest" % scalatestVersion % "test"
resolvers += "Artima Maven Repository" at "https://repo.artima.com/releases"
test in assembly := {}

val fixes = Seq(
  // Fix for Exception: Incompatible Jackson 2.9.2
  // Version conflict between HBase and Spark
  // Forcing the version to match Spark
  // FIXME Would that work with dependencyOverrides?
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.9.10",
  // Fix for NoClassDefFoundError: org/apache/spark/streaming/dstream/DStream
  // when saving a DataFrame
  "org.apache.spark" %% "spark-streaming" % sparkVersion % Provided,
  // Fix for NoClassDefFoundError: org/apache/hadoop/hbase/fs/HFileSystem
  // Why?!?! The example does NOT use them directly!
  "org.apache.hbase" % "hbase-server" % hbaseVersion,
  "org.apache.hbase" % "hbase-client" % hbaseVersion
)
libraryDependencies ++= fixes

// Excluding duplicates for the uber-jar
// There are other deps to provide necessary packages
excludeDependencies ++= Seq(
  ExclusionRule(organization = "asm", "asm"),
  ExclusionRule(organization = "commons-beanutils", "commons-beanutils"),
  ExclusionRule(organization = "commons-beanutils", "commons-beanutils-core"),
  ExclusionRule(organization = "org.mortbay.jetty", "servlet-api")
)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.first
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case PathList("mozilla", "public-suffix-list.txt") => MergeStrategy.first
  case PathList("google", xs @ _*) => xs match {
    case ps @ (x :: xs) if ps.last.endsWith(".proto") => MergeStrategy.first
    case _ => MergeStrategy.deduplicate
  }
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
    // FIXME Make sure first is OK (it's worked well so far)
    MergeStrategy.first
}
