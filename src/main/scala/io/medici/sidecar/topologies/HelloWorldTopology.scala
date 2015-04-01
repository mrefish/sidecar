package io.medici.sidecar.topologies

import backtype.storm.Config
import backtype.storm.LocalCluster
import backtype.storm.StormSubmitter
import backtype.storm.generated.AlreadyAliveException
import backtype.storm.generated.Nimbus.Client
import backtype.storm.topology.TopologyBuilder
import backtype.storm.utils.NimbusClient
import backtype.storm.utils.Utils
import io.medici.sidecar.bolts.HelloWorldBolt
import io.medici.sidecar.spouts.HelloWorldSpout
import java.util.Map
import org.json.simple.JSONValue


object HelloWorldTopology {
  final val NIMBUS_HOST = "nimbus1"
  final val NIMBUS_THRIFT_PORT = 6627
  final val ZOOKEEPER_SERVERS = "zookeeper1"
  final val ZOOKEEPER_PORT = 2181

  def main(args: Array[String]) = {
    var builder = new TopologyBuilder()
    builder.setSpout("randomHelloWorld", new HelloWorldSpout(), 2)
    builder.setBolt("HelloWorldBolt", new HelloWorldBolt(), 10)
      .shuffleGrouping("randomHelloWorld")

    var conf = new Config()

    if(args!=null && args.length > 0) {
      conf.setNumWorkers(20)
      StormSubmitter.submitTopology(args(0), conf, builder.createTopology())
    } else {
      conf.setDebug(true)

      var cluster = new LocalCluster()
      cluster.submitTopology("test", conf, builder.createTopology())
      Utils.sleep(10000)
      cluster.killTopology("test")
      cluster.shutdown()
    }
  }
}
