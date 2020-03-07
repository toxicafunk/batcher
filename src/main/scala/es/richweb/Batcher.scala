package es.richweb

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.concurrent.Future

import scala.collection.JavaConverters._
import scala.util.control.NonFatal

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.{util => ju}
import java.util.concurrent.TimeUnit

object Batcher {

  val queue: LinkedBlockingQueue[String] = new LinkedBlockingQueue(100)
  val batchSize = 5
  val maxTimeout = 6000

  implicit val ec = ExecutionContext.global

  val process = (msg: String) => {
    Thread.sleep(10000)
    println(s"Processing $msg")
  }

  val send = (msg: String) => process(msg)

  val sendAsync = (msg: String) => Future {
    queue.offer(msg)
    println("Message enqueued...")
    ()
  }

  val daemonExecutor: ExecutorService =
    Executors.newSingleThreadExecutor(new ThreadFactory {
      override def newThread(r: Runnable): Thread = {
        val t = Executors.defaultThreadFactory.newThread(r)
        t.setDaemon(true)
        t
      }
    })

  val task = new Runnable {
      val messages =  new ju.ArrayList[String]()
      def tick(): Unit = try {
        messages.clear()
        // Start with `take()` since it'll block on an empty queue. If this
        // succeeds, then drain any remaining messages into our messages list.
        val head = queue.take()
        if (head != null) {
          messages.add(head)
          // Drain remaining messages into the list.
          queue.drainTo(messages, batchSize)
          println(messages)
          messages.subList(0, batchSize).iterator.asScala.foreach(process)
        } else {
          ()
        }
      } catch {
        case _: InterruptedException => Thread.currentThread.interrupt
        case NonFatal(exception) => {
          println(s"Swallowing exception thrown while sending metric: $exception")
        }
      }

      def run(): Unit = {
        while (!Thread.interrupted) {
          println("TICK!")
          tick
        }
      }
  }

  daemonExecutor.submit(task)

  def main(args: Array[String]): Unit ={
    val msgs = Array("one", "two", "three", "four", "five", "six", "seven", "eight", "nine")

    println("Sending...")
    //msgs.foreach(send)
    println("Sending Async...")
    msgs.foreach(sendAsync)

    Thread.sleep(10000)
  }

}
