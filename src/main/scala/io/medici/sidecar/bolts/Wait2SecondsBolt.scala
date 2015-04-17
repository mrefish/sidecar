package io.medici.sidecar.bolts

import backtype.storm.task.{OutputCollector, TopologyContext}
import backtype.storm.topology.OutputFieldsDeclarer
import backtype.storm.topology.base.BaseRichBolt
import backtype.storm.tuple.{Fields, Tuple, Values}
import backtype.storm.utils.Utils
import java.util.Map


class Wait2SecondsBolt extends BaseRichBolt {
  private val serialVersionUID:Long = -841805977046116528L
  var _collector: OutputCollector = null;

  @Override
  def prepare(stormConf: Map[_,_], context: TopologyContext, collector: OutputCollector) = {
    _collector = collector;
  }

  // expecting a tuple with a "message" field, which will be echoed
  @Override
  def execute(tuple: Tuple) {
    println("inside Wait2SecondsBolt, about to sleep with tuple: " + tuple);
    Utils.sleep(2000);
    _collector.emit(tuple, new Values("key", tuple.getStringByField("message")));
    _collector.ack(tuple);
  }

  @Override
  def declareOutputFields(declarer: OutputFieldsDeclarer) {
    // we need "key" and "message" when this goes out to the KafkaBolt
    declarer.declare(new Fields("key", "message"));
  }
}
