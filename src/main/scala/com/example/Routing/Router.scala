package com.example.Routing

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.example.Routing.Router.RouterMessage

import scala.util.Random


object Worker{
  def props = Props[Worker]
}

class Worker extends Actor{
  override def receive: Receive = {
    case RouterMessage(msg) =>
      println(s"[Worker] message $msg")
  }

}



object Router{
  sealed trait RouterMsg

  // Messages
  case class RouterMessage(msg:String) extends RouterMsg

  def props = Props[Router]
}


class Router extends Actor{

  private var workers:List[ActorRef] = _

  override def preStart {
    workers = List.fill(5)(context.system.actorOf(Worker.props))
    workers.foreach(r => println(s"[Router] $r"))
  }

  override def receive: Receive = {
    case routerMessage: RouterMessage =>
      val i = Random.nextInt(workers.length)
      println(s"[Router] Forwarding to the message to one of the worker $i")
      workers(i).forward(routerMessage)
    case _ =>
      println("[Router] bad message")
  }
}


object mainApp{
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("RouterSystem")
    val router = system.actorOf(Router.props, "router")
    router ! RouterMessage("message 1")
    router ! RouterMessage("message 2")
    router ! RouterMessage("message 3")
    router ! RouterMessage("message 4")
    Thread.sleep(3000)
    system.terminate()
  }

}