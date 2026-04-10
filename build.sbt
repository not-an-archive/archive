import Dependencies._

ThisBuild / version        := "0.1.0"
ThisBuild / scalaVersion   := "3.8.3"
ThisBuild / scalacOptions ++= Seq(
  "-encoding", "utf8",
  "-feature",
  "-language:implicitConversions",
  "-language:existentials",
  "-unchecked",
  "-Werror",
  "-deprecation"
)

lazy val core = (project in file("naa-core"))
  .settings(
    name := "naa-core",
    libraryDependencies ++= platformDependencies ++ testDependencies
  )

lazy val server = (project in file("naa-server"))
  .dependsOn(core)
  .settings(
    name := "naa-server",
    libraryDependencies ++= platformDependencies
  )

lazy val tools = (project in file("naa-tools"))
  .settings(
    name := "naa-tools",
    libraryDependencies ++= platformDependencies ++ testDependencies
  )

lazy val integration = (project in file("naa-it"))
  .dependsOn(server)
  .settings(
    name := "naa-it",
    publish / skip := true,
    libraryDependencies ++= platformDependencies ++ testDependencies
  )

lazy val archive = (project in file("."))
  .aggregate(core, tools, server, integration)
