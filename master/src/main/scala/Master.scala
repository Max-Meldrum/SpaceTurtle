/*
 * Copyright 2017 Max Meldrum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package master

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging
import http.RestService
import utils.HttpConfig

object Master extends App with HttpConfig with LazyLogging {
  implicit val system = ActorSystem("Master")
  implicit val materializer = ActorMaterializer()
  val service = new RestService()
  logger.info("Setting up SpaceTurtle Master on " + interface + ":" + port)
  Http().bindAndHandle(service.route, interface , port)
}