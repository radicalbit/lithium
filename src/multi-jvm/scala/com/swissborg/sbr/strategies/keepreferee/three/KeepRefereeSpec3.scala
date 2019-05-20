package com.swissborg.sbr.strategies.keepreferee.three

import akka.remote.transport.ThrottlerTransportAdapter.Direction
import com.swissborg.sbr.ThreeNodeSpec
import com.swissborg.sbr.strategies.keepreferee.KeepRefereeSpecThreeNodeConfig

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
class KeepRefereeSpec3 extends ThreeNodeSpec("KeepReferee", KeepRefereeSpecThreeNodeConfig) {
  override def assertions(): Unit =
    "handle indirectly connected members" in within(60 seconds) {
      runOn(node1) {
        testConductor.blackhole(node1, node2, Direction.Receive).await
      }

      enterBarrier("node3-disconnected")

      runOn(node1, node2) {
        waitForUp(node1, node2)
      }

      enterBarrier("node3-unreachable")

      enterBarrier("node1-3-up")

      runOn(node1) {
        waitToBecomeUnreachable(node2)
      }

      enterBarrier("node2-unreachable")

      runOn(node2) {
        waitToBecomeUnreachable(node1)
      }

      enterBarrier("node1-unreachable")

      runOn(node3) {
        waitToBecomeUnreachable(node1, node2)
      }

      enterBarrier("node2-3-unreachable")

      runOn(node1, node2, node3) {
        waitForSelfDowning
      }

      enterBarrier("node1-2-3-suicide")
    }
}