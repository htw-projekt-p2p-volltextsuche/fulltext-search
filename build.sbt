val Http4sVersion = "0.21.16"
val CirceVersion = "0.13.0"
val MunitVersion = "0.7.20"
val LogbackVersion = "1.2.3"
val MunitCatsEffectVersion = "0.13.0"

lazy val root = (project in file("."))
  .settings(
    organization := "htw.ai.p2p",
    name := "speechsearch",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.4",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "io.circe" %% "circe-generic" % CirceVersion,
      "org.scalameta" %% "munit" % MunitVersion % Test,
      "org.typelevel" %% "munit-cats-effect-2" % MunitCatsEffectVersion % Test,
      "ch.qos.logback" % "logback-classic" % LogbackVersion,
      "org.scalameta" %% "svm-subs" % "20.2.0"
    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3"),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
    testFrameworks += new TestFramework("munit.Framework")
  )

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
