package es.richweb

import zio.{Fiber, Queue, RIO, Runtime, Schedule, Task, UIO, URIO, ZIO}
import zio.clock.Clock
import zio.console._
import zio.duration.DurationSyntax
import zio.stream.{Sink, ZStream}

object ZBatcher {

  private val rt = Runtime.unsafeFromLayer(Console.live ++ Clock.live)

  val batchSize = 5
  val maxTimeout = 5000

  private val queue: UIO[Queue[String]] = Queue.bounded[String](100)
  private val everyNms = Schedule.spaced(new DurationSyntax(maxTimeout).millis)
  private val sink = Sink.collectAllN[String](batchSize)

  // ZIO[-R, E, +A] == R => Either[E, A]
  // Function[-I, +O] == I => O

  val process: List[String] => List[Unit] = (msgs: List[String]) => {
    Thread.sleep(1000)
    msgs.map(msg => println(s"Processing $msg"))
  }

  def send(msg: String)(implicit q: Queue[String]): ZIO[Console, Nothing, Unit] =
      for {
        _ <- putStrLn(s"Sending... $q")
        _ <- q.offer(msg)
      } yield ()

  def sendAsync(msg: String)(implicit q: Queue[String]): ZIO[Any, Nothing, Unit] =
      for {
        _ <- q.offer(msg).fork
      } yield ()

  def listen(implicit q: Queue[String]): URIO[Console with Clock, Fiber[Throwable, Unit]] =
    ZStream
      .fromQueue(q)
      .aggregateAsyncWithin(sink, everyNms)
      .tap(l => putStrLn(s"Batched: $l"))
      .mapM(l => Task(process(l)))
      .runDrain
      .fork

  val program: Array[String] => ZIO[Console with Clock, Throwable, Int] = (msgs: Array[String]) =>
    queue >>= (
        implicit q =>
          for {
            _ <- listen
            _ <- RIO.traverse(msgs)(send)
            //_ <- RIO.traverse(msgs)(sendAsync)
          } yield 10000
      )

  def main(args: Array[String]): Unit = {
    val msgs = Array(
      "one",
      "two",
      "three",
      "four",
      "five",
      "six",
      "seven",
      "eight",
      "nine",
      "ten",
      "eleven",
      "twelve"
    )
    rt.unsafeRun(program(msgs))
    Thread.sleep(12000)
  }

}
