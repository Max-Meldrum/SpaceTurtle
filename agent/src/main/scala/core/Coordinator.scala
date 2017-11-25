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

import com.typesafe.scalalogging.LazyLogging
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.leader.LeaderLatchListener
import org.apache.curator.framework.state.{ConnectionState, ConnectionStateListener}
import utils.{Leader, Role, Worker}
import zookeeper.ZkClient.ZooKeeperClient


class Coordinator(latch: LeaderElection)(implicit zk: ZooKeeperClient) extends LazyLogging {
  private[this] var connectionStateListener = None: Option[ConnectionStateListener]
  private[this] var leaderMonitor = None: Option[LeaderMonitor]

  /** Starts the execution of the Agent Coordinator
    *
    * @param role Leader || Worker
    */
  def start(role: Role): Unit = {
    role match {
      case Leader => {
        connectionStateListener = Some(leaderListener())
        leaderMonitor = Some(new LeaderMonitor())
      }
      case Worker => {
        connectionStateListener = Some(workerListener())
      }
    }
    zk.getConnectionStateListenable.addListener(connectionStateListener.get)

    latch.getLatch().addListener(new LeaderLatchListener {
      override def isLeader: Unit = {
        logger.info("Taking over as leader")
        leaderMonitor match {
          case Some(m) =>
            m.createCache()
          case None =>
            leaderMonitor = Some(new LeaderMonitor())
            leaderMonitor.get.
              createCache()
        }
      }

      override def notLeader(): Unit = {
        logger.info("Not Leader any longer")
        leaderMonitor match {
          case Some(m) => m.closeCache()
          case None => leaderMonitor = Some(new LeaderMonitor())
        }

      }
    })
  }

  private def leaderListener(): ConnectionStateListener = {
    val l = new ConnectionStateListener{
      override def stateChanged(client: CuratorFramework, newState: ConnectionState): Unit  = {
        newState match {
          case ConnectionState.LOST => logger.info("Lost connection to ZooKeeper")
          case ConnectionState.SUSPENDED => logger.info("Connection to ZooKeeper suspended")
          case ConnectionState.CONNECTED => logger.info("Connection to ZooKeeper established")
          case ConnectionState.RECONNECTED => assignRole(getState())
          case ConnectionState.READ_ONLY => logger.info("Connection in read only mode")
        }
      }
    }
    l
  }

  private def workerListener(): ConnectionStateListener = {
    val l = new ConnectionStateListener{
      override def stateChanged(client: CuratorFramework, newState: ConnectionState): Unit  = {
        newState match {
          case ConnectionState.LOST => logger.info("Lost connection to ZooKeeper")
          case ConnectionState.SUSPENDED => logger.info("Connection to ZooKeeper suspended")
          case ConnectionState.CONNECTED => logger.info("Connection to ZooKeeper established")
          case ConnectionState.RECONNECTED => assignRole(getState())
          case ConnectionState.READ_ONLY => logger.info("Connection in read only mode")
        }
      }
    }
    l
  }

  def getState(): Role = {
    latch.isLeader() match {
      case true => Leader
      case false => Worker
    }
  }

  private def assignRole(role: Role): Unit = {
    connectionStateListener match {
      case Some(listener) => zk.getConnectionStateListenable.removeListener(listener)
      case None =>
    }
    connectionStateListener = Some(
      role match{
        case Leader => leaderListener()
        case Worker => workerListener()
      })
  }
}
