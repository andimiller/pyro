name := "pyro"

version := "0.1"

scalaVersion := "2.12.8"

scalacOptions += "-Ypartial-unification"

libraryDependencies += "org.typelevel" %% "cats-core" % "1.5.0"

libraryDependencies += "com.chuusai" %% "shapeless" % "2.3.3"

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.8")