name := "sidecar"

organization := "io.medici"

version := "0.0.1"

scalaVersion := "2.11.6"
val stormVersion = "0.9.3"
val chillVersion = "0.5.1"
val bijectionVersion = "0.7.1"

scalacOptions in Test ++= Seq("-Yrangepos")

// Read here for optional dependencies:
// http://etorreborre.github.io/specs2/guide/org.specs2.guide.Runners.html#Dependencies

resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)

resolvers ++= Seq(
  Classpaths.typesafeReleases,
  "Hadoop Releases" at "https://repository.cloudera.com/content/repositories/releases/",
  "Cloudera" at "https://repository.cloudera.com/artifactory/public/",
  "Cloudera2" at "http://repository.cloudera.com/cloudera/cloudera-repos/",
  "releases" at "http://oss.sonatype.org/content/repositories/releases",
  "typesafe-repository" at "http://repo.typesafe.com/typesafe/releases/",
  "clojars-repository" at "https://clojars.org/repo",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
  // "com.twitter" %% "bijection-core" % bijectionVersion,
  // "com.twitter" %% "bijection-avro" % bijectionVersion,
  // "com.twitter" %% "chill" % chillVersion,
  // "com.twitter" %% "chill-avro" % chillVersion,
  // "com.twitter" %% "chill-bijection" % chillVersion,
  // The excludes of jms, jmxtools and jmxri are required as per https://issues.apache.org/jira/browse/KAFKA-974.
  // The exclude of slf4j-simple is because it overlaps with our use of logback with slf4j facade;  without the exclude
  // we get slf4j warnings and logback's configuration is not picked up.
  "org.apache.kafka" % "kafka_2.10" % "0.8.2.0"
    exclude("javax.jms", "jms")
    exclude("com.sun.jdmk", "jmxtools")
    exclude("com.sun.jmx", "jmxri")
    exclude("org.slf4j", "slf4j-simple")
    exclude("log4j", "log4j")
    exclude("org.apache.zookeeper", "zookeeper")
    exclude("com.101tec", "zkclient"),
  "org.apache.storm" % "storm-core" % stormVersion % "provided"
    exclude("org.apache.zookeeper", "zookeeper")
    exclude("org.slf4j", "log4j-over-slf4j"),
  "org.apache.storm" % "storm-kafka" % stormVersion
    exclude("org.apache.zookeeper", "zookeeper"),
  "com.101tec" % "zkclient" % "0.4"
    exclude("org.apache.zookeeper", "zookeeper"),
  "org.apache.curator" % "curator-test" % "2.4.0"
    exclude("org.jboss.netty", "netty")
    exclude("org.slf4j", "slf4j-log4j12"),
  "commons-io" % "commons-io" % "2.4",
  "org.apache.commons" % "commons-pool2" % "2.3",
  // Logback with slf4j facade
  "ch.qos.logback" % "logback-classic" % "1.1.2",
  // Test dependencies
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "org.mockito" % "mockito-all" % "1.9.5" % "test"
)

scalacOptions ++= Seq(
  "-feature",
  "-Xlint",
  "-Xfatal-warnings",
  "-deprecation"
)
initialCommands := "import io.medici.sidecar._"

//addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.13.0")

lazy val root = (project in file(".")).
  settings(
    name := "sidecar",
    version := "0.0.1",
    scalaVersion := "2.11.6",
    mainClass in Compile := Some("io.medici.sidecar.HelloWorldTopology")
  )
