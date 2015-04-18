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


abstract class WorkflowTopology(fromTopic: String, toTopic: String) {

  val _fromTopic = fromTopic
  val _toTopic = toTopic
  val mapper = new FieldNameBasedTupleToKafkaMapper[String, String]
  val kafkaBolt = new KafkaBolt().
      withTopicSelector(new DefaultTopicSelector(_toTopic)).
      withTupleToKafkaMapper(mapper)

  def main(args: Array[String]):Unit;

  def buildConfig(): Config = {
    // create topology config
    val conf = new Config()
    // set kafka producer properties
    val props = new Properties()
    props.put("metadata.broker.list", "localhost:9092")
    props.put("request.required.acks", "1")
    props.put("serializer.class", "kafka.serializer.StringEncoder")
    conf.put(KafkaBolt.KAFKA_BROKER_PROPERTIES, props)
    conf
  }

  def buildKafkaTopicSpout(topic: String): KafkaSpout = {
    val zkHostPort = "localhost:2181"
    val zkRoot = "/acking-kafka-" + topic + "-spout"
    val zkSpoutId = "acking-" + topic + "-spout"
    val zkHosts = new ZkHosts(zkHostPort)

    val spoutCfg = new SpoutConfig(zkHosts, topic, zkRoot, zkSpoutId)
    val kafkaSpout = new KafkaSpout(spoutCfg)
    kafkaSpout
  }

}

object ChopAndWaitWorkflowTopology extends WorkflowTopology("matching-engine", "ledger") {
  def main(args: Array[String]):Unit = {
    val builder = new TopologyBuilder()
    builder.setSpout("kafka-spout", buildKafkaTopicSpout(_fromTopic), 1)
    builder.setBolt("simple-bolt", new SimpleBolt()).shuffleGrouping("kafka-spout")
    builder.setBolt("wait2Seconds", new Wait2SecondsBolt()).shuffleGrouping("simple-bolt")
    builder.setBolt("forward-to-kafka", kafkaBolt, 1).shuffleGrouping("wait2Seconds")
    StormSubmitter.submitTopology(_fromTopic + "-topology", buildConfig(), builder.createTopology())
  }
}

object FromLedgerWorkflowTopology extends WorkflowTopology("ledger", "broker-dealer") {
  def main(args: Array[String]):Unit = {
    val builder = new TopologyBuilder()
    builder.setSpout("kafka-spout", buildKafkaTopicSpout(_fromTopic), 1)
    builder.setBolt("propagate-bolt", new PropagateOrFailBolt()).shuffleGrouping("kafka-spout")
    builder.setBolt("forward-to-kafka", kafkaBolt, 1).shuffleGrouping("propagate-bolt")
    StormSubmitter.submitTopology(_fromTopic + "-topology", buildConfig(), builder.createTopology())
  }
}

object IntoLedgerWorkflowTopology extends WorkflowTopology("broker-dealer", "ledger") {
  def main(args: Array[String]):Unit = {
    val builder = new TopologyBuilder()
    builder.setSpout("kafka-spout", buildKafkaTopicSpout(_fromTopic), 1)
    builder.setBolt("propagate-bolt", new PropagateOrFailBolt()).shuffleGrouping("kafka-spout")
    builder.setBolt("forward-to-kafka", kafkaBolt, 1).shuffleGrouping("propagate-bolt")
    StormSubmitter.submitTopology(_fromTopic + "-topology", buildConfig(), builder.createTopology())
  }
}

