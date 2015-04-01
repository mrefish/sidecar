# sidecar


## Play with Kafka & Storm &c

- download [Kafka](http://kafka.apache.org/downloads.html)... eg. 0.9.3 binaries

- edit Storm's conf/storm.yaml and add:
```
storm.zookeeper.servers:
     - "localhost"
nimbus.host: "localhost"
```

- download [Storm](https://storm.apache.org/downloads.html)... eg. 2.11-0.8.2.1 binaries

- clone kstorm repo: https://github.com/quux00/kstorm
... and build it: mvn package

- run Zookeeper, in Kafka dir: `bin/zookeeper-server-start.sh config/zookeeper.properties`
- run Kafka, in Kafka dir: `bin/kafka-server-start.sh config/server.properties`
- run Nimbus, in Storm dir: `bin/storm nimbus`
- run Storm, in Storm dir: `bin/storm supervisor`

- set up "sentences" topic, from Kafka dir: `bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic sentences`
- install kstorm bolts, from Storm dir (after filling in PATH_TO_STORM): `bin/storm jar $PATH_TO_KSTORM/target/kstorm-1.0-SNAPSHOT-jar-with-dependencies.jar quux00.wordcount.kafka.WordCountAckedTopology`

Oh... you want to see something working?
- edit SplitSentenceBolt.java in kstorm, eg. add this before the emit: `System.out.println("SSBolt: " + word);`
- recompile kstorm, in kstorm dir: `mvn clean package`
- kill previous topology, in Storm dir: `bin/storm kill acking-word-storm-topology`
- reinstall topology, in Storm dir (after that topology disappears from Storm): `bin/storm jar $PATH_TO_KSTORM/target/kstorm-1.0-SNAPSHOT-jar-with-dependencies.jar quux00.wordcount.kafka.WordCountAckedTopology`
- add messages to Kafka (see below, but using "sentences" topic instead of "test")
- watch for your output in the Storm log in logs/worker-6700.log

Other curiosities:
- see Kafka topics, in Kafka dir: `bin/kafka-topics.sh --list --zookeeper localhost:2181`
- see all Kafka messages, in Kafka dir: `bin/kafka-console-consumer.sh --zookeeper localhost:2181 --from-beginning --topic test`
- add messages to Kafka, in Kafka dir: `bin/kafka-console-producer.sh --broker-list localhost:9092 --topic test`
... which waits while you type in a bunch of test data... CTRL-C to quit.

