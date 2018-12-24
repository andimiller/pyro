package net.andimiller.pyro.dsl.matcher

import cats._, cats.implicits._
import net.andimiller.pyro.core._

object Matcher {
  implicit class MustSyntax[A](a: A) {
    def mustBe(p: Predicate[A]): Boolean = p(a)
    def mustBe[F[_] : Foldable, P[_]](f: F[Predicate[A]])(implicit m: Monoid[Predicate[A]]): Boolean = f.combineAll.apply(a)
  }

  def equalTo[A: Eq](a: A) = Predicate.isEqualTo(a)
}
