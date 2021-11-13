package com.workshop.kafka.transactions.serdes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class JsonSerializer<T> implements Serializer<T> {

    private static final Logger logger = LoggerFactory.getLogger(JsonDeserializer.class);
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void configure(Map map, boolean b) {
    }

    @Override
    public byte[] serialize(String topic, T o) {
        byte[] retVal = null;

        try {
            retVal = objectMapper.writeValueAsBytes(o);
        } catch (JsonProcessingException e) {
            logger.error(e.toString());
        }

        return retVal;
    }

    @Override
    public void close() {
    }
}
