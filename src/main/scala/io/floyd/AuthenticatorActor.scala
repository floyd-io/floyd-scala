package io.floyd

import io.floyd.db.ReactiveConnection

import akka.actor.{Actor, ActorLogging}
import akka.pattern.pipe
import reactivemongo.bson._

import scala.concurrent._

case class User(username: String, password: String)
case class ValidUser()
case class InvalidUser()

class AuthenticatorActor extends Actor with ActorLogging {
  val collection = ReactiveConnection.db.apply("users")

  import context.dispatcher

  def receive = {
    case user: User =>
      val query = BSONDocument(
        "username" -> user.username,
        "password" -> user.password
      )
      val futureList: Future[List[BSONDocument]] = collection.find(query).
        cursor[BSONDocument].collect[List]()

      val futureResult: Future[Any] = futureList map { validUsers =>
        if (validUsers.isEmpty)
          InvalidUser
        else
          ValidUser
      }

      futureResult pipeTo sender
  }

}
