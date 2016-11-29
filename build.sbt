lazy val commonSettings = Seq(
  organization := "xyz.aoei",
  version := "1.1",
  scalaVersion := "2.11.8",

  libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0" % "test",

  libraryDependencies += "xyz.aoei" %% "msgpack-rpc-scala" % "1.4"
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "neovim-scala"
  )

lazy val bindings = (project in file("Bindings")).
  settings(commonSettings: _*).
  settings(
    name := "neovim-scala-bindings",

    libraryDependencies += "com.eed3si9n" %% "treehugger" % "0.4.1",
    libraryDependencies += "com.geirsson" %% "scalafmt" % "0.4.10"
  )

lazy val generate = taskKey[Unit]("Generate api bindings")

generate := (run in Compile in bindings).toTask("").value

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra :=
  <url>http://github.com/Chad-/neovim-scala</url>
    <licenses>
      <license>
        <name>BSD-style</name>
        <url>http://www.opensource.org/licenses/bsd-license.php</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:Chad-/neovim-scala.git</url>
      <connection>scm:git:git@github.com:Chad-/neovim-scala.git</connection>
    </scm>
    <developers>
      <developer>
        <id>Chad-</id>
        <name>Chad Morrison</name>
        <url>http://aoei.xyz</url>
      </developer>
    </developers>
