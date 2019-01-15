package net.andimiller.pyro

import cats._
import cats.data._
import cats.implicits._
import shapeless._
import shapeless.ops.hlist.{LeftFolder, MapFolder, Prepend}
import shapeless.poly._

object core {

  type Predicate[A] = A => Either[NonEmptyList[String], A]
  case class Asserts[A](a: A, as: List[Predicate[A]]) {
    def assert(s: String)(f: A => Boolean): Asserts[A] = copy(as = { (a: A) => if (f(a)) Right(a) else Left(NonEmptyList.of(s)) } :: as)
    def compile: Compiled[A] = Compiled(this)
    def and[B](b: Asserts[B]) = this :: b :: HNil
  }
  case class Compiled[A](as: Asserts[A]) {
    def run: Either[NonEmptyList[String], A] = as.as.map(_.apply(as.a)).sequence.map(_ => as.a)
    def accumulatedRun: Either[NonEmptyList[String], A] = as.as.map(_.apply(as.a)).parSequence.map(_ => as.a)
  }

  object Evaluator extends Poly1 {
    implicit def asserts[T] = at[Asserts[T]](_.compile.run.map(_ => ()))
  }

  object AccumulatedEvaluator extends Poly1 {
    implicit def asserts[T] = at[Asserts[T]](_.compile.accumulatedRun.map(_ => ()))
  }

  object syntax {
    implicit class InitialAssertChainSyntax[A](a: A) {
      def assert(s: String)(f: A => Boolean): Asserts[A] = Asserts(a, List((a: A) => if (f(a)) Right(a) else Left(NonEmptyList.of(s))))
    }
    implicit class AssertsChain[H <: HList](hs: H) {
      def and[B](b: Asserts[B])(implicit c: UnaryTCConstraint[H, Asserts], p: Prepend[H, Asserts[B] :: HNil]) = hs :+ b
      def run(implicit c: UnaryTCConstraint[H, Asserts], mf: MapFolder[H, Either[NonEmptyList[String], Unit], Evaluator.type]): Either[NonEmptyList[String], Unit] =
        hs.foldMap(().asRight[NonEmptyList[String]])(Evaluator)((a, b) => List(a, b).sequence.map(_ => ()))
      def accumulatedRun(implicit c: UnaryTCConstraint[H, Asserts], mf: MapFolder[H, Either[NonEmptyList[String], Unit], AccumulatedEvaluator.type]): Either[NonEmptyList[String], Unit] =
        hs.foldMap(().asRight[NonEmptyList[String]])(AccumulatedEvaluator)((a, b) => List(a, b).parSequence.map(_ => ()))
    }
  }

  object Predicate {
    def fromOptionString[A](f: A => Option[String]): Predicate[A] = (a: A) => f(a).map(r => Left(NonEmptyList.of(r))).getOrElse(Right(a))
  }

}
