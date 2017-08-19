package com.example.HelloWorld

import akka.actor.{Actor, ActorSystem, Props}


// Define actor message
case class GreeterMsg(greeting:String)


// Define greeter actor
class Greeter extends Actor{
  override def receive: Receive = {
    case GreeterMsg(greeting) =>
      println(context.self)
      println(s"Received message $greeting")
  }
}


object HelloAkka{


  def main(args: Array[String]): Unit = {
    // Create a hello world Akka system
    val system = ActorSystem("HelloAkkaActorSystem")


    // Create the greeter actor
    val greeter = system.actorOf(Props[Greeter], "greeter")

    // Send message
    greeter ! GreeterMsg("Hello Akka!")

    println(s"Greeter actor: $greeter")

    Thread.sleep(3000)
    system.terminate()

  }


}
