
import com.rabbitmq.client._

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
      scalaz.concurrent.Task(publish(channel, qName, message))
    }

  }
  case class Publisher(channel: com.rabbitmq.client.Channel, qName: String)
  {
    def publishProcess(str : String) : Process[Task, Unit] = {
      Process.eval(Publisher.publishTask(channel, qName, str))
    }
  }

  object Subscriber {
    def subscriber(chan: com.rabbitmq.client.Channel, qName: String, tag:String = "myConsumerTag") : Task[String] = {
       val subscribe : Task[String] =
         Task async { cb =>
          chan.basicConsume(qName, true, tag,
            new DefaultConsumer(chan) {
              override def handleDelivery(consumerTag : String,
                                           envelope: Envelope,
                                           properties : AMQP.BasicProperties,
                                           body : Array[Byte]): Unit = {
                val routingKey = envelope.getRoutingKey
                val contentType = properties.getContentType
                val deliveryTag = envelope.getDeliveryTag
                cb(\/-(new String(body)))
              }
            }
          )}
      subscribe
    }
  }

  case class Subscriber(chan: com.rabbitmq.client.Channel, qName: String) {
    val consumer = Subscriber.subscriber(chan, qName)
    val subscribeZAll: Process[Task, String] = Process.eval(consumer)
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
      val data = output.runAsync(x => x match {
        case \/-(right) => right.foreach(println _)
        case -\/(left) => println("Hit an error")
      })

      Publisher.publish(chan, qName, "HELLO")
      Publisher.publish(chan, qName, "HELLO AGAIN")
      Thread.sleep(5000)
    }
  }
}
object Runner extends App {
  override def main(args : Array[String]) =
    RabbitMQ.PublisherTest.testing()
}