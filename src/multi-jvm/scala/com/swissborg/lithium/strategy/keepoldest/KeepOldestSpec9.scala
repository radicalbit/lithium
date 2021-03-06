package com.swissborg.lithium

package strategy

package keepoldest

import akka.remote.transport.ThrottlerTransportAdapter.Direction

import scala.concurrent.duration._

class KeepOldestSpec9MultiJvmNode1  extends KeepOldestSpec9
class KeepOldestSpec9MultiJvmNode2  extends KeepOldestSpec9
class KeepOldestSpec9MultiJvmNode3  extends KeepOldestSpec9
class KeepOldestSpec9MultiJvmNode4  extends KeepOldestSpec9
class KeepOldestSpec9MultiJvmNode5  extends KeepOldestSpec9
class KeepOldestSpec9MultiJvmNode6  extends KeepOldestSpec9
class KeepOldestSpec9MultiJvmNode7  extends KeepOldestSpec9
class KeepOldestSpec9MultiJvmNode8  extends KeepOldestSpec9
class KeepOldestSpec9MultiJvmNode9  extends KeepOldestSpec9
class KeepOldestSpec9MultiJvmNode10 extends KeepOldestSpec9

/**
 * Node2 and node3 are indirectly connected in a ten node cluster
 * Node9 and node10 are indirectly connected in a ten node cluster
 */
sealed abstract class KeepOldestSpec9 extends TenNodeSpec("KeepOldest", KeepOldestSpecTenNodeConfig) {
  override def assertions(): Unit =
    "handle scenario 11" in within(120 seconds) {
      runOn(node1) {
        testConductor.blackhole(node8, node9, Direction.Both).await
        testConductor.blackhole(node9, node10, Direction.Both).await
      }

      enterBarrier("links-failed")

      runOn(node1, node2, node3, node4, node5, node6, node7) {
        waitForSurvivors(node1, node4, node5, node6, node7)
        waitExistsAllDownOrGone(
          Seq(Seq(node8, node10), Seq(node9))
        )
      }

      enterBarrier("split-brain-resolved")
    }
}
