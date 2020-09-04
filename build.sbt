lazy val projectSettings = Seq(
  scalaVersion := "2.13.2",
  version := "0.1.0-SNAPSHOT",
  organization := "computer.hollis",
  organizationName := "hollis",
)

lazy val root = (project in file("."))
  .settings(projectSettings)
  .settings(lintSettings)
  .aggregate(core, demos)
  .dependsOn(core, demos)

lazy val core = (project in file("core"))
  .settings(
    name := "compose",
    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % "1.4.0",
      "com.iheart" %% "ficus" % "1.4.7",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
      "io.lemonlabs" %% "scala-uri" % "2.3.0",
      "org.scalatest" %% "scalatest" % "3.1.1" % Test,
    ),
  )
  .settings(projectSettings)
  .settings(lintSettings)

lazy val demos = (project in file("demos"))
  .dependsOn(core)
  .settings(
    name := "compose-demos"
  )
  .settings(projectSettings)
  .settings(lintSettings)

ThisBuild / scalafixScalaBinaryVersion := "2.13"

lazy val lintSettings = Seq(
  scalacOptions ++= Seq(
    "-deprecation",
    "-Xlint:unused",
    "-Wdead-code",
  ),
  scalacOptions in (Compile, console) ~= { _.filterNot(Set("-Xlint:unused")) },
  scalacOptions in (Test, console) := (scalacOptions in (Compile, console)).value,
  semanticdbEnabled := true,
  semanticdbVersion := scalafixSemanticdb.revision,
)
