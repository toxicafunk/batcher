name := "batcher"
version := "0.1-SNAPSHOT"
scalaVersion in ThisBuild := "2.13.1"

fork in Test := false
fork in run := true

val zioVersion        = "1.0.0-RC18-1"

libraryDependencies ++=  Seq(
  "dev.zio"    %% "zio"              % zioVersion,
  "dev.zio"    %% "zio-streams"              % zioVersion
)
