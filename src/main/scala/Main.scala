import RabbitMQ.Subscriber
import argonaut.Json
import com.rabbitmq.client.AMQP.Connection
import com.rabbitmq.client.Channel

import scalaz.concurrent.Task
import scalaz.{-\/, \/-, \/}

/**
 * Created by ryanstradling on 3/15/15.
 */

object Main extends App {
  lazy val server = "localhost"
  lazy val port = 5672
  lazy val qName = "MYQUEUE"

  def createContacts() : List[Person] = {
    List(
       Person("Joe", "Blow", "email@.com", None, None),
         Person("Sarah", "Noon", "email@.com", None, None),
         Person("Heather", "Runner", "email@.com", None, None),
         Person("John", "Friend", "email@.com", None, None),
         Person("Sam", "Walback",  "email@.com", None, None),
         Person("Mike", "Plane",  "email@.com", None, None),
         Person("Super", "Friend", "email@.com", None, None),
         Person("Lois", "Lane", "email@.com", None, None))
  }
  import scalaz.stream.Process
  def publishContacts(channel : Channel, data : List[Json]) : Process[Task, Unit] = {
    val pub = RabbitMQ.Publisher(channel, qName)
    Process.emitAll(data.map(_.toString)) flatMap pub.publishProcess
  }

  override def main (args : Array[String]): Unit = {
    import argonaut._, Argonaut._
    val jsonContacts: List[Json] = createContacts.map(x => x.asJson)
    val connection = RabbitMQ.Connection(server, port)
    val channel = RabbitMQ.Channel(connection)
    val queue = RabbitMQ.Queue(channel)(qName)

    publishContacts(channel, jsonContacts).run.run

    val sub = Subscriber(channel, qName)
    val process = sub.subscribeZAll flatMap JsonConverter.process
    process.run.runAsync(_ match {
      case \/-(right) => System.out.println(right)
      case -\/(left) => System.out.println(left)
    })

  }
}
