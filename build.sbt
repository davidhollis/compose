ThisBuild / scalaVersion     := "2.13.2"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "computer.hollis"
ThisBuild / organizationName := "hollis"

lazy val root = (project in file("."))
  .settings(
    name := "compose",
    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % "1.4.0",
      "com.iheart" %% "ficus" % "1.4.7",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
      "org.scalatest" %% "scalatest" % "3.1.1" % Test,
    ),
    scalacOptions ++= Seq(
      "-deprecation",
    ),
  )
