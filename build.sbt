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
  "org.mongodb" % "mongo-java-driver" % "3.12.10",
  "dev.morphia.morphia" % "morphia-core" % "2.2.6",
  "com.typesafe" % "config" % "1.4.2",
  "org.zeroturnaround" % "zt-zip" % "1.15"
)

libraryDependencies += "org.mongodb.scala" %% "mongo-scala-driver" % "2.8.0"
libraryDependencies += "cn.playscala" % "play-mongo_2.12" % "0.3.0"
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4"
libraryDependencies += "io.minio" % "minio" % "6.0.13"
libraryDependencies += "commons-io" % "commons-io" % "2.5"
libraryDependencies += "com.phasmidsoftware" %% "tableparser" % "1.0.14"

unmanagedBase := baseDirectory.value / "lib"
