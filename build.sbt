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

lazy val core = (project in file("naa-core"))
  .settings(
    commonSettings,
    name := "naa-core",
    libraryDependencies ++= platformDependencies ++ testDependencies
  )

lazy val server = (project in file("naa-server"))
  .dependsOn(core)
  .settings(
    commonSettings,
    name := "naa-server",
    libraryDependencies ++= platformDependencies
  )

lazy val tools = (project in file("naa-tools"))
  .settings(
    commonSettings,
    name := "naa-tools",
    libraryDependencies ++= platformDependencies ++ testDependencies
  )

lazy val integration = (project in file("naa-it"))
  .dependsOn(server)
  .settings(
    commonSettings,
    name := "naa-it",
    publish / skip := true,
    libraryDependencies ++= platformDependencies ++ testDependencies
  )
