import cats._, cats.implicits._, cats.effect._

class MyFlatSpec[F[_]: Effect] {
  import FlatSpecSyntax._
}

object FlatSpecSyntax {
  implicit class ShouldSyntax(name: String) {
    def should(thing: String): Should = Should(name, thing)
  }
  case class Should(name: String, thing: String) {
    def in[F[_]: Effect, T](f: F[T]) = Test[F, T](name, thing, f)
  }
  case class Test[F[_], T](name: String, thing: String, action: F[T])

}

object WordSpecSyntax {

  implicit class InSyntax(name: String) {
    def should[F[_], T](tests: List[(String, F[T])]) = Tests[F, T](tests.map(tupleToTest))
  }
  case class Tests[F[_], T](tests: List[Test[F, T]])
  implicit def tupleToTest[F[_], T](pair: (String, F[T])) = Test(pair._1, pair._2)
  case class Test[F[_], T](thing: String, action: F[T])


  /// example
  "strings" should List(
    "be lowercaseable" -> IO { "foo".toLowerCase },
    "be uppercaseable" -> IO { "foo".toUpperCase },
  )

}
