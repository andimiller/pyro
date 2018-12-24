name := "pyro"

version := "0.1"

scalaVersion := "2.12.8"

scalacOptions += "-Ypartial-unification"

libraryDependencies += "org.typelevel" %% "cats-core" % "1.5.0"

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.8")