package io.medici.sidecar.bolts

import backtype.storm.task.OutputCollector
import backtype.storm.task.TopologyContext
import backtype.storm.topology.OutputFieldsDeclarer
import backtype.storm.topology.base.BaseRichBolt
import backtype.storm.tuple.Fields
import backtype.storm.tuple.Tuple
import backtype.storm.tuple.Values
import dispatch._
import java.util.Map
import org.apache.log4j.Logger
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success, Try}


object HttpBolt {
  private val LOG: Logger = Logger.getLogger(classOf[HttpBolt])
}

class HttpBolt extends BaseRichBolt {
  @transient implicit val ctx: ExecutionContext = ExecutionContext.global
  var _collector: OutputCollector = null

  @Override
  def prepare(stormConf: Map[_,_], context: TopologyContext, collector: OutputCollector) = {
    _collector = collector
  }

  @Override
  def execute(input: Tuple) {
    val uri = "https://api.ripple.com/v1/server"

    val result = dispatch.Http(url(uri))

    result onComplete {
      case Success(content) => {
        _collector.emit(input, new Values("key", content.getResponseBody))
        _collector.ack(input)
      }
      case Failure(exception) => {
        _collector.reportError(exception)
        _collector.fail(input)
      }
    }
  }

  @Override
  def declareOutputFields(declarer: OutputFieldsDeclarer) {
    declarer.declare(new Fields("key", "message"))
  }
}
