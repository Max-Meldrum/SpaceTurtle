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

package core


import java.util.concurrent.TimeUnit

import org.apache.curator.framework.recipes.leader.LeaderLatch
import zookeeper.{Agent, ZkPaths}
import zookeeper.ZkClient.ZooKeeperClient

import collection.JavaConverters._


class LeaderElection(agent: Agent)(implicit zk: ZooKeeperClient) extends ZkPaths {
  private[this] val leaderLatch = new LeaderLatch(zk, electionPath, agent.host)


  def startLatch(): Unit = {
    leaderLatch.start
    leaderLatch.await(1, TimeUnit.SECONDS)
  }
  def closeLatch(): Unit = leaderLatch.close
  def isLeader(): Boolean = leaderLatch.hasLeadership
  def getLatch(): LeaderLatch = leaderLatch

  def agentExists(): Boolean = leaderLatch.getParticipants
    .asScala
    .exists(_.getId == agent.host)
}
