package com.swissborg.sbr.strategies.keepreferee

import akka.actor.Address
import akka.cluster.ClusterEvent.CurrentClusterState
import akka.cluster.MemberStatus.Up
import akka.cluster.swissborg.TestMember
import cats.Id
import com.swissborg.sbr.strategies.keepreferee.KeepReferee.Config
import com.swissborg.sbr.strategies.keepreferee.KeepReferee.Config.{Address => RefereeAddress}
import com.swissborg.sbr.{DownReachable, DownUnreachable, WorldView}
import eu.timepit.refined._
import eu.timepit.refined.auto._
import org.scalatest.{Matchers, WordSpec}

import scala.collection.immutable.SortedSet

class KeepRefereeSuite extends WordSpec with Matchers {
  private val aa = TestMember(Address("akka.tcp", "sys", "a", 2552), Up)
  private val bb = TestMember(Address("akka.tcp", "sys", "b", 2552), Up)
  private val cc = TestMember(Address("akka.tcp", "sys", "c", 2552), Up)

  private val referee = refineV[RefereeAddress](aa.address.toString).left.map(new IllegalArgumentException(_)).toTry.get

  "KeepReferee" must {
    "down the unreachable nodes when being the referee node and reaching enough nodes" in {
      val w = WorldView.fromSnapshot(
        aa,
        CurrentClusterState(SortedSet(aa, bb, cc), Set(bb), seenBy = Set.empty)
      )

      KeepReferee[Id](Config(referee, 1)).takeDecision(w) should ===(DownUnreachable(w))
    }

    "down the reachable nodes when being the referee and not reaching enough nodes" in {
      val w = WorldView.fromSnapshot(
        aa,
        CurrentClusterState(SortedSet(aa, bb, cc), Set(bb), seenBy = Set.empty)
      )

      KeepReferee[Id](Config(referee, 3)).takeDecision(w) should ===(DownReachable(w))
    }

    "down the unreachable nodes when the referee is reachable and reaching enough nodes" in {
      val w = WorldView.fromSnapshot(
        cc,
        CurrentClusterState(SortedSet(aa, bb, cc), Set(bb), seenBy = Set.empty)
      )

      KeepReferee[Id](Config(referee, 1)).takeDecision(w) should ===(DownUnreachable(w))
    }

    "down the reachable nodes when the referee is reachable and not reaching enough nodes" in {
      val w = WorldView.fromSnapshot(
        cc,
        CurrentClusterState(SortedSet(aa, bb, cc), Set(bb), seenBy = Set.empty)
      )

      KeepReferee[Id](Config(referee, 3)).takeDecision(w) should ===(DownReachable(w))
    }

    "down the reachable nodes when the referee is unreachable" in {
      val w = WorldView.fromSnapshot(
        bb,
        CurrentClusterState(SortedSet(aa, bb, cc), Set(aa), seenBy = Set.empty)
      )

      KeepReferee[Id](Config(referee, 1)).takeDecision(w) should ===(DownReachable(w))
      KeepReferee[Id](Config(referee, 3)).takeDecision(w) should ===(DownReachable(w))
    }

    "compile for valid addresses" in {
      """refineMV[RefereeAddress]("protocol://system@address:1234")""" should compile
      """refineMV[RefereeAddress]("a.b.c://system@address:1234")""" should compile
      """refineMV[RefereeAddress]("a.b.c://system@127.0.0.1:1234")""" should compile
      """refineMV[RefereeAddress]("a.b.c://system@d.e.f:1234")""" should compile
    }
  }
}
