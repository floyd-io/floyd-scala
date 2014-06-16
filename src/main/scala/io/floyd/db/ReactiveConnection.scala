package io.floyd.db

import com.typesafe.config.{ConfigFactory, Config}
import reactivemongo.api.MongoDriver

object ReactiveConnection {
  val config: Config = ConfigFactory.load();

  // gets an instance of the driver
  // (creates an actor system)
  val driver = new MongoDriver

  val connection = driver.connection(List(config.getString("floyd.database.hostname")))

  import concurrent.ExecutionContext.Implicits.global
  val db = connection(config.getString("floyd.database.name"))

}