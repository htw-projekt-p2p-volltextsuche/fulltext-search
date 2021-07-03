val BetterMonadicForVersion  = "0.3.1"
val CatsEffectTestingVersion = "0.5.3"
val CatsRetryVersion         = "2.1.1"
val CirceVersion             = "0.13.0"
val EnumeratumCirceVersion   = "1.6.1"
val Http4sVersion            = "0.21.24"
val KindProjectorVersion     = "0.10.3"
val LogbackVersion           = "1.2.3"
val Log4CatsVersion          = "1.1.1"
val PureConfigVersion        = "0.14.1"
val ScalaTestVersion         = "3.2.7"
val SvmSubsVersion           = "20.2.0"

val buildSettings = Defaults.coreDefaultSettings ++ Seq(
  organization := "htw.ai.p2p",
  name := "speechsearch",
  version := "0.0.1-SNAPSHOT",
  scalaVersion := "2.13.6",
  fork := true,
  scalacOptions ++= Seq(
    "-target:11",
    "-feature",
    "-deprecation",
    "-unchecked",
    "-language:postfixOps",
    "-language:higherKinds",
    "-Dconfig.override_with_env_vars=true",
    "-Ymacro-annotations"
  ),
  libraryDependencies ++= Seq(
    "org.http4s" %% "http4s-blaze-server",
    "org.http4s" %% "http4s-blaze-client",
    "org.http4s" %% "http4s-circe",
    "org.http4s" %% "http4s-dsl"
  ) map (_ % Http4sVersion),
  libraryDependencies ++= Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser",
    "io.circe" %% "circe-generic-extras",
    "io.circe" %% "circe-literal"
  ) map (_ % CirceVersion),
  libraryDependencies ++= Seq(
    "com.github.pureconfig" %% "pureconfig",
    "com.github.pureconfig" %% "pureconfig-cats-effect",
    "com.github.pureconfig" %% "pureconfig-http4s"
  ) map (_ % PureConfigVersion),
  libraryDependencies ++= Seq(
    "ch.qos.logback"     % "logback-classic"  % LogbackVersion,
    "com.beachape"      %% "enumeratum-circe" % EnumeratumCirceVersion,
    "com.github.cb372"  %% "cats-retry"       % CatsRetryVersion,
    "io.chrisdavenport" %% "log4cats-slf4j"   % Log4CatsVersion,
    "org.scalameta"     %% "svm-subs"         % SvmSubsVersion
  ),
  libraryDependencies += scalaVersion("org.scala-lang" % "scala-reflect" % _).value,
  libraryDependencies ++= Seq(
    "org.scalactic"  %% "scalactic"                     % ScalaTestVersion,
    "org.scalatest"  %% "scalatest"                     % ScalaTestVersion,
    "com.codecommit" %% "cats-effect-testing-scalatest" % CatsEffectTestingVersion
  ) map (_ % Test),
  addCompilerPlugin("org.typelevel" %% "kind-projector"     % KindProjectorVersion),
  addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % BetterMonadicForVersion)
)

lazy val root: Project = (project in file("."))
  .settings(
    buildSettings ++ Seq(
      run := (run in Compile).evaluated
    )
  )
  .dependsOn(generated)
  .aggregate(generated)

lazy val generated = (project in file("generated"))
  .enablePlugins(OpenApiGeneratorPlugin)
  .settings(
    buildSettings ++ Seq(
      openApiInputSpec := "docs/openapi.yaml",
      openApiOutputDir := ".",
      openApiGeneratorName := "html2",
      openApiGenerateApiDocumentation := SettingEnabled,
      openApiValidateSpec := SettingDisabled,
      openApiGenerateModelDocumentation := SettingEnabled,
      openApiGenerateModelTests := SettingDisabled
    )
  )
