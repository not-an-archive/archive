import Dependencies._

lazy val commonSettings = Seq(
  version      := "0.1.0",
  scalaVersion := ScalaLanguageVersion
)

ThisBuild / scalacOptions := Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-language:higherKinds",
  "-language:implicitConversions"
)

ThisBuild /Compile / run / fork := true

lazy val core = (project in file("na-core"))
  .settings(
    commonSettings,
    name := "na-core",
    libraryDependencies ++= platformDependencies ++ testDependencies
  )

lazy val server = (project in file("na-server"))
  .dependsOn(core)
  .settings(
    commonSettings,
    name := "na-server",
    libraryDependencies ++= platformDependencies
  )

lazy val tools = (project in file("na-tools"))
  .settings(
    commonSettings,
    name := "na-tools",
    libraryDependencies ++= platformDependencies ++ testDependencies
  )

lazy val integration = (project in file("na-it"))
  .dependsOn(server)
  .settings(
    commonSettings,
    name := "na-it",
    publish / skip := true,
    libraryDependencies ++= platformDependencies ++ testDependencies
  )
