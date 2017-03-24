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

package se.meldrum.spaceturtle

import com.typesafe.scalalogging.LazyLogging
import se.meldrum.spaceturtle.network.server.{SpaceTurtleServer, zkConnection}

import scala.util.{Failure, Success}

/** Main Starting Point of Program
  *
  * Starts the SpaceTurtle server on port 8080 by default,
  * or by the port sent in by command line.
  */
object Main extends App with LazyLogging {
  var port = None: Option[Int]

  if (args.length > 0) {
    port = Some(args(0).toInt)
  }

  zkConnection.connect() match {
    case Success(_) => SpaceTurtleServer.run(port.getOrElse(8080))
    case Failure(e) => logger.error(e.toString)
  }
}
