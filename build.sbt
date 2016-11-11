name := "neovim-scala"

organization := "xyz.aoei"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies += "xyz.aoei" %% "msgpack-rpc-scala" % "1.1"

libraryDependencies += "com.eed3si9n" %% "treehugger" % "0.4.1"

resolvers += Resolver.sonatypeRepo("public")
