name := "bigtable-spark-samples"

version := "0.1"

scalaVersion := "2.11.12"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-sql" % "2.4.5" % Provided,
  "org.apache.hbase.connectors.spark" % "hbase-spark" % "1.0.0" % Provided,
  // Exception: Incompatible Jackson 2.9.2
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.9.10",
  // NoClassDefFoundError: org/apache/spark/streaming/dstream/DStream
  // when saving a DataFrame (!)
  "org.apache.spark" %% "spark-streaming" % "2.4.5" % Provided,
  "com.google.cloud.bigtable" % "bigtable-hbase-2.x-hadoop" % "1.15.0",
  // NoClassDefFoundError: org/apache/hadoop/hbase/fs/HFileSystem
  // Why?!?! The example does NOT use them directly!
  "org.apache.hbase" % "hbase-server" % "2.2.3",
  "org.apache.hbase" % "hbase-client" % "2.2.3"
)

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
    // FIXME
    MergeStrategy.first
}