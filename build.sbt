name := "pyro"

version := "0.6"

scalaVersion := "2.12.8"

scalacOptions += "-Ypartial-unification"

scalafmtConfig in ThisBuild := Some(file("scalafmt.conf"))

lazy val pyro = project.in(file("pyro")).settings(
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-effect" % "1.2.0"
  )
)

lazy val bananasCore = project.in(file("bananas/core")).settings(
  name := "bananas-core",
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-effect" % "1.2.0",
    "co.fs2" %% "fs2-core" % "1.0.3"
  )
)

lazy val bananasPlugin = project.in(file("bananas/plugin")).settings(
  name := "bananas-plugin",
  sbtPlugin := true,
  libraryDependencies ++= Seq(
    "com.lihaoyi" %% "fansi" % "0.2.5"
  )
).dependsOn(bananasCore)


//libraryDependencies += "com.chuusai" %% "shapeless" % "2.3.3"
//libraryDependencies += "com.lihaoyi" %% "sourcecode" % "0.1.5"

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.8")

