package com.swissborg.lithium

package strategy

package keepreferee

import akka.remote.transport.ThrottlerTransportAdapter.Direction

import scala.concurrent.duration._

class KeepRefereeSpec5MultiJvmNode1 extends KeepRefereeSpec5
class KeepRefereeSpec5MultiJvmNode2 extends KeepRefereeSpec5
class KeepRefereeSpec5MultiJvmNode3 extends KeepRefereeSpec5
class KeepRefereeSpec5MultiJvmNode4 extends KeepRefereeSpec5
class KeepRefereeSpec5MultiJvmNode5 extends KeepRefereeSpec5

/**
 * Node4 and node5 are indirectly connected in a five node cluster
 *
 * Node4 and node5 should down themselves as they are indirectly connected.
 * The three other nodes survive as they contain the referee.
 */
sealed abstract class KeepRefereeSpec5 extends FiveNodeSpec("KeepReferee", KeepRefereeSpecFiveNodeConfig) {
  override def assertions(): Unit =
    "handle scenario 5" in within(60 seconds) {
      runOn(node1) {
        testConductor.blackhole(node4, node5, Direction.Both).await
      }

      enterBarrier("links-failed")

      runOn(node1, node2, node3) {
        waitForSurvivors(node1, node2, node3)
        waitForAllLeaving(node4, node5)
      }

      runOn(node4, node5) {
        waitForSelfDowning
      }

      enterBarrier("split-brain-resolved")
    }
}
