package io.floyd.db

import akka.actor.{Status, ActorLogging, Actor}
import akka.pattern.pipe
import spray.routing.authentication.UserPass
import reactivemongo.bson.{BSONObjectID, BSONDocument}

case class UserRegistered()
case class UserNotRegistered()

class UserRegisterActor extends Actor with ActorLogging {

  val users = ReactiveConnection.db.apply("users")

  import context.dispatcher

  override def receive: Receive = {
    case user: UserPass =>
      if (user.pass.length() < 8) {
        log.debug("invalid password. Sending exception")
        sender() ! Status.Failure(new Exception("password too short"))
      } else {
        val userDocument = BSONDocument(
          "_id" -> BSONObjectID.generate,
          "username" -> user.user,
          "password" -> user.pass
        )

        users.insert(userDocument) map { lastError =>
          UserRegistered
        } pipeTo sender()
      }


  }
}
