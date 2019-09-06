package com.digio.kafka.transactions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.IntStream;

/**
 * TransactionProducer will publish transactions
 */
public class TransactionProducer {

    private static Logger logger = LoggerFactory.getLogger(TransactionProducer.class.getName());

    public static void main(String[] args) {

        LegacyBankingSystem legacyBankingSystem = new LegacyBankingSystem();
        ObjectMapper objectMapper = new ObjectMapper();

        Properties properties = new Properties();

        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // producer acks
        properties.setProperty(ProducerConfig.ACKS_CONFIG, "all"); // strongest producing guarantee
        properties.setProperty(ProducerConfig.RETRIES_CONFIG, "3");
        properties.setProperty(ProducerConfig.LINGER_MS_CONFIG, "1");

        // leverage idempotent producer from Kafka 0.11 !
        properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true"); // ensure we don't push duplicates

        Producer<String, String> producer = new KafkaProducer<>(properties);

        // Do the thing.
        TimerTask repeatedTask = new TimerTask() {
            public void run() {
                // Write 1 per second - ramp this up to 100 when we get moving!
                IntStream.range(0, 1).forEach(value -> {
                    Transaction transaction = legacyBankingSystem.getTransaction();
                    logger.info("Transaction {}", transaction);
                    try {
                        producer.send(new ProducerRecord<>(
                                "transaction-topic",
                                transaction.getCustomer(),
                                objectMapper.writeValueAsString(transaction)));
                    } catch (JsonProcessingException e) {
                        logger.info("Could not write out my transaction!");
                    }
                });
                producer.flush();
            }
        };
        Timer timer = new Timer("Timer");
        long delay = 1000L;
        long period = 1000L;
        timer.scheduleAtFixedRate(repeatedTask, delay, period);

        // Close the producer gracefully
        Runtime.getRuntime().addShutdownHook(new Thread(producer::close));
    }
}
