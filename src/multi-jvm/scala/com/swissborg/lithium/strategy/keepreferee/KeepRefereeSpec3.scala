package com.swissborg.lithium

package strategy

package keepreferee

import akka.remote.transport.ThrottlerTransportAdapter.Direction

import scala.concurrent.duration._

class KeepRefereeSpec3MultiJvmNode1 extends KeepRefereeSpec3
class KeepRefereeSpec3MultiJvmNode2 extends KeepRefereeSpec3
class KeepRefereeSpec3MultiJvmNode3 extends KeepRefereeSpec3

/**
 * Node1 and node2 are indirectly connected in a three node cluster
 *
 * Node1 should down itself as its indirectly connected even if it is the referee.
 * Node2 should down itself as its indirectly connected.
 * Node3 should down itself as its not the referee.
 */
sealed abstract class KeepRefereeSpec3 extends ThreeNodeSpec("KeepReferee", KeepRefereeSpecThreeNodeConfig) {
  override def assertions(): Unit =
    "handle scenario 3" in within(60 seconds) {
      runOn(node1) {
        testConductor.blackhole(node1, node2, Direction.Both).await
      }

      enterBarrier("links-failed")

      runOn(node1, node2, node3) {
        waitForSelfDowning
      }

      enterBarrier("split-brain-resolved")
    }
}
