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

import se.meldrum.spaceturtle.BaseSpec


class SpaceTurtleConfigSpec extends BaseSpec with SpaceTurtleConfig {

  test("That valid SpaceTurtleConfig exists") {
    assert(spaceTurtleHost.isEmpty == false)
    assert(spaceTurtleUser.isEmpty == false)
    assert(spaceTurtleStoragePath.isEmpty == false)
    assert(spaceTurtlePort > 0 && spaceTurtlePort < 65535)
  }
}
