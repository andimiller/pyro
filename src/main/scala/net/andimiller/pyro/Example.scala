package net.andimiller.pyro

import net.andimiller.pyro.core._, net.andimiller.pyro.dsl.matcher.Matcher._
import cats._, cats.implicits._

object Example {
  def above(i: Int) = Predicate[Int](_ > i)
  def below(i: Int) = Predicate[Int](_ < i)

  7 mustBe(above(5) and below(10))
  7 mustBe above(5)
  7 mustBe (above(5), below(10))

  7 mustBe equalTo(4)

}
