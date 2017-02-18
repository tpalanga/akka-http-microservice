
lazy val coverageSettings = Seq(
  coverageExcludedPackages := ".*Bootstrap.*"
)
lazy val commonSettings = Seq(
  organization := "com.tpalanga",
  version := "1.0",
  scalaVersion := "2.11.8",
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
) ++ coverageSettings

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

lazy val root = (project in file(".")).
  settings(
    name := "akka-http-microservice",
    commonSettings,
    libraryDependencies ++= commonDependencies ++ testingDependencies
  )

lazy val dataservice = project.
  settings(
    name := "dataservice",
    commonSettings,
    libraryDependencies ++= commonDependencies ++ testingDependencies
  )

lazy val dataserviceTest = project.
  settings(
    name := "dataserviceTest",
    commonSettings,
    libraryDependencies ++=  commonDependencies ++ testingDependencies
  )

