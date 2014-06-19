package io.floyd

import akka.actor.{Props, Actor}
import akka.pattern.{ask, pipe}
import akka.util.Timeout

import spray.routing.authentication.UserPass

import scala.concurrent.duration._
import scala.collection.mutable.Map

case class LoginUser(user:String, pass: String)
case class Token(token:String)

class TokenAuthActor extends Actor {

  val tokensUsers: Map[String, String] = Map()
  val authenticatorActor = context.actorOf(Props[AuthenticatorActor], "authenticator-actor")

  import context.dispatcher

  def receive = {
    case LoginUser(user, pass) =>
      implicit val timeout = Timeout(5 seconds)
      val futureResult = authenticatorActor ? Some(UserPass(user,pass))
      futureResult map {
        case Some(userFound) =>
          val tokensFound = tokensUsers.filter(_._2 == userFound).keys.toList
          tokensFound match {
            case token :: tail =>
              token
            case Nil =>
              val uuid = java.util.UUID.randomUUID.toString()
              tokensUsers += (uuid -> user)
              uuid
          }
        case None =>
          "invalid user"
      } pipeTo sender

    case Token(token) =>
      sender ! tokensUsers.get(token)
  }

}
