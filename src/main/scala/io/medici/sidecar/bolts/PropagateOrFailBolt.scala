package io.medici.sidecar.bolts

import backtype.storm.task.{OutputCollector, TopologyContext}
import backtype.storm.topology.OutputFieldsDeclarer
import backtype.storm.topology.base.BaseRichBolt
import backtype.storm.tuple.{Fields, Tuple, Values}
import java.util.Map


class PropagateOrFailBolt extends BaseRichBolt {

  private val serialVersionUID:Long = -6740037542382180320L

  var _collector: OutputCollector = null;

  @Override
  def prepare(stormConf: Map[_,_], context: TopologyContext, collector: OutputCollector) = {
    _collector = collector;
  }

  // expecting a tuple with a "message" field, which will be echoed
  @Override
  def execute(tuple: Tuple) {

    val message = tuple.getValue(0) match {
      case str: String => str
      case bytes: Array[Byte] => new String(bytes, "UTF-8")
      case x => 
        System.out.println("Got bolt value I don't understand: " + x)
        "err"
    }
    System.out.println("Propagate got message: " + message)

    System.out.println("Propagating message for message " + message)
    val commands = message.split(" ");
    commands(0) match {
      case "err" => 
        // not throwing exception, since that would bypass emit & cause retries
        System.out.println("Hit error, so ending propagation for " + message)
      case _ =>
        if (commands.length > 1) {
          _collector.emit(tuple, new Values("someKey", commands.tail.fold("")({_+" "+_}).tail));
        }
    }
    _collector.ack(tuple);
  }

  @Override
  def declareOutputFields(declarer: OutputFieldsDeclarer) {
    declarer.declare(new Fields("key", "message"));
  }
}
