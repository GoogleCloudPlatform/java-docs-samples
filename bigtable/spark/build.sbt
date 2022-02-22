/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

name := "bigtable-spark-samples"

version := "0.1"

// Versions to match Dataproc 1.4
// https://cloud.google.com/dataproc/docs/concepts/versioning/dataproc-release-1.4
scalaVersion := "2.11.12"
val sparkVersion = "2.4.8"
val bigtableVersion = "2.0.0"
val hbaseVersion = "2.4.8"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-sql" % sparkVersion % Provided,
  "org.apache.hbase.connectors.spark" % "hbase-spark" % "1.0.0" % Provided,
  "com.google.cloud.bigtable" % "bigtable-hbase-2.x-hadoop" % bigtableVersion
)

val scalatestVersion = "3.2.6"
libraryDependencies += "org.scalactic" %% "scalactic" % scalatestVersion
libraryDependencies += "org.scalatest" %% "scalatest" % scalatestVersion % "test"
test in assembly := {}

val fixes = Seq(
  // Required by 'value org.apache.hadoop.hbase.spark.HBaseContext.dstream'
  "org.apache.spark" %% "spark-streaming" % sparkVersion % Provided,
  // hbase-server is needed because HBaseContext references org/apache/hadoop/hbase/fs/HFileSystem
  // hbase-client is declared to override the version of hbase-client declared by bigtable-hbase-2.x-hadoop
  "org.apache.hbase" % "hbase-server" % hbaseVersion,
  "org.apache.hbase" % "hbase-client" % hbaseVersion
)
libraryDependencies ++= fixes

// Fix for Exception: Incompatible Jackson 2.9.2
// Version conflict between HBase and Spark
// Forcing the version to match Spark
dependencyOverrides += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.12.1"

// Excluding duplicates for the uber-jar
// There are other deps to provide necessary packages
excludeDependencies ++= Seq(
  ExclusionRule(organization = "asm", "asm"),
  ExclusionRule(organization = "commons-beanutils", "commons-beanutils"),
  ExclusionRule(organization = "commons-beanutils", "commons-beanutils-core"),
  ExclusionRule(organization = "org.mortbay.jetty", "servlet-api")
)

ThisBuild / assemblyMergeStrategy := {
  case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.first
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case PathList("META-INF", "native", xs @ _*)         => MergeStrategy.first
  case PathList("META-INF", "native-image", xs @ _*)         => MergeStrategy.first
  case PathList("mozilla", "public-suffix-list.txt")         => MergeStrategy.first
  case PathList("google", xs @ _*) => xs match {
    case ps @ (x :: xs) if ps.last.endsWith(".proto") => MergeStrategy.first
    case _ => MergeStrategy.deduplicate
  }
  case PathList("javax", xs @ _*)         => MergeStrategy.first
  case PathList("io", "netty", xs @ _*)         => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".proto" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith "module-info.class" => MergeStrategy.discard
  case x =>
    val oldStrategy = (ThisBuild / assemblyMergeStrategy).value
    oldStrategy(x)
}
