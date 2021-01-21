package com.digitalasset.upgrade

import akka.NotUsed
import akka.actor.ActorSystem
import akka.grpc.GrpcClientSettings
import akka.stream.scaladsl.Source
import com.daml.ledger.api.v1.{CommandCompletionService, CommandCompletionServiceClient, CompletionEndRequest, CompletionEndResponse, CompletionStreamRequest, CompletionStreamResponse}

import scala.concurrent.Future

class CommandCompletionServiceProxy(clientSettings: GrpcClientSettings)(implicit val system: ActorSystem) extends CommandCompletionService {
  private val client = CommandCompletionServiceClient(clientSettings)

  /**
   * Subscribe to command completion events.
   */
  override def completionStream(in: CompletionStreamRequest): Source[CompletionStreamResponse, NotUsed] =
    client.completionStream(in).map { completion => // .map .filter .drop ...
      // Intercept stream element, transform etc...
      completion
    }

  /**
   * Returns the offset after the latest completion.
   */
  override def completionEnd(in: CompletionEndRequest): Future[CompletionEndResponse] =
    client.completionEnd(in)

}
