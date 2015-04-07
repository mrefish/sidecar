package io.medici.sidecar.topologies

import backtype.storm.Config
import backtype.storm.StormSubmitter
import backtype.storm.topology.TopologyBuilder
import io.medici.sidecar.bolts._
import java.util.Properties
import storm.kafka.bolt.KafkaBolt
import storm.kafka.bolt.selector.DefaultTopicSelector
import storm.kafka.bolt.mapper.FieldNameBasedTupleToKafkaMapper
import storm.kafka.KafkaSpout
import storm.kafka.SpoutConfig
import storm.kafka.ZkHosts


object KafkaTopologyScala {

  val SENTENCE_SPOUT_ID = "kafka-sentence-spout"
  val SIMPLE_BOLT_ID = "acking-simple-bolt"
  val TOPOLOGY_NAME = "kafka-topology"
  val TOPIC = "sentences"
  val numSpoutExecutors = 1

  def main(args: Array[String]):Unit = {

    // make kafka spout, simple bolt, and kafka bolt
    val kspout = buildKafkaSentenceSpout()
    val simpleBolt = new SimpleBolt()
    val mapper = new FieldNameBasedTupleToKafkaMapper[String, String]("key", "message")
    val bolt = new KafkaBolt().
      withTopicSelector(new DefaultTopicSelector(TOPIC)).
      withTupleToKafkaMapper(mapper)

    // set kafka producer properties.
    val props = new Properties()
    props.put("metadata.broker.list", "localhost:9092")
    props.put("request.required.acks", "1")
    props.put("serializer.class", "kafka.serializer.StringEncoder")

    // make topology
    val builder = new TopologyBuilder()
    builder.setSpout(SENTENCE_SPOUT_ID, kspout, numSpoutExecutors)
    builder.setBolt(SIMPLE_BOLT_ID, simpleBolt).shuffleGrouping(SENTENCE_SPOUT_ID)
    builder.setBolt("httpBolt", new HttpBolt(), 1).shuffleGrouping(SIMPLE_BOLT_ID)
    builder.setBolt("forwardToKafka", bolt, 1).shuffleGrouping("httpBolt")

    // create topology config
    val conf = new Config()
    conf.put(KafkaBolt.KAFKA_BROKER_PROPERTIES, props)

    StormSubmitter.submitTopology(TOPOLOGY_NAME, conf, builder.createTopology())
  }

  def buildKafkaSentenceSpout(): KafkaSpout = {
    val zkHostPort = "localhost:2181"
    val zkRoot = "/acking-kafka-sentence-spout"
    val zkSpoutId = "acking-sentence-spout"
    val zkHosts = new ZkHosts(zkHostPort)

    val spoutCfg = new SpoutConfig(zkHosts, TOPIC, zkRoot, zkSpoutId)
    val kafkaSpout = new KafkaSpout(spoutCfg)
    kafkaSpout
  }
}
