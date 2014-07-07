package io.floyd.actors

import akka.actor.{Actor, ActorLogging}
import akka.pattern.pipe
import io.floyd.db.ReactiveConnection
import reactivemongo.bson._
import spray.routing.authentication.UserPass

import scala.concurrent._

class AuthenticatorActor extends Actor with ActorLogging {
  val collection = ReactiveConnection.db.apply("users")

  import context.dispatcher

  def receive = {
    case user: UserPass =>
      val query = BSONDocument(
        "username" -> user.user,
        "password" -> user.pass
      )
      val futureList: Future[List[BSONDocument]] = collection.find(query).
        cursor[BSONDocument].collect[List]()

      val futureResult: Future[Any] = futureList map {
        case userFound :: tail =>
          userFound.getAs[BSONObjectID]("_id").get.stringify
        case Nil => None
      }

      futureResult pipeTo sender
  }

}
