package com.digio.kafka.transactions;

import com.digio.kafka.transactions.serdes.MessageTimeExtractor;
import com.digio.kafka.transactions.serdes.TransactionSerde;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class StreamingTopology {

    private static Logger logger = LoggerFactory.getLogger(StreamingTopology.class.getName());


    public static final Properties config() {
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "bank-starter-app");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, TransactionSerde.class.getName());

        return config;
    }

    public static KStream<String, Transaction> createStream(StreamsBuilder builder) {

        return builder.stream("transaction-topic", Consumed.with(new Serdes.StringSerde(), new TransactionSerde())
                .withTimestampExtractor(new MessageTimeExtractor()));
    }

    public static KStream<String, Long> computeTotals(KStream<String, Transaction> kstream) {

        return kstream.groupByKey()
                .aggregate(
                        () -> 0L,
                        (key, value, aggregate) -> aggregate + value.getAmount(),
                        Materialized.with(new Serdes.StringSerde(), new Serdes.LongSerde()))
                .toStream();
    }

    public static KStream<Windowed<String>, Long> computeRunningTotal(KStream<String, Transaction> kStream) {

        long windowSizeMs = TimeUnit.SECONDS.toMillis(30);
        TimeWindows weeklySpendWindow = TimeWindows.of(windowSizeMs);

        return kStream
                .groupByKey()
                .windowedBy(weeklySpendWindow)
                .aggregate(
                        () -> 0L,
                        (key, value, aggregate) -> aggregate + value.getAmount(),
                        Materialized.with(new Serdes.StringSerde(), new Serdes.LongSerde()))
                .toStream();
    }

   public static GlobalKTable<String, String> createCategoryLookupTable(StreamsBuilder builder) {

        return null;
    }

    public static KStream<String, Transaction> categorisedStream(KStream<String, Transaction> kStream, GlobalKTable<String, String> kTable) {

        return null;
    }

    public static void topology(StreamsBuilder builder) {
        Produced<String, Long> stringLongProduced = Produced.with(new Serdes.StringSerde(), new Serdes.LongSerde());


        KStream<String, Transaction> stream = createStream(builder);
        KStream<String, Long> totalStream = computeTotals(stream);
        KStream<Windowed<String>, Long> windowedLongKStream = computeRunningTotal(stream);
        KStream<String, Transaction> enhancedTransactions = categorisedStream(stream, createCategoryLookupTable(builder));
    }

    public static void main(String[] args) {

        StreamsBuilder builder = new StreamsBuilder();
        topology(builder);

        // Start and start streaming!
        Topology topology = builder.build();
        logger.info(topology.describe().toString());
        KafkaStreams streams = new KafkaStreams(topology, config());
        streams.cleanUp(); // only do this in dev - not in prod
        streams.start();
        // shutdown hook to correctly close the streams application
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

}
