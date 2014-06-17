package io.floyd

import io.floyd.db.ReactiveConnection

import akka.actor.{Actor, ActorLogging}
import akka.pattern.pipe
import spray.routing.authentication.UserPass
import reactivemongo.bson._

import scala.concurrent._

class AuthenticatorActor extends Actor with ActorLogging {
  val collection = ReactiveConnection.db.apply("users")

  import context.dispatcher

  def receive = {
    case Some(user: UserPass) =>
      val query = BSONDocument(
        "username" -> user.user,
        "password" -> user.pass
      )
      val futureList: Future[List[BSONDocument]] = collection.find(query).
        cursor[BSONDocument].collect[List]()

      val futureResult: Future[Any] = futureList map {
        case userFound :: tail => Some(user.user)
        case Nil => None
      }

      futureResult pipeTo sender
    case None =>
      sender ! None
  }

}
