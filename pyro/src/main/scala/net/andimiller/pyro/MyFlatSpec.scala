package net.andimiller.pyro

import cats._
import cats.implicits._
import cats.effect._

import scala.language.{higherKinds, implicitConversions}
import scala.reflect.runtime.universe._

class MyFlatSpec[F[_]: Effect] {}

object FlatSpecSyntax {
  implicit class ShouldSyntax(name: String) {
    def should(thing: String): Should = Should(name, thing)
  }
  case class Should(name: String, thing: String) {
    def in[F[_]: Effect, T](f: F[T]) = Test[F, T](name, thing, f)
  }
  case class Test[F[_], T](name: String, thing: String, action: F[T])

}

case class Tests[F[_], T](name: String, tests: List[Test[F, T]])

object Tests {
  implicit def testsFunctor[F[_]: Functor]: Functor[Tests[F, ?]] =
    new Functor[Tests[F, ?]] {
      override def map[A, B](fa: Tests[F, A])(f: A => B): Tests[F, B] =
        fa.copy(tests = fa.tests.map(_.map(f)))
    }
}

case class Test[F[_], T](thing: String, action: F[T])
object Test {
  implicit def testFunctor[F[_]: Functor]: Functor[Test[F, ?]] =
    new Functor[Test[F, ?]] {
      override def map[A, B](fa: Test[F, A])(f: A => B): Test[F, B] =
        fa.copy(action = Functor[F].map(fa.action)(f))
    }
}

trait Banana

abstract class WordSpec[F[_]](implicit val F: Effect[F]) extends Banana {

  implicit class InSyntax(name: String) {
    def should[T](tests: List[(String, F[T])]): Tests[F, T] =
      Tests[F, T](name, tests.map(tupleToTest))
  }
  implicit def tupleToTest[T](pair: (String, F[T])): Test[F, T] =
    Test(pair._1, pair._2)
  implicit def bareActionToTest[T](action: F[T])(
      implicit s: sourcecode.Name): Test[F, T] = Test(s.value, action)
  def tests: Tests[F, Unit]

}

class ExampleWordSpec extends WordSpec[IO] {
  val t = "strings" should List(
    "be lowercaseable" -> IO { "foo".toLowerCase },
    "be uppercaseable" -> IO { "foo".toUpperCase },
  )
  override def tests: Tests[IO, Unit] = t.void

  IO { new Throwable("yes").asLeft[String] }.rethrow
}
