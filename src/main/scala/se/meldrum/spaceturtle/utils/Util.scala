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

package se.meldrum.spaceturtle.utils

import java.io.{BufferedReader, InputStreamReader}
import java.net.URL
import scala.util.{Failure, Success, Try}

object Util {

  /** Fetch External IP
    *
    * @return Option[String] with either external IP or nothing
    */
  def getExternalIP(): Option[String] = {
    val conn = Try {
      val url = new URL("http://checkip.amazonaws.com")
      val in = new BufferedReader(new InputStreamReader(url.openStream()))
      // get IP
      in.readLine()
    }
    conn match {
      case Success(ip) => Some(ip)
      case Failure(_) => None
    }
  }
}
