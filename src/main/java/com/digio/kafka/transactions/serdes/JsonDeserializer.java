package com.digio.kafka.transactions.serdes;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class JsonDeserializer<T> implements Deserializer {

    private static final Logger logger = LoggerFactory.getLogger(JsonDeserializer.class);
    private ObjectMapper mapper = new ObjectMapper();
    private Class<T> type;

    public JsonDeserializer(Class<T> type) {
        this.type = type;
    }

    @Override
    public void configure(Map map, boolean b) {
    }

    @Override
    public T deserialize(String s, byte[] bytes) {

        try {
            return mapper.readValue(bytes, type);
        } catch (IOException e) {
            logger.warn("Could not deserialise for type {}", this.type.getClass().getName());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void close() {
    }
}