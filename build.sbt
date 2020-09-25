// Project
inThisBuild(
  Seq(
    scalaVersion := "2.13.2",
    version := "0.2.0-SNAPSHOT",
    organization := "computer.hollis",
    organizationName := "hollis",
    organizationHomepage := Some(url("http://hollis.computer/")),
    description := "A functional web application framework for Scala",
    licenses := List("BSD-3" -> new URL("https://github.com/davidhollis/compose/blob/main/LICENSE")),
    homepage := Some(url("https://compose.hollis.computer/")),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/davidhollis/compose"),
        "scm:git@github.com:davidhollis/compose.git",
      )
    ),
    developers := List(
      Developer(
        id = "hollis",
        name = "David Hollis",
        email = "david@davidhollis.net",
        url = url("https://davidhollis.net/"),
      )
    ),
  )
)

// Publishing and Signing
ThisBuild / pgpSigningKey := Credentials
  .forHost(credentials.value, s"${organization.value}.gpg")
  .map(_.userName)
ThisBuild / publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
ThisBuild / publishMavenStyle := true

lazy val root = (project in file("."))
  .settings(lintSettings)
  .settings(skip in publish := true)
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
      "com.typesafe.play" %% "play-json" % "2.9.1",
      "com.typesafe.play" %% "twirl-api" % "1.5.0",
      "org.scalatest" %% "scalatest" % "3.1.1" % Test,
    ),
  )
  .settings(lintSettings)

lazy val demos = (project in file("demos"))
  .dependsOn(core)
  .settings(
    name := "compose-demos"
  )
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
