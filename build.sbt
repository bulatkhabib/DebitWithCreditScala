import Dependencies.Worker

import scala.collection.Seq

ThisBuild / libraryDependencies ++= Seq(
  compilerPlugin("org.typelevel" %% "kind-projector"     % "0.13.2" cross CrossVersion.full),
  compilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1")
)

ThisBuild / scalaVersion := "2.13.12"
ThisBuild / scalacOptions ++= Seq(
  "-Ymacro-annotations",
  "-language:higherKinds",
  "-unchecked",
  "-feature",
  "-encoding",
  "UTF-8"
)

lazy val `worker` = (project in file("worker"))
  .settings(
    name := "worker",
    libraryDependencies ++= Worker.dependencies,
    dependencyOverrides ++= Worker.overridingDependencies,
  )

lazy val `worker-root` =
  (project in file("."))
    .aggregate(`worker`)