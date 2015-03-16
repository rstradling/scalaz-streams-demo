name := "scalaz-streams-demo"

version := "1.0"

scalaVersion := "2.11.6"

resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"

libraryDependencies += "org.scalaz.stream" %% "scalaz-stream" % "0.6"

libraryDependencies += "com.rabbitmq" % "amqp-client" % "3.5.0"

libraryDependencies += "io.argonaut" %% "argonaut" % "6.0.4"
    