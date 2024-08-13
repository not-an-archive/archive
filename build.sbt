import Dependencies._

lazy val commonSettings = Seq(
  version      := "0.1.0",
  scalaVersion := ScalaLanguageVersion
)

scalacOptions := Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-language:higherKinds",
  "-language:implicitConversions"
)

lazy val org = (project in file("."))
  .settings(
    commonSettings,
    name := "archive",
    libraryDependencies ++= platformDependencies
  )

lazy val it = (project in file("it"))
  .dependsOn(org)
  .settings(
    commonSettings,
    name := "archive-it",
    publish / skip := true,
    libraryDependencies ++= platformDependencies ++ testDependencies
  )
