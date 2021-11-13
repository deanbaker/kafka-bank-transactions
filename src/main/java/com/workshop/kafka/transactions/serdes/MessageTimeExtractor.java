package com.workshop.kafka.transactions.serdes;

import com.workshop.kafka.transactions.Transaction;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;

public class MessageTimeExtractor implements TimestampExtractor {

    @Override
    public long extract(ConsumerRecord<Object, Object> consumerRecord, long l) {

        return ((Transaction) consumerRecord.value()).getOccurred();
    }
}
