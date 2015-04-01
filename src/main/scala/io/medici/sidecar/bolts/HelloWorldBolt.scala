package io.medici.sidecar.bolts

import backtype.storm.task.OutputCollector
import backtype.storm.task.TopologyContext
import backtype.storm.topology.OutputFieldsDeclarer
import backtype.storm.topology.base.BaseRichBolt
import backtype.storm.tuple.Fields
import backtype.storm.tuple.Tuple
import backtype.storm.tuple.Values
import java.util.Map
import org.apache.log4j.Logger


object HelloWorldBolt {
  private val LOG: Logger = Logger.getLogger(classOf[HelloWorldBolt])
}

class HelloWorldBolt extends BaseRichBolt {
  private val serialVersionUID:Long = -841805977046116528L
  private var myCount:Int = 0

  @Override
  def prepare(stormConf: Map[_,_], context: TopologyContext, collector: OutputCollector) = {}

  @Override
  def execute(input: Tuple) {
    val test = input.getStringByField("sentence")
    if(test == "Hello World"){
      myCount = myCount + 1
      System.out.println("Found a Hello World! My Count is now: " + Integer.toString(myCount))
      HelloWorldBolt.LOG.debug("Found a Hello World! My Count is now: " + Integer.toString(myCount))
    }
  }

  @Override
  def declareOutputFields(declarer: OutputFieldsDeclarer) {
    declarer.declare(new Fields("myCount"))
  }
}
