lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.12.19"
    )),
    name := "root-test"
  )

val sparkVersion = "3.0.1"

libraryDependencies ++= Seq(
  "com.fasterxml.jackson.core" % "jackson-annotations" % "2.13.5",
  "com.fasterxml.jackson.core" % "jackson-core" % "2.13.5",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.13.5",
  "com.fasterxml.jackson.dataformat" % "jackson-dataformat-xml" % "2.13.5",
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % "2.13.5",
  "io.netty" % "netty-common" % "4.1.101.Final",
  "io.netty" % "netty-handler" % "4.1.101.Final",
  "io.netty" % "netty-handler-proxy" % "4.1.101.Final",
  "io.netty" % "netty-buffer" % "4.1.101.Final",
  "io.netty" % "netty-codec" % "4.1.101.Final",
  "io.netty" % "netty-codec-http" % "4.1.101.Final",
  "io.netty" % "netty-codec-http2" % "4.1.101.Final",
  "io.netty" % "netty-transport-native-unix-common" % "4.1.101.Final",
  "io.netty" % "netty-transport-native-epoll" % "4.1.101.Final",
  "io.netty" % "netty-transport-native-kqueue" % "4.1.101.Final",
  "org.scalatest" %% "scalatest" % "3.2.2" % Test,
  "com.github.mrpowers" %% "spark-fast-tests" % "0.21.3" % "test",
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-streaming" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "com.azure" % "azure-identity" % "1.12.0",
  "com.azure" % "azure-storage-file-datalake" % "12.6.0"

)
