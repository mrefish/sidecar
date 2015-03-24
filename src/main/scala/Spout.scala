package io.medici.sidecar

import java.util.Map
import java.util.Random

import org.apache.log4j.Logger

import backtype.storm.spout.SpoutOutputCollector
import backtype.storm.task.TopologyContext
import backtype.storm.topology.OutputFieldsDeclarer
import backtype.storm.topology.base.BaseRichSpout
import backtype.storm.tuple.Fields
import backtype.storm.tuple.Values
import backtype.storm.utils.Utils


object HelloWorldSpout {
  private val LOG:Logger = Logger.getLogger(classOf[HelloWorldSpout])
}


class HelloWorldSpout extends BaseRichSpout {
    private val serialVersionUID:Long = -4646687160233411001L
    private var collector:SpoutOutputCollector = _

    private val referenceRandom = new Random().nextInt(MAX_RANDOM)
    private final val MAX_RANDOM = 10

    @Override
    def open(conf:Map[_,_], context:TopologyContext, collector:SpoutOutputCollector) = {
        this.collector = collector
    }

    @Override
    def nextTuple() = {
        Utils.sleep(100)
        val rand = new Random()
        val instanceRandom = rand.nextInt(MAX_RANDOM)
        if(instanceRandom == referenceRandom){
            collector.emit(new Values("Hello World"))
        }
    else {
            collector.emit(new Values("Other Random Word"))
      }
    }

    @Override
    def declareOutputFields(declarer:OutputFieldsDeclarer) = {
        declarer.declare(new Fields("sentence"))
    }

}
