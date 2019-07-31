/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package myapps;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;


/**
 * In this example, we implement a simple LineSplit program using the high-level Streams DSL
 * that reads from a source topic "streams-plaintext-input", where the values of messages represent lines of text,
 * and writes the messages as-is into a sink topic "streams-pipe-output".
 */
public class Pipe {

    public static void main(String[] args) throws Exception {
    	//配置
    	Properties props = new Properties();
    	//同一个应用就用一个application_id
    	props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-pipe");
    	props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); 
    	
    	props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
    	props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

    	//定义处理逻辑。就是定义一个个processor
    	final StreamsBuilder builder = new StreamsBuilder();
    	
    	//定义一个source stream。会持续不断的从streams-plaintext-input读取数据。
    	//数据流类型是string的kv对
    	KStream<String, String> source = builder.stream("streams-plaintext-input");
    	
    	//读取数据后最简单的操作就是写入另外一个stream
    	source.to("streams-pipe-output");
    	
    	//看看这个builder定义的拓扑结构，其实就是处理流程
    	final Topology topology = builder.build();
    	System.out.println(topology.describe());
		
		final KafkaStreams streams = new KafkaStreams(topology, props);

		//CountDownLatch是倒数计时器
        final CountDownLatch latch = new CountDownLatch(1);
 
        // attach shutdown handler to catch control-c
        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });
 
        try {
            streams.start();
            latch.await();
        } catch (Throwable e) {
            System.exit(1);
        }
        System.exit(0);
    	
    }
}
