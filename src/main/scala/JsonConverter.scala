import argonaut._, Argonaut._

import scalaz.concurrent.Task
import scalaz.{\/-, -\/, \/}

object JsonConverter {
  def JsonStringToPerson(person: String): Person = {
    val p = person.decodeOption[Person].get
    System.out.println("person = " + p)
    p
  }

  def process(input: String): scalaz.stream.Process[Task, Person] = scalaz.stream.Process.eval(Task.delay(JsonStringToPerson(input)))

}

