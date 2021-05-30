import sbt.addCompilerPlugin

val Http4sVersion = "0.21.16"
val CirceVersion = "0.13.0"
val MunitVersion = "0.7.20"
val LogbackVersion = "1.2.3"
val MunitCatsEffectVersion = "0.13.0"
val ScalaTestVersion = "3.2.7"

val buildSettings = Defaults.coreDefaultSettings ++ Seq(
  organization := "htw.ai.p2p",
  name := "speechsearch",
  version := "0.0.1-SNAPSHOT",

  scalacOptions ++= Seq("Ymacro-annotations"),
  scalaVersion := "2.13.4",

  libraryDependencies ++= Seq(
    "org.http4s" %% "http4s-blaze-server",
    "org.http4s" %% "http4s-blaze-client",
    "org.http4s" %% "http4s-circe",
    "org.http4s" %% "http4s-dsl"
  ).map(_ % Http4sVersion),

  libraryDependencies ++= Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser"
  ).map(_ % CirceVersion),

  libraryDependencies ++= Seq(
    "org.scalameta" %% "munit" % MunitVersion % Test,
    "org.typelevel" %% "munit-cats-effect-2" % MunitCatsEffectVersion % Test,
    "ch.qos.logback" % "logback-classic" % LogbackVersion,
    "org.scalameta" %% "svm-subs" % "20.2.0",
    "org.scalactic" %% "scalactic" % ScalaTestVersion,
    "org.scalatest" %% "scalatest" % ScalaTestVersion % "test"
  ),

  addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3"),
  addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),

  testFrameworks += new TestFramework("munit.Framework")
)

lazy val root: Project = (project in file("."))
  .settings(buildSettings ++ Seq(
    run:=(run in Compile in core).evaluated
  ))

lazy val macros: Project = (project in file("macros"))
  .settings(buildSettings ++ Seq(
    libraryDependencies += scalaVersion("org.scala-lang" % "scala-reflect" % _).value
  ))

lazy val core: Project = (project in file("core")) settings buildSettings dependsOn macros

// TODO: generate model from api spec if possible
// ./project/plugins.sbt:
// addSbtPlugin("org.openapitools" % "sbt-openapi-generator" % "5.0.1")

// val OpenapiGeneratorVersion = "5.1.1"
// libraryDependencies += "org.openapitools" % "openapi-generator" % OpenapiGeneratorVersion
// lazy val generated = (project in file("generated"))
//   .settings(
//     openApiGeneratorName := "http4s",
//     openApiInputSpec := "openapi.yaml",
//     openApiApiPackage := "htw.ai.p2p.speechsearch.api",
//     openApiModelPackage := "htw.ai.p2p.speechsearch.model"
//   )
