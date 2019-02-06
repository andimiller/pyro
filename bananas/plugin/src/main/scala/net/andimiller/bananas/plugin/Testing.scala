package net.andimiller.bananas.plugin

import sbt.testing._

class Bananas extends Framework {
  override def name(): String = "bananas"
  override def fingerprints(): Array[Fingerprint] =
    Array(new BananasFingerprint())
  override def runner(args: Array[String],
                      remoteArgs: Array[String],
                      testClassLoader: ClassLoader): Runner = new BananasRunner
}

class BananasRunner extends Runner {
  var state = new StringBuilder()

  override def tasks(taskDefs: Array[TaskDef]): Array[Task] = taskDefs.map {
    t =>
      val w = Class.forName(t.fullyQualifiedName()).newInstance()
      new Task {
        override def tags(): Array[String] = Array()
        override def execute(eventHandler: EventHandler,
                             loggers: Array[Logger]): Array[Task] = {
          w match {
            case w: WordSpec[_] =>
              val F = w.F
              w.tests.tests.foreach { test =>
                val result: Either[Throwable, Unit] =
                  F.toIO(F.attempt(F.void(test.action))).unsafeRunSync()
                val colour =
                  if (result.isRight) fansi.Color.Green else fansi.Color.Red
                state.append(colour(s"${w.tests.name}: ${test.thing}\n"))
                eventHandler.handle(new Event {
                  override def fullyQualifiedName(): String =
                    t.fullyQualifiedName()
                  override def fingerprint(): Fingerprint = t.fingerprint()
                  override def selector(): Selector = new SuiteSelector
                  override def status(): Status =
                    result.toOption
                      .map(_ => Status.Success)
                      .getOrElse(Status.Failure)
                  override def throwable(): OptionalThrowable =
                    result.left.toOption
                      .map(t => new OptionalThrowable(t))
                      .getOrElse(new OptionalThrowable())
                  override def duration(): Long = -1
                })
              }
          }
          Array()
        }
        override def taskDef(): TaskDef = t
      }
  }

  override def done(): String = state.toString()
  override def remoteArgs(): Array[String] = Array()
  override def args(): Array[String] = Array()
}

class BananasFingerprint extends SubclassFingerprint {
  override def isModule: Boolean = false
  override def superclassName(): String = "net.andimiller.pyro.Banana"
  override def requireNoArgConstructor(): Boolean = false
}
