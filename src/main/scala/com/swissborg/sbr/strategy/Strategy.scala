package com.swissborg.sbr
package strategy

/**
  * Interface for split-brain resolution strategies.
  */
private[sbr] trait Strategy[F[_]] {

  /**
    * The strategy decision given the world view.
    *
    * @param worldView the world view of the cluster from the callers actor sytem.
    */
  def takeDecision(worldView: WorldView): F[Decision]
}
