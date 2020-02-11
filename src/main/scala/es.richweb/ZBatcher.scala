package es.richweb

import zio.{Queue, RIO, Runtime, Schedule, Task, UIO}
import zio.clock.Clock
import zio.console._
import zio.internal.PlatformLive
import zio.duration.DurationSyntax
import zio.stream.{Sink, ZStream}

object ZBatcher {

  val rt = Runtime(
    new Console.Live with Clock.Live,
    PlatformLive.Default
  )
  val batchSize = 5
  val maxTimeout = 6000
  private val queue: UIO[Queue[String]] = Queue.bounded[String](100)
  val everyNms = Schedule.spaced(new DurationSyntax(maxTimeout).millis)
  val sink = Sink.collectAllN[String](batchSize)

  // ZIO[-R, E, +A] == R => Either[E, A]
  // Function[-I, +O] == I => O

  val process = (msgs: List[String]) => {
    Thread.sleep(1000)
    msgs.map(msg => println(s"Processing $msg"))
  }

  def send(msg: String)(implicit q: Queue[String]) =
      for {
        _ <- q.offer(msg)
      } yield ()

  def sendAsync(msg: String)(implicit q: Queue[String]) =
      for {
        _ <- q.offer(msg).fork
      } yield ()

  def listen(implicit q: Queue[String]) =
    ZStream
      .fromQueue(q)
      .aggregateAsyncWithin(sink, everyNms)
      .tap(l => putStrLn(s"Batched: $l"))
      .mapM(l => Task(process(l)))
      .runDrain
      .fork

  val program = (msgs: Array[String]) =>
    queue >>= (
        implicit q =>
          for {
            _ <- listen
            //_ <- RIO.traverse(msgs)(send)
            _ <- RIO.traverse(msgs)(sendAsync)
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
