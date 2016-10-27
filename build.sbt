name := "interpolation-as-a-service"
version := "0.1.0"
scalaVersion := "2.11.8"

organization := "com.azavea"
licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html"))

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-Yinline-warnings",
  "-language:implicitConversions",
  "-language:reflectiveCalls",
  "-language:higherKinds",
  "-language:postfixOps",
  "-language:existentials",
  "-feature")

publishMavenStyle := true
publishArtifact in Test := false
pomIncludeRepository := { _ => false }

resolvers += Resolver.bintrayRepo("azavea", "geotrellis")

Revolver.settings

libraryDependencies ++= Seq(
  "com.azavea.geotrellis" %% "geotrellis-raster"                 % Version.geotrellis,
  "com.typesafe.akka"     %% "akka-actor"                        % Version.akka,
  "com.typesafe.akka"     %% "akka-http-experimental"            % Version.akka,
  "com.typesafe.akka"     %% "akka-http-spray-json-experimental" % Version.akka,
  "io.circe"          %% "circe-core"                        % Version.circe,
  "io.circe"          %% "circe-generic"                     % Version.circe,
  "io.circe"          %% "circe-parser"                      % Version.circe,
  "de.heikoseeberger" %% "akka-http-circe"                   % Version.akkaCirce,
  "com.iheart"        %% "ficus"                             % Version.ficus,
  "org.scalatest"         %%  "scalatest"                        % "2.2.0" % "test"
)

// When creating fat jar, remote some files with
// bad signatures and resolve conflicts by taking the first
// versions of shared packaged types.
assemblyMergeStrategy in assembly := {
  case "reference.conf" => MergeStrategy.concat
  case "application.conf" => MergeStrategy.concat
  case "META-INF/MANIFEST.MF" => MergeStrategy.discard
  case "META-INF\\MANIFEST.MF" => MergeStrategy.discard
  case "META-INF/ECLIPSEF.RSA" => MergeStrategy.discard
  case "META-INF/ECLIPSEF.SF" => MergeStrategy.discard
  case _ => MergeStrategy.first
}
