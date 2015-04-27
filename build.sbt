name := "scalaz-streams-demo"

version := "1.0"

scalaVersion := "2.11.6"

lazy val doobieVersion = "0.2.2-SNAPSHOT"

resolvers ++= Seq("Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
                  "tpolecat" at "http://dl.bintray.com/tpolecat/maven")

libraryDependencies ++= Seq(
  "org.scalaz.stream" %% "scalaz-stream" % "0.7a",
  "org.scalaz" %% "scalaz-concurrent" % "7.1.1",
  "org.scalaz" %% "scalaz-core" % "7.1.1",
  "org.scalaz" %% "scalaz-effect" % "7.1.1",
  "com.rabbitmq" % "amqp-client" % "3.5.0",
  "io.argonaut" %% "argonaut" % "6.1-M4",
  "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
  "org.tpolecat" %% "doobie-core" % doobieVersion,
  "com.typesafe" % "config" % "1.2.1")
    