organization  := "io.floyd"

version       := "0.1"

scalaVersion  := "2.10.3"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaV = "2.3.2"
  val sprayV = "1.3.1"
  Seq(
    "io.spray"            %   "spray-can"      % sprayV,
    "io.spray"            %   "spray-routing"  % sprayV,
    "io.spray"            %   "spray-testkit"  % sprayV  % "test",
    "com.typesafe.akka"   %%  "akka-actor"     % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"   % akkaV   % "test",
    "org.scalatest"       %   "scalatest_2.10" % "2.1.6" % "test"
  )
}

Revolver.settings
