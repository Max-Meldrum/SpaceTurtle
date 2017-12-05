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

package agent

import api.AgentService
import fs2.{Stream, Task}
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.util.StreamApp


/** Main Starting Point of Program
  * Sets upp the REST server
  */
object AgentSystem extends StreamApp {
  override def stream(args: List[String]): Stream[Task, Nothing] = {
    BlazeBuilder
      .bindHttp(8080, "localhost")
      .mountService(AgentService.helloWorldService)
      .serve
  }
}
