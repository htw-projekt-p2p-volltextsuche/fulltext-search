import sbt.addCompilerPlugin

val BetterMonadicForVersion  = "0.3.1"
val CatsEffectTestingVersion = "0.5.3"
val CirceVersion             = "0.13.0"
val EnumeratumCirceVersion   = "1.7.0"
val Http4sVersion            = "0.21.24"
val KindProjectorVersion     = "0.10.3"
val LogbackVersion           = "1.2.3"
val ScalaTestVersion         = "3.2.7"
val SvmSubsVersion           = "20.2.0"

val buildSettings = Defaults.coreDefaultSettings ++ Seq(
  organization := "htw.ai.p2p",
  name := "speechsearch",
  version := "0.0.1-SNAPSHOT",
  scalacOptions ++= Seq("Ymacro-annotations"),
  scalaVersion := "2.13.6",
  libraryDependencies ++= Seq(
    "org.http4s" %% "http4s-blaze-server",
    "org.http4s" %% "http4s-blaze-client",
    "org.http4s" %% "http4s-circe",
    "org.http4s" %% "http4s-dsl"
  ).map(_ % Http4sVersion),
  libraryDependencies ++= Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser",
    "io.circe" %% "circe-generic-extras",
    "io.circe" %% "circe-literal"
  ).map(_ % CirceVersion),
  libraryDependencies ++= Seq(
    "ch.qos.logback"  % "logback-classic"               % LogbackVersion,
    "org.scalameta"  %% "svm-subs"                      % SvmSubsVersion,
    "com.beachape"   %% "enumeratum-circe"              % EnumeratumCirceVersion,
    "org.scalactic"  %% "scalactic"                     % ScalaTestVersion         % Test,
    "org.scalatest"  %% "scalatest"                     % ScalaTestVersion         % Test,
    "com.codecommit" %% "cats-effect-testing-scalatest" % CatsEffectTestingVersion % Test
  ),
  addCompilerPlugin("org.typelevel" %% "kind-projector"     % KindProjectorVersion),
  addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % BetterMonadicForVersion)
)

lazy val root: Project = (project in file("."))
  .settings(
    buildSettings ++ Seq(
      run := (run in Compile in core).evaluated
    )
  )

lazy val macros: Project = (project in file("macros"))
  .settings(
    buildSettings ++ Seq(
      libraryDependencies += scalaVersion(
        "org.scala-lang" % "scala-reflect" % _
      ).value
    )
  )

lazy val core: Project =
  (project in file("core")) settings buildSettings dependsOn macros
