package io.floyd

import akka.actor.{Actor, ActorLogging}
import akka.pattern.pipe

import reactivemongo.api._
import reactivemongo.bson._

import scala.concurrent._
import scala.util.{Success, Failure}

case class User(userName: String, password: String)
case class ValidUser()
case class InvalidUser()

class AuthenticatorActor extends Actor with ActorLogging {
  // gets an instance of the driver
  // (creates an actor system)
  val config = context.system.settings.config

  val driver = new MongoDriver
  val connection = driver.connection(List(config.getString("floyd.database.hostname")))
  import context.dispatcher

  val db = connection(config.getString("floyd.database.name"))
  val collection = db("users")

  def receive = {
    case user: User =>
      val query = BSONDocument(
        "userName" -> user.userName,
        "password" -> user.password
      )
      val futureList: Future[List[BSONDocument]] = collection.find(query).cursor[BSONDocument].collect[List]()
      val promise:Promise[Any] = Promise()

      futureList onComplete {
        case Success(validUsers) =>
          if (validUsers.isEmpty){
            promise success InvalidUser
          }
          else{
            promise success ValidUser
          }
        case Failure(t) =>
          promise failure new Exception("failed on retrieve from DB", t)
      }
      promise.future pipeTo sender

  }

}
