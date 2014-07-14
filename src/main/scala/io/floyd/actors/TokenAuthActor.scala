package io.floyd.actors

import io.floyd.db.AuthenticatorActor

import akka.actor.{Actor, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import spray.routing.authentication.UserPass

import scala.collection.mutable.Map
import scala.concurrent.duration._

case class Token(token:String, userId: String)

class TokenAuthActor extends Actor {

  val tokensUsers: Map[String, String] = Map()
  val authenticatorActor = context.actorOf(Props[AuthenticatorActor], "authenticator-actor")

  import context.dispatcher

  def receive = {
    case user: UserPass =>
      implicit val timeout = Timeout(5 seconds)
      val futureResult = authenticatorActor ? user
      futureResult map {
        case id: String =>
          val newToken = java.util.UUID.randomUUID.toString()
          tokensUsers += (newToken -> id)
          newToken -> id
        case None =>
          throw new Exception("invalid user")
      } pipeTo sender

    case token:String =>
      sender ! tokensUsers.get(token)
  }

}
