package com.swissborg.lithium

import akka.cluster.swissborg.EitherValues
import cats.effect.Sync
import cats.{Applicative, ApplicativeError, Functor, Semigroupal}
import com.swissborg.lithium.instances.ArbitraryTestInstances._
import com.swissborg.lithium.strategy._
import eu.timepit.refined.auto._
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.refineV
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.chooseNum
import org.scalacheck.{Arbitrary, Gen}

trait ArbitraryStrategy[F] {
  def fromScenario(scenario: Scenario): Arbitrary[F]
}

object ArbitraryStrategy extends EitherValues {
  implicit def keepRefereeArbitraryStrategy[F[_]: Applicative]: ArbitraryStrategy[KeepReferee[F]] =
    new ArbitraryStrategy[KeepReferee[F]] {
      override def fromScenario(scenario: Scenario): Arbitrary[KeepReferee[F]] = Arbitrary {
        val maybeNodes = scenario.worldViews.headOption.map(_.nodes)

        for {
          referee <- Gen.oneOf(maybeNodes.fold(Arbitrary.arbitrary[Node]) { nodes =>
            chooseNum(0, nodes.length - 1).map(nodes.toNonEmptyList.toList.apply)
          }, Arbitrary.arbitrary[Node])

          downIfLessThan <- chooseNum(1, maybeNodes.fold(1)(_.length))
        } yield new strategy.KeepReferee[F](
          KeepReferee.Config(refineV[SBAddress](referee.member.address.toString).rightValue,
                             refineV[Positive](downIfLessThan).rightValue)
        )
      }
    }

  implicit def staticQuorumArbitraryStrategy[F[_]: Sync]: ArbitraryStrategy[StaticQuorum[F]] =
    new ArbitraryStrategy[StaticQuorum[F]] {
      override def fromScenario(scenario: Scenario): Arbitrary[StaticQuorum[F]] = Arbitrary {
        val clusterSize = scenario.clusterSize

        val minQuorumSize = clusterSize / 2 + 1
        for {
          quorumSize <- chooseNum(minQuorumSize, clusterSize.value)
          role       <- arbitrary[String]
        } yield new strategy.StaticQuorum(StaticQuorum.Config(role, refineV[Positive](quorumSize).rightValue))
      }
    }

  implicit def keepMajorityArbitraryStrategy[F[_]: ApplicativeError[*[_], Throwable]]
    : ArbitraryStrategy[KeepMajority[F]] =
    new ArbitraryStrategy[KeepMajority[F]] {
      override def fromScenario(scenario: Scenario): Arbitrary[KeepMajority[F]] =
        Arbitrary {
          for {
            role                  <- arbitrary[String]
            weaklUpMembersAllowed <- arbitrary[Boolean]
          } yield new strategy.KeepMajority(KeepMajority.Config(role), weaklUpMembersAllowed)
        }
    }

  implicit def keepOldestArbitraryStrategy[F[_]: ApplicativeError[*[_], Throwable]]: ArbitraryStrategy[KeepOldest[F]] =
    new ArbitraryStrategy[KeepOldest[F]] {
      override def fromScenario(scenario: Scenario): Arbitrary[KeepOldest[F]] = Arbitrary {
        for {
          downIfAlone <- arbitrary[Boolean]
          role        <- arbitrary[String]
        } yield new strategy.KeepOldest(KeepOldest.Config(downIfAlone, role))
      }

    }

  implicit def downAllArbitraryStrategy[F[_]: Applicative]: ArbitraryStrategy[DownAll[F]] =
    new ArbitraryStrategy[DownAll[F]] {
      override def fromScenario(scenario: Scenario): Arbitrary[DownAll[F]] =
        Arbitrary(Gen.const(new strategy.DownAll[F]()))
    }

  implicit def downIndirectlyConnectedArbitraryStrategy[F[_]: Applicative]: ArbitraryStrategy[IndirectlyConnected[F]] =
    new ArbitraryStrategy[IndirectlyConnected[F]] {
      override def fromScenario(scenario: Scenario): Arbitrary[IndirectlyConnected[F]] =
        Arbitrary(Gen.const(new strategy.IndirectlyConnected[F]()))
    }

  implicit def unionArbitraryStrategy[F[_]: Functor: Semigroupal, Strat1[_[_]], Strat2[_[_]]](
    implicit ev1: Strat1[F] <:< Strategy[F],
    ev2: Strat2[F] <:< Strategy[F],
    arbStrat1: ArbitraryStrategy[Strat1[F]],
    arbStrat2: ArbitraryStrategy[Strat2[F]]
  ): ArbitraryStrategy[Union[F, Strat1, Strat2]] =
    new ArbitraryStrategy[Union[F, Strat1, Strat2]] {
      override def fromScenario(scenario: Scenario): Arbitrary[Union[F, Strat1, Strat2]] =
        Arbitrary {
          for {
            strat1 <- arbStrat1.fromScenario(scenario).arbitrary
            strat2 <- arbStrat2.fromScenario(scenario).arbitrary
          } yield new Union[F, Strat1, Strat2](strat1, strat2)
        }
    }
}
