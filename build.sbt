name := "movies-converter"

version := "0.1"

scalaVersion := "2.13.4"
scalacOptions += "-Ymacro-annotations"

val kantanVersion = "0.6.1"

libraryDependencies ++= Seq(
  "com.nrinaudo" %% "kantan.csv-generic" % kantanVersion,
  "com.nrinaudo" %% "kantan.csv" % kantanVersion,

)

val circeVersion = "0.12.3"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)