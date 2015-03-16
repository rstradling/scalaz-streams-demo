import com.rabbitmq.client.{ConnectionFactory, QueueingConsumer}

import scalaz.{\/, -\/, \/-}
import scalaz.concurrent._
import scalaz.stream.Process
import scalaz.stream.Process.Halt

/**
 * Created by ryanstradling on 3/15/15.
 */
object RabbitMQ {

  object Publisher {
    def publish(channel: com.rabbitmq.client.Channel, qName: String, message: String): Unit = {
        channel.basicPublish("", qName, null, message.getBytes())
    }
    def publishTask(channel: com.rabbitmq.client.Channel, qName: String, message: String): Task[Unit] = {
      scalaz.concurrent.Task.delay(publish(channel, qName, message))
    }

  }
  case class Publisher(channel: com.rabbitmq.client.Channel, qName: String)
  {
    def publishProcess(str : String) : Process[Task, Unit] = {
      Process.eval(Publisher.publishTask(channel, qName, str))
    }
  }

  object Subscriber {
    def initialize(chan: com.rabbitmq.client.Channel, qName: String) : QueueingConsumer = {
      val consumer = new QueueingConsumer(chan)
      chan.basicConsume(qName, true, consumer)
      consumer
    }
    def subscribe(consumer : QueueingConsumer): String = {
      val delivery = consumer.nextDelivery()
      val message = new String(delivery.getBody())
      System.out.println(s"message = $message")
      message
    }
  }

  case class Subscriber(chan: com.rabbitmq.client.Channel, qName: String) {
    val consumer = Subscriber.initialize(chan, qName)
    val subscribeZ1: Process[Task, String] = Process.eval(Task.fork(Task.delay(Subscriber.subscribe(consumer))))
    val subscribeZAll = subscribeZ1.repeat
  }

  object Queue {
    def apply(channel: com.rabbitmq.client.Channel)(qName: String): Unit = {
      channel.queueDeclare(qName, false, false, false, null)

    }
  }

  object Connection {
    def apply(host: String, port: Integer): com.rabbitmq.client.Connection = {
      val factory = new ConnectionFactory()
      factory.setHost(host)
      factory.setPort(port)
      factory.newConnection()
    }

    def close(conn: com.rabbitmq.client.Connection): Unit = {
      conn.close()
    }
  }

  object Channel {
    def apply(connection: com.rabbitmq.client.Connection): com.rabbitmq.client.Channel = {
      connection.createChannel()
    }

    def close(channel: com.rabbitmq.client.Channel): Unit = {
      channel.close()
    }
  }


  object PublisherTest {
    def testing(): Unit = {
      val qName = "TESTING"
      val con = Connection("localhost", 5672)
      val chan = Channel(con)
      Queue(chan)(qName)

      val sub = Subscriber(chan, qName)
      val output = sub.subscribeZAll.runLog
      val data = output.runAsyncInterruptibly(_ match {
        case \/-(right) => System.out.println(right)
        case -\/(left) => System.out.println(left)
      })
      System.out.println(output)

      Publisher.publish(chan, qName, "HELLO")
      Publisher.publish(chan, qName, "HELLO AGAIN")
      System.out.println("HERE")
      Thread.sleep(5000)
      data()
    }
  }
}
object Runner extends App {
  override def main(args : Array[String]) =
    RabbitMQ.PublisherTest.testing()
}