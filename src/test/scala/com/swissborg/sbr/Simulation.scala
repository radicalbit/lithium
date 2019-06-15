package com.swissborg.sbr

import cats.implicits._
import cats.{Functor, Monoid}
import com.swissborg.sbr.scenarios.Scenario
import com.swissborg.sbr.strategy.Strategy
import com.swissborg.sbr.utils.PostResolution
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary

/**
  * A simulation of a split-brain resolution.
  *
  * @param strategy the strategy to be used to resolve the scenario.
  * @param scenario the split-brain scenario.
  */
class Simulation[F[_]: Functor, Strat[_[_]], S <: Scenario](val strategy: Strat[F], val scenario: S)(
    implicit ev: Strat[F] <:< Strategy[F],
    M: Monoid[F[PostResolution]]
) {

  /**
    * True if after applying the strategy's decisions no independent
    * cluster are created.
    */
  val splitBrainResolved: F[Boolean] = {
    scenario.worldViews
      .foldMap { worldView =>
        strategy.takeDecision(worldView).map(PostResolution.fromDecision(worldView))
      }
      .map(_.noSplitBrain)
  }
}
object Simulation {
  implicit def arbSimulation[F[_]: Functor, Strat[_[_]], S <: Scenario: Arbitrary](
      implicit strategy: ArbitraryStrategy[Strat[F]],
      M: Monoid[F[PostResolution]],
      ev: Strat[F] <:< Strategy[F]
  ): Arbitrary[Simulation[F, Strat, S]] =
    Arbitrary {
      for {
        scenario <- arbitrary[S]
        strategy <- strategy.fromScenario(scenario).arbitrary
      } yield new Simulation[F, Strat, S](strategy, scenario)
    }
}
