package io.medici.sidecar.topologies;

import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;
import io.medici.sidecar.bolts.SimpleBolt;
import java.util.Properties;
import storm.kafka.bolt.KafkaBolt;
import storm.kafka.bolt.selector.DefaultTopicSelector;
import storm.kafka.bolt.mapper.FieldNameBasedTupleToKafkaMapper;
import storm.kafka.KafkaSpout;
import storm.kafka.SpoutConfig;
import storm.kafka.ZkHosts;


public class KafkaTopology {

  private static final String SENTENCE_SPOUT_ID = "kafka-sentence-spout";
  private static final String SIMPLE_BOLT_ID = "acking-simple-bolt";
  private static final String TOPOLOGY_NAME = "kafka-topology";
  private static final String TOPIC = "sentences";
  private static final int numSpoutExecutors = 1;

  public static void main(String[] args) throws Exception {

    // make kafka spout, simple bolt, and kafka bolt
    KafkaSpout kspout = buildKafkaSentenceSpout();
    SimpleBolt simpleBolt = new SimpleBolt();
    KafkaBolt bolt = new KafkaBolt()
      .withTopicSelector(new DefaultTopicSelector(TOPIC))
      .withTupleToKafkaMapper(new FieldNameBasedTupleToKafkaMapper("key", "message"));

    // set kafka producer properties.
    Properties props = new Properties();
    props.put("metadata.broker.list", "localhost:9092");
    props.put("request.required.acks", "1");
    props.put("serializer.class", "kafka.serializer.StringEncoder");

    // make topology
    TopologyBuilder builder = new TopologyBuilder();
    builder.setSpout(SENTENCE_SPOUT_ID, kspout, numSpoutExecutors);
    builder.setBolt(SIMPLE_BOLT_ID, simpleBolt).shuffleGrouping(SENTENCE_SPOUT_ID);
    builder.setBolt("forwardToKafka", bolt, 1).shuffleGrouping(SIMPLE_BOLT_ID);

    // create topology config
    Config conf = new Config();
    conf.put(KafkaBolt.KAFKA_BROKER_PROPERTIES, props);

    StormSubmitter.submitTopology(TOPOLOGY_NAME, conf, builder.createTopology());
  }

  private static KafkaSpout buildKafkaSentenceSpout() {
    String zkHostPort = "localhost:2181";
    String zkRoot = "/acking-kafka-sentence-spout";
    String zkSpoutId = "acking-sentence-spout";
    ZkHosts zkHosts = new ZkHosts(zkHostPort);

    SpoutConfig spoutCfg = new SpoutConfig(zkHosts, TOPIC, zkRoot, zkSpoutId);
    KafkaSpout kafkaSpout = new KafkaSpout(spoutCfg);
    return kafkaSpout;
  }
}
