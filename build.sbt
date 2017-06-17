lazy val coverageSettings = Seq(
  coverageExcludedPackages := ".*Bootstrap.*"
)
lazy val commonSettings = Seq(
  organization := "com.tpalanga",
  version := "1.0-SNAPSHOT",
  scalaVersion := "2.11.8",
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
) ++ coverageSettings

lazy val dockerSettings = Seq(
  dockerBaseImage := "openjdk:alpine",
  dockerUpdateLatest in Docker := true
)

val akkaVersion = "2.4.17"
val akkaHttpVersion = "10.0.3"
val scalaTestVersion = "3.0.1"

val commonDependencies = Seq (
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "io.spray" %%  "spray-json" % "1.3.3"
)
val testingDependencies = Seq(
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % "test",
  "org.scalactic" %% "scalactic" % scalaTestVersion % "test",
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
)

lazy val accountService = project.
  settings(
    name := "accountService",
    commonSettings,
    libraryDependencies ++= commonDependencies ++ testingDependencies,
    dockerSettings,
    dockerExposedPorts := Seq(8080)
  )
  .dependsOn(testLib)
  .enablePlugins(JavaAppPackaging, DockerPlugin, AshScriptPlugin)

lazy val accountServiceTest = project.
  settings(
    name := "accountServiceTest",
    commonSettings,
    libraryDependencies ++=  commonDependencies ++ testingDependencies
  )
  .dependsOn(testLib % "test")

lazy val newsletterService = project.
  settings(
    name := "newsletterService",
    commonSettings,
    libraryDependencies ++= commonDependencies ++ testingDependencies,
    dockerSettings,
    dockerExposedPorts := Seq(8081)
  )
  .enablePlugins(JavaAppPackaging, DockerPlugin, AshScriptPlugin)

lazy val newsletterServiceTest = project.
  settings(
    name := "newsletterServiceTest",
    commonSettings,
    libraryDependencies ++=  commonDependencies ++ testingDependencies
  )
  .dependsOn(testLib % "test")

lazy val testLib = project.
  settings(
    name := "testLib",
    commonSettings,
    libraryDependencies ++=  commonDependencies
  )

