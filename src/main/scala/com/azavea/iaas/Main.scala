package com.azavea.iaas

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

object AkkaSystem {
  implicit val system = ActorSystem("iaas-system")
  implicit val materializer = ActorMaterializer()

  trait LoggerExecutor {
    protected implicit val log = Logging(system, "app")
  }
}

object Main extends Router with Config {

  def main(args: Array[String]): Unit = {
    import AkkaSystem._

    Http().bindAndHandle(routes, httpConfig.interface, httpConfig.port)
  }
}
