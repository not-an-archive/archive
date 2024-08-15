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

lazy val `na-server` = (project in file("na-server"))
  .settings(
    commonSettings,
    name := "na-server",
    libraryDependencies ++= platformDependencies
  )

lazy val `na-it` = (project in file("na-it"))
  .dependsOn(`na-server`)
  .settings(
    commonSettings,
    name := "na-it",
    publish / skip := true,
    libraryDependencies ++= platformDependencies ++ testDependencies
  )
