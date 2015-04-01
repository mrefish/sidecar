package io.medici.sidecar.bolts;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

public class SimpleBolt extends BaseRichBolt {

  private OutputCollector _collector;

  @Override @SuppressWarnings("rawtypes")
  public void prepare(Map stormConf, TopologyContext context, OutputCollector outCollector) {
    _collector = outCollector;
  }

  @Override
  public void execute(Tuple tuple) {
    Object value = tuple.getValue(0);
    String sentence = null;
    if (value instanceof String) {
      sentence = (String) value;

    } else {
      // Kafka returns bytes
      byte[] bytes = (byte[]) value;
      try {
        sentence = new String(bytes, "UTF-8");
      } catch (UnsupportedEncodingException e) {
        throw new RuntimeException(e);
      }
    }

    // remove last character and send to kafka
    if (sentence.length() > 1) {
      _collector.emit(tuple, new Values("key", sentence.substring(0, sentence.length() -1)));
    }

    _collector.ack(tuple);
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer) {
    declarer.declare(new Fields("key", "message"));
  }
}
