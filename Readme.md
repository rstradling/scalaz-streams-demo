This is an early pass at using Scalaz-streams.  It assumes that you have RabbitMQ and Postgresql installed on your machine.
It takes some dummy json data, publishes it to a queue, listens to the queue, converts it back to json, 
and then pushes it to a postgresql database.

It is meant to illustrate my learnings with Scalaz-streams that is also hopefully helpful to others.