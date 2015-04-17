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


object SampleWorkflowTopology {

  val TOPIC = "broker-dealer"
  val TOPOLOGY_NAME = "bd-topology"

  def main(args: Array[String]):Unit = {

    val mapper = new FieldNameBasedTupleToKafkaMapper[String, String]
    val kafkaBolt = new KafkaBolt().
      withTopicSelector(new DefaultTopicSelector(TOPIC)).
      withTupleToKafkaMapper(mapper)

    // make topology
    val builder = new TopologyBuilder()
    builder.setSpout("kafka-spout", buildKafkaSentenceSpout(), 1)
    builder.setBolt("simple-bolt", new SimpleBolt()).shuffleGrouping("kafka-spout")
    builder.setBolt("wait2Seconds", new Wait2SecondsBolt()).shuffleGrouping("simple-bolt")
    builder.setBolt("forward-to-Kafka", kafkaBolt, 1).shuffleGrouping("wait2Seconds")

    StormSubmitter.submitTopology(TOPOLOGY_NAME, buildConfig(), builder.createTopology())
  }

  def buildConfig(): Config = {
    // create topology config
    val conf = new Config()
    // set kafka producer properties.
    val props = new Properties()
    props.put("metadata.broker.list", "localhost:9092")
    props.put("request.required.acks", "1")
    props.put("serializer.class", "kafka.serializer.StringEncoder")
    conf.put(KafkaBolt.KAFKA_BROKER_PROPERTIES, props)
    conf
  }

  def buildKafkaSentenceSpout(): KafkaSpout = {
    val zkHostPort = "localhost:2181"
    val zkRoot = "/acking-kafka-" + TOPIC + "-spout"
    val zkSpoutId = "acking-" + TOPIC + "-spout"
    val zkHosts = new ZkHosts(zkHostPort)

    val spoutCfg = new SpoutConfig(zkHosts, TOPIC, zkRoot, zkSpoutId)
    val kafkaSpout = new KafkaSpout(spoutCfg)
    kafkaSpout
  }
}


