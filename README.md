# Kafka Transactions

## Problem statement

We want to create a streaming application that will make use of some simple account transaction messages.

```json
{
  "customer": "Bob",
  "amount": 64,
  "category": "CG01",
  "occurred": 1567148940000  
}
```

Broadly we will perform the following tasks:
* Calculate the total for each customer.
* Calculate the total for a month for each customer
* Enrich the transaction data with another source.

To do this we will use 5 topics, 2 input and 3 output:

| Topic                        | Type | Key    | Value  |
| ---------------------------- |--------| -------|---|
| transactions-topic           | Input | String | json (String) |
| category-topic               | Input | String |   String |
| customer-total-topic         | Output | String |   Long |
| customer-rolling-total-topic | Output |String |   Long |
| enhanced-transactions-topic  | OUtput | String | json (String) |

We do not need to tell Kafka what it will be storing when we create the topics, but we do need to tell
our `Producer` and `Streaming Application` how to serialize and deserialize these values.

### The Producer

Ultimately we will be writing transactions to a topic `transactions-topic` in json format. Have a bit of a think
about what key we should use!


We will be following kafka's exactly-once semantics, read about it here: https://www.confluent.io/blog/exactly-once-semantics-are-possible-heres-how-apache-kafka-does-it/

The producer will have access to a `LegacyBankingSystem` that will provide you with 
transactions to be published, so don't worry about generating this data yourself.

### Calculating a customers balance

We will then create a streaming topology that can handle these events. We will then 
aggregate a customers balance and write this back to another topic - for this we will use a 
`compacted` topic: https://kafka.apache.org/documentation/#compaction

If we were to see the following events to the `transactions-topic`:
```json
[{
  "customer": "Bob",
  "amount": 64,
  "category": "CG01",
  "occurred": 1567148940000  
},
{
  "customer": "Bob",
  "amount": 64,
  "category": "CG01",
  "occurred": 1567148940000  
},
{
  "customer": "Alice",
  "amount": 64,
  "category": "CG01",
  "occurred": 1567148940000  
}]
```

How many records should be in the `customer-total-topic`? And what should their values be?


### Calculating the last 30 day spend.

We will use the same topology to create another view of the data - the total spend over the 
last 30 days (so the balance will reset to 0 after 30 days)

Again we will look to use a Compacted topic here.


### Enriching transaction data.

You might notice that there is a `category` code embedded into each transaction record. Not very useful huh?
Let's use a GlobalKTable to allow us to reference data sourced from the `category-topic` to enrich out transactions.
If there is no corresponding value for a category, we will retain the code value. 

https://docs.confluent.io/current/streams/concepts.html#streams-concepts-ktable


## Scripts to help

Go have a look at `create-topics.sh` file for more details... 

### Create the topics

#### Create the input topic:
```bash
./bin/kafka-topics.sh --bootstrap-server kafka-1:19092 --create --topic transaction-topic --partitions 1 --replication-factor 1
```

#### Create the category topic:
```bash
./bin/kafka-topics.sh --bootstrap-server kafka-1:19092 --create --topic category-topic --partitions 1 --replication-factor 1
```

#### Create the enhanced transaction topic
```bash
./bin/kafka-topics.sh --bootstrap-server kafka-1:19092 --create --topic enhanced-transaction-topic --partitions 1 --replication-factor 1 
```

#### Create the running total topic:
```bash
./bin/kafka-topics.sh --bootstrap-server kafka-1:19092 --create \
            --topic customer-total-topic \
            --partitions 1 \
            --replication-factor 1 \
            --config cleanup.policy=compact \
            --config min.cleanable.dirty.ratio=0.01 \
            --config segment.ms=100
```
You will notice here are are using a `cleanup.policy` of `compact`, but also making
the segment super small (100ms)! This is so we can see the log compaction happening without
waiting the default hours to see compaction in action.

#### Create the windowed topic 

```bash
./bin/kafka-topics.sh --bootstrap-server kafka-1:19092 --create \
            --topic customer-rolling-total-topic \
            --partitions 1 \
            --replication-factor 1 \
            --config cleanup.policy=compact \
            --config min.cleanable.dirty.ratio=0.01 \
            --config segment.ms=100
```

#### Consume a topic
We will use the console consumer to keep track of how things are going:
```bash
 ./bin/kafka-console-consumer.sh --bootstrap-server kafka-1:19092 \
             --topic customer-total-topic \
             --from-beginning \
             --formatter kafka.tools.DefaultMessageFormatter \
             --property print.key=true \
             --property print.value=true \
             --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
             --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer
```

#### Other useful commands
````bash
# list topics
./bin/kafka-topics.sh --bootstrap-server kafka-1:19092 --list
 
 
 # Read the raw transactions
 ./bin/kafka-console-consumer.sh --bootstrap-server kafka-1:19092 \
             --topic transaction-topic \
             --from-beginning \
             --formatter kafka.tools.DefaultMessageFormatter \
             --property print.key=true \
             --property print.value=true \ 
             
# Produce directly to the queue             
./bin/kafka-console-producer.sh --broker-list kafka-1:19092 --topic category-topic --property "parse.key=true" --property "key.separator=:"             
````
