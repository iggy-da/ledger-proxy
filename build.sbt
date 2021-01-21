name := "ledger-proxy"

version := "0.1"

scalaVersion := "2.12.12"

enablePlugins(AkkaGrpcPlugin)

val sdkVersion = "1.7.0"

libraryDependencies += "com.daml" %% "ledger-api-scalapb" % sdkVersion % "protobuf-src" intransitive()
libraryDependencies += "com.thesamet.scalapb.common-protos" %% "proto-google-common-protos-scalapb_0.10" % "1.17.0-0" % "protobuf-src"

akkaGrpcGeneratedSources := Seq(AkkaGrpc.Client, AkkaGrpc.Server)
