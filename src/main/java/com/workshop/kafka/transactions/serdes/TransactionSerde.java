package com.workshop.kafka.transactions.serdes;

import com.workshop.kafka.transactions.Transaction;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class TransactionSerde implements Serde {
    @Override
    public void configure(Map map, boolean b) {
    }

    @Override
    public void close() {
    }

    @Override
    public Serializer serializer() {
        return new JsonSerializer<Transaction>();
    }

    @Override
    public Deserializer deserializer() {
        return new JsonDeserializer<>(Transaction.class);
    }
}
