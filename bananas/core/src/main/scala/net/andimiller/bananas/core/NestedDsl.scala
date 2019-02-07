package net.andimiller.bananas.core

import cats._, cats.implicits._, cats.data._, cats.effect._
import fs2._

object NestedDsl {

  implicit class NestedGrammar(s: String) {
    // nested constructors
    def should    = PartialGrammarNode(s, "should")
    def must      = PartialGrammarNode(s, "must")
    def shouldNot = PartialGrammarNode(s, "should not")
    def mustNot   = PartialGrammarNode(s, "must not")
    def is        = PartialGrammarNode(s, "is")
    def isNot     = PartialGrammarNode(s, "is not")
    def are       = PartialGrammarNode(s, "are")
    def areNot    = PartialGrammarNode(s, "are not")

//    def in[F[_]](test: F[Assertions]) = Test(NonEmptyChain(s), test)
    def in[F[_]: Functor, T](test: F[ValidatedNel[String, T]]) = Test(NonEmptyChain(s), test.map(_.void))
  }

  case class PartialGrammarNode(s: String, v: String) {
    private def grammar[F[_]](t: Test[F]): Test[F] =
      t.copy(
        labels = NonEmptyChain
          .fromChainPrepend(s, t.labels.tail.prepend(v + " " + t.labels.head)))
    def apply[F[_]](t: Tests[F]): Tests[F]  = t.map(grammar)
    def apply[F[_]](ts: Test[F]*): Tests[F] = apply(Stream.emits(ts))
    def apply[F[_]](t: Test[F]): Test[F]    = grammar(t)
  }

  val cool = "be cool" in IO { ().validNel[String] }
  "dogs" should("be cool" in IO { ().validNel[String] })

  "dogs" should Stream.emits(List(
    "be pet" in IO { ().validNel[String] },
    "test" in IO { ().validNel[String] }
  ))

  "maths" should Stream.range(1, 1000).map { i =>
    s"double $i" in IO { (i * 2).validNel[String].ensure(NonEmptyList.of("is bigger than i"))(_ > i) }
  }

}
