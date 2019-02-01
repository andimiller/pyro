import org.scalatest.words.ResultOfStringPassedToVerb
import org.scalatest.{FlatSpec, MustMatchers, WordSpec}
import cats._
import cats.implicits._
import cats.effect._
import org.scalactic.source.Position

class ExampleTest extends FlatSpec with MustMatchers with IOHarness {
  "foo" must "do thing" during {
    IO { 4 + 2}
  }
}

trait IOHarness { this: FlatSpec =>
  implicit class IOOps(r: ResultOfStringPassedToVerb) {
    def during(f: IO[Any])(implicit pos: Position): Unit = r.in(f.unsafeRunSync())(pos)
  }
}

trait IOHarness2 { this: WordSpec =>
  implicit class IOOps(r: ResultOfStringPassedToVerb) {
    "baz" in {

    }
  }

}
