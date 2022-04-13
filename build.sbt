import sbt.Keys.libraryDependencies

name := "web"

version := "1.0"

lazy val `web` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "Akka Snapshot Repository" at "https://repo.akka.io/snapshots/"

scalaVersion := "2.12.8"
scalacOptions ++= Seq("-encoding", "UTF-8")
libraryDependencies ++= Seq(ehcache, ws, specs2 % Test, guice)

libraryDependencies ++= Seq(
  "ml.dmlc" %% "xgboost4j" % "1.5.2",
  "ml.dmlc" %% "xgboost4j-spark" % "1.5.2",
  "org.apache.spark" %% "spark-mllib" % "3.2.1",
  "org.apache.spark" %% "spark-sql" % "3.2.1",
  "org.mongodb.scala" %% "mongo-scala-driver" % "4.5.1",
  "com.typesafe" % "config" % "1.4.2",
  "org.zeroturnaround" % "zt-zip" % "1.15"
)

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4"
libraryDependencies += "io.minio" % "minio" % "6.0.13"
libraryDependencies += "commons-io" % "commons-io" % "2.5"
libraryDependencies += "com.phasmidsoftware" %% "tableparser" % "1.0.14"

unmanagedBase := baseDirectory.value / "lib"
