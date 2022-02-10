ThisBuild / scalaVersion := "2.12.15"

lazy val matryoshkaVersion = "0.21.3"
lazy val validationVersion = "2.1.0"

libraryDependencies ++= Seq(
  "com.slamdata" %% "matryoshka-core" % matryoshkaVersion,
  "org.apache.avro" % "avro" % "1.8.2",
  "io.github.jto" %% "validation-core" % validationVersion,
  "io.github.jto" %% "validation-jsonast" % validationVersion,
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "com.slamdata" %% "matryoshka-scalacheck" % matryoshkaVersion % "test",
  "org.scalacheck" %% "scalacheck" % "1.14.1"
)

lazy val fakeSpark = project in file("spark")

lazy val lc2018 = project in file(".") dependsOn fakeSpark

addCompilerPlugin("org.typelevel" % "kind-projector" % "0.13.2" cross CrossVersion.full)

Test / parallelExecution := false