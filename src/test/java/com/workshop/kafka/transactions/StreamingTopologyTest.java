package com.workshop.kafka.transactions;

import com.workshop.kafka.transactions.serdes.JsonDeserializer;
import com.workshop.kafka.transactions.serdes.JsonSerializer;
import junit.framework.TestCase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.test.ConsumerRecordFactory;

import java.util.Date;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

public class StreamingTopologyTest extends TestCase {

    private static ConsumerRecordFactory transactionRecordFactory =
            new ConsumerRecordFactory<>("transaction-topic", new StringSerializer(), new JsonSerializer<>());

    private static ConsumerRecordFactory categoryRecordFactory =
            new ConsumerRecordFactory<>("category-topic", new StringSerializer(), new StringSerializer());

    private static ConsumerRecord<byte[], byte[]> createTransactionRecord(Transaction message) {
        return transactionRecordFactory.create("transaction-topic", message.getCustomer(), message);
    }

    private static ConsumerRecord<byte[], byte[]> createCategoryRecord(String key, String value) {
        return categoryRecordFactory.create("category-topic", key, value);
    }

    private static ProducerRecord<String, Long> readOutput(TopologyTestDriver driver) {
        return driver.readOutput("output", new StringDeserializer(), new LongDeserializer());
    }

    private static ProducerRecord<String, Transaction> readTransaction(TopologyTestDriver driver) {
        return driver.readOutput("output", new StringDeserializer(), new JsonDeserializer<Transaction>(Transaction.class));
    }


    public void testTotal() {

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Transaction> stream = StreamingTopology.createStream(builder);
        KStream<String, Long> totals = StreamingTopology.computeTotals(stream);
        totals.to("output", Produced.with(new Serdes.StringSerde(), new Serdes.LongSerde()));

        try (TopologyTestDriver testDriver = new TopologyTestDriver(builder.build(), StreamingTopology.config())) {
            testDriver.pipeInput(createTransactionRecord(new Transaction().setCustomer("dean").setAmount(150).setOccurred(new Date().getTime())));
            testDriver.pipeInput(createTransactionRecord(new Transaction().setCustomer("alice").setAmount(6).setOccurred(new Date().getTime())));
            testDriver.pipeInput(createTransactionRecord(new Transaction().setCustomer("dean").setAmount(100).setOccurred(new Date().getTime())));

            readOutput(testDriver);
            ProducerRecord<String, Long> aliceTotal = readOutput(testDriver);
            ProducerRecord<String, Long> deanTotal = readOutput(testDriver);

            assertThat(deanTotal.key(), is("dean"));
            assertThat(deanTotal.value(), is(250L));

            assertThat(aliceTotal.key(), is("alice"));
            assertThat(aliceTotal.value(), is(6L));
        }
    }


    public void testRollingTotalInSameWindow() {
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Transaction> stream = StreamingTopology.createStream(builder);
        KStream<Windowed<String>, Long> totals = StreamingTopology.computeRunningTotal(stream);
        totals.to("output", Produced.with(WindowedSerdes.timeWindowedSerdeFrom(String.class), new Serdes.LongSerde()));



        try (TopologyTestDriver testDriver = new TopologyTestDriver(builder.build(), StreamingTopology.config())) {

            long now = new Date().getTime();

            testDriver.pipeInput(createTransactionRecord(new Transaction().setCustomer("dean").setAmount(150).setOccurred(now)));
            testDriver.pipeInput(createTransactionRecord(new Transaction().setCustomer("dean").setAmount(100).setOccurred(now)));

            readOutput(testDriver);
            ProducerRecord<String, Long> deanTotal = readOutput(testDriver);

            assertThat(deanTotal.value(), is(250L));
        }
    }

    public void testRollingTotalInDifferentWindow() {
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Transaction> stream = StreamingTopology.createStream(builder);
        KStream<Windowed<String>, Long> totals = StreamingTopology.computeRunningTotal(stream);
        totals.to("output", Produced.with(WindowedSerdes.timeWindowedSerdeFrom(String.class), new Serdes.LongSerde()));



        try (TopologyTestDriver testDriver = new TopologyTestDriver(builder.build(), StreamingTopology.config())) {

            long now = new Date().getTime();

            testDriver.pipeInput(createTransactionRecord(new Transaction().setCustomer("dean").setAmount(150).setOccurred(now)));
            testDriver.pipeInput(createTransactionRecord(new Transaction().setCustomer("dean").setAmount(100).setOccurred(now + 1000 * 40)));

            readOutput(testDriver);
            ProducerRecord<String, Long> deanTotal = readOutput(testDriver);

            assertThat(deanTotal.value(), is(100L));
        }
    }

   public void testLookup() {

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Transaction> transactionStream = StreamingTopology.createStream(builder);
        GlobalKTable<String, String> lookup = StreamingTopology.createCategoryLookupTable(builder);

        KStream<String, Transaction> enhanced = StreamingTopology.categorisedStream(transactionStream, lookup);
        enhanced.to("output");


        try(TopologyTestDriver testDriver = new TopologyTestDriver(builder.build(), StreamingTopology.config())) {
            String categoryKey = "CG01";
            testDriver.pipeInput(createCategoryRecord(categoryKey, "Groceries"));
            testDriver.pipeInput(createTransactionRecord(new Transaction().setCustomer("dean").setAmount(100).setCategory(categoryKey).setOccurred(new Date().getTime())));


            ProducerRecord<String, Transaction> actual = readTransaction(testDriver);

            // Assert that there is only one message on the topic
            assertThat(testDriver.readOutput("output"), is(nullValue()));

            assertThat(actual.value().getAmount(), is(100));
            assertThat(actual.value().getCategory(), is("Groceries"));

        }

    }
}
