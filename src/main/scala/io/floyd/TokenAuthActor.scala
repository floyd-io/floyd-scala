package io.floyd

import akka.actor.{Props, Actor}
import akka.pattern.{ask, pipe}
import akka.util.Timeout

import spray.routing.authentication.UserPass

import scala.concurrent.duration._
import scala.collection.mutable.Map

case class Token(token:String)

class TokenAuthActor extends Actor {

  val tokensUsers: Map[String, String] = Map()
  val authenticatorActor = context.actorOf(Props[AuthenticatorActor], "authenticator-actor")

  import context.dispatcher

  def receive = {
    case user: UserPass =>
      implicit val timeout = Timeout(5 seconds)
      val futureResult = authenticatorActor ? user
      futureResult map {
        case Some(userFound) =>
          val tokensFound = (tokensUsers filter {
            case (_, username) => username == userFound
          }).keys.toList
          tokensFound match {
            case token :: tail => token
            case Nil =>
              val newToken = java.util.UUID.randomUUID.toString()
              tokensUsers += (newToken -> user.user)
              newToken
          }
        case None =>
          throw new Exception("invalid user")
      } pipeTo sender

    case Token(token) =>
      sender ! tokensUsers.get(token)
  }

}
