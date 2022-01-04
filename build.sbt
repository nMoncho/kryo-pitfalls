import Dependencies._

ThisBuild / scalaVersion := "2.13.7"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "kryo-playground",
    logBuffered := false,
    Test / parallelExecution := false,
    libraryDependencies += "com.twitter" %% "chill" % "0.9.5",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.10" % Test,
    libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.15.4" % Test,
    libraryDependencies += "org.scalatestplus" %% "scalacheck-1-14" % "3.2.2.0" % Test,
    libraryDependencies += "com.storm-enroute" %% "scalameter" % "0.19" % Test,
    testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework")
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
