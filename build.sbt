organization  := "com.xmlite"

version       := "0.1"

scalaVersion  := "2.10.0-RC2"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.0.M4-B2" % "test" cross CrossVersion.full
)
