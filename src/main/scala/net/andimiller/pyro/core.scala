package net.andimiller.pyro

import cats._, cats.implicits._

package object core {

  case class Differ[A](run: (A, A) => Option[String]) {
    def apply(a: (A, A)): Option[String] = run.tupled(a)
    def toPredicate(a: A) = Predicate[A]((o: A) => run(o, a).isEmpty)
  }

  object Differ {
    def apply[A](f: (A, A) => Option[String]) = new Differ(f)
  }

  case class Predicate[A](run: A => Boolean) {
    def apply(a: A) = run(a)
  }

  case class OrPredicate[A](run: Predicate[A]) {
    def apply(a: A) = run.run(a)
  }

  object OrPredicate {
    def apply[A](f: A => Boolean): OrPredicate[A] = OrPredicate(f)

    implicit def monoidForOrPredicate[A]: Monoid[OrPredicate[A]] = new OrPredicateMonoid[A]

    class OrPredicateMonoid[A] extends Monoid[OrPredicate[A]] {
      override def empty: OrPredicate[A] = OrPredicate[A]((_: A) => false)

      override def combine(x: OrPredicate[A], y: OrPredicate[A]): OrPredicate[A] = OrPredicate(x.run or y.run)
    }

    implicit val OrPredicateContravariantMonoidal: ContravariantMonoidal[OrPredicate] = new ContravariantMonoidal[OrPredicate] {
      override def product[A, B](fa: OrPredicate[A], fb: OrPredicate[B]): OrPredicate[(A, B)] = OrPredicate[(A, B)] { ab: (A, B) =>
        fa(ab._1) || fb(ab._2)
      }

      override def contramap[A, B](fa: OrPredicate[A])(f: B => A): OrPredicate[B] = OrPredicate[B]((b: B) => fa.run(f(b)))

      override def unit: OrPredicate[Unit] = OrPredicate[Unit]((_: Unit) => false)
    }
  }

  object Predicate {
    def apply[A](f: A => Boolean): Predicate[A] = Predicate(f)

    def isEqualTo[A](a: A)(implicit e: Eq[A]): Predicate[A] = Predicate[A] { o => e.eqv(a, o) }

    implicit def monoidForPredicate[A]: Monoid[Predicate[A]] = new PredicateMonoid[A]

    class PredicateMonoid[A] extends Monoid[Predicate[A]] {
      override def empty: Predicate[A] = Predicate[A](_ => true)

      override def combine(x: Predicate[A], y: Predicate[A]): Predicate[A] = x and y
    }

    implicit val PredicateContravariantMonoidal: ContravariantMonoidal[Predicate] = new ContravariantMonoidal[Predicate] {
      override def product[A, B](fa: Predicate[A], fb: Predicate[B]): Predicate[(A, B)] = Predicate[(A, B)] { ab: (A, B) =>
        fa(ab._1) && fb(ab._2)
      }

      override def contramap[A, B](fa: Predicate[A])(f: B => A): Predicate[B] = Predicate[B](b => fa(f(b)))

      override def unit: Predicate[Unit] = Predicate[Unit](_ => true)
    }

    implicit class PredicateOps[A](p: Predicate[A]) {
      def negate: Predicate[A] = Predicate(p.run.andThen(!_))

      def and(o: Predicate[A]): Predicate[A] = p.product(o).contramap((a: A) => (a, a))

      def or(o: Predicate[A]): Predicate[A] = OrPredicate(p).product(OrPredicate(o)).contramap((a: A) => (a, a)).run
    }

  }

}
