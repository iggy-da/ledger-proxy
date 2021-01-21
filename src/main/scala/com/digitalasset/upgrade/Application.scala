package com.digitalasset.upgrade

import akka.actor.ActorSystem
import akka.grpc.GrpcClientSettings
import akka.grpc.scaladsl.ServiceHandler
import akka.http.scaladsl.{Http, HttpConnectionContext}
import com.daml.ledger.api.{v1 => api}
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext


object Application extends App {

  /*
   * Starts a "proxy" gRPC server that can intercept messages between an actual ledger and a client (both directions).
   * Can be used to simulate application and network failures.
   */

  val conf = ConfigFactory
      .parseString("akka.http.server.preview.enable-http2 = on")
      .withFallback(ConfigFactory.defaultApplication())
  implicit val system: ActorSystem = ActorSystem("HelloWorld", conf)
  implicit val ec: ExecutionContext = system.dispatcher
  // Connection to actual ledger
  val settings: GrpcClientSettings = GrpcClientSettings
      .connectToServiceAt("127.0.0.1", 6865)
      .withTls(false)

  // Services that we want to proxy
  val services = List(
    // If we pass the client binding as service handler, we create a 1:1 proxy
    api.TransactionServiceHandler.partial(api.TransactionServiceClient(settings)),
    api.ActiveContractsServiceHandler.partial(api.ActiveContractsServiceClient(settings)),
    api.CommandSubmissionServiceHandler.partial(api.CommandSubmissionServiceClient(settings)),
    api.LedgerIdentityServiceHandler.partial(api.LedgerIdentityServiceClient(settings)),
    api.CommandServiceHandler.partial(api.CommandServiceClient(settings)),
    // We can use a modified client binding to intercept messages
    api.CommandCompletionServiceHandler.partial(new CommandCompletionServiceProxy(settings)),
  )

  val service = ServiceHandler.concatOrNotFound(services: _*)

  val binding = Http().bindAndHandleAsync(
    service,
    interface = "127.0.0.1",
    port = 8080, // connect to this instead of the ledger
    connectionContext = HttpConnectionContext())

  // report successful binding
  binding.foreach { binding => println(s"gRPC server bound to: ${binding.localAddress}") }

}
