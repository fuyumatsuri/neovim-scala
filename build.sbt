lazy val commonSettings = Seq(
  organization := "xyz.aoei",
  version := "1.0",
  scalaVersion := "2.11.8",

  libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0" % "test",

  libraryDependencies += "xyz.aoei" %% "msgpack-rpc-scala" % "1.2"
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

