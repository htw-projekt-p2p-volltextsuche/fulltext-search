val BetterMonadicForVersion  = "0.3.1"
val CatsEffectTestingVersion = "0.5.3"
val CatsMtlVersion           = "1.2.0"
val CatsRetryVersion         = "2.1.1"
val CirceVersion             = "0.13.0"
val EnumeratumCirceVersion   = "1.6.1"
val Http4sVersion            = "0.21.24"
val KindProjectorVersion     = "0.13.0"
val LogbackVersion           = "1.2.3"
val Log4CatsVersion          = "1.1.1"
val MeowMtlVersion           = "0.4.0"
val PureConfigVersion        = "0.16.0"
val ScalaInflectorVersion    = "1.4.0"
val ScalaMockVersion         = "5.1.0"
val ScalaTestVersion         = "3.2.7"
val SvmSubsVersion           = "20.2.0"

val buildSettings = Defaults.coreDefaultSettings ++ Seq(
  organization := "htw.ai.p2p",
  name := "speechsearch",
  version := "0.0.1-SNAPSHOT",
  scalaVersion := "2.13.6",
  fork := true,
  javaOptions += "-Dconfig.override_with_env_vars=true",
  scalacOptions ++= Seq(
    "-target:11",
    "-feature",
    "-deprecation",
    "-unchecked",
    "-language:postfixOps",
    "-language:higherKinds",
    "-Ymacro-annotations"
  ),
  initialize ~= { _ =>
    System.setProperty("config.override_with_env_vars", "true")
  },
  libraryDependencies ++= Seq(
    "http4s-blaze-server",
    "http4s-blaze-client",
    "http4s-circe",
    "http4s-dsl"
  ) map ("org.http4s" %% _ % Http4sVersion),
  libraryDependencies ++= Seq(
    "circe-core",
    "circe-generic",
    "circe-parser",
    "circe-generic-extras",
    "circe-literal"
  ) map ("io.circe" %% _ % CirceVersion),
  libraryDependencies ++= Seq(
    "pureconfig",
    "pureconfig-http4s"
  ) map ("com.github.pureconfig"         %% _          % PureConfigVersion),
  libraryDependencies += "org.typelevel" %% "cats-mtl" % CatsMtlVersion,
  libraryDependencies ++= Seq(
    "meow-mtl-core",
    "meow-mtl-effects",
    "meow-mtl-monix"
  ) map ("com.olegpy" %% _ % MeowMtlVersion),
  libraryDependencies ++= Seq(
    "ch.qos.logback"     % "logback-classic"  % LogbackVersion,
    "com.beachape"      %% "enumeratum-circe" % EnumeratumCirceVersion,
    "com.github.cb372"  %% "cats-retry"       % CatsRetryVersion,
    "io.chrisdavenport" %% "log4cats-slf4j"   % Log4CatsVersion,
    "org.scalameta"     %% "svm-subs"         % SvmSubsVersion,
    "com.qvantel"       %% "scala-inflector"  % ScalaInflectorVersion
  ),
  libraryDependencies += scalaVersion("org.scala-lang" % "scala-reflect" % _).value,
  libraryDependencies ++= Seq(
    "org.scalactic"  %% "scalactic"                     % ScalaTestVersion,
    "org.scalatest"  %% "scalatest"                     % ScalaTestVersion,
    "org.scalamock"  %% "scalamock"                     % ScalaMockVersion,
    "com.codecommit" %% "cats-effect-testing-scalatest" % CatsEffectTestingVersion
  ) map (_ % Test),
  addCompilerPlugin("com.olegpy" %% "better-monadic-for" % BetterMonadicForVersion),
  addCompilerPlugin(
    "org.typelevel" %% "kind-projector" % KindProjectorVersion cross CrossVersion.full
  )
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
