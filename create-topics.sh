# Start a shell within the broker
docker-compose exec kafka-1 bash
# Crate the topics
kafka-topics --bootstrap-server kafka-1:19092 --create --topic transaction-topic --partitions 3 --replication-factor 3
kafka-topics --bootstrap-server kafka-1:19092 --create --topic category-topic --partitions 3 --replication-factor 3
kafka-topics --bootstrap-server kafka-1:19092 --create --topic enhanced-transaction-topic --partitions 3 --replication-factor 3
kafka-topics --bootstrap-server kafka-1:19092 --create \
            --topic customer-total-topic \
            --partitions 3 \
            --replication-factor 3 \
            --config cleanup.policy=compact \
            --config min.cleanable.dirty.ratio=0.01 \
            --config segment.ms=100
kafka-topics --bootstrap-server kafka-1:19092 --create \
            --topic customer-rolling-total-topic \
            --partitions 3 \
            --replication-factor 3 \
            --config cleanup.policy=compact \
            --config min.cleanable.dirty.ratio=0.01 \
            --config segment.ms=100
kafka-topics --bootstrap-server kafka-1:19092 --list



# Produce directly to the queue
kafka-console-producer --broker-list kafka-1:19092 --topic category-topic --property "parse.key=true" --property "key.separator=:"
CG01:Rent
CG02:Food
CG03:Beers
CG04:Whisky

# View the output of the categories
kafka-console-consumer --bootstrap-server kafka-1:19092 \
             --topic category-topic \
             --from-beginning \
             --formatter kafka.tools.DefaultMessageFormatter \
             --property print.key=true \
             --property print.value=true

# View the raw transactions
 kafka-console-consumer --bootstrap-server kafka-1:19092 \
             --topic transaction-topic \
             --from-beginning \
             --formatter kafka.tools.DefaultMessageFormatter \
             --property print.key=true \
             --property print.value=true \

# View the enhanced transactions
 kafka-console-consumer --bootstrap-server kafka-1:19092 \
             --topic enhanced-transaction-topic \
             --from-beginning \
             --formatter kafka.tools.DefaultMessageFormatter \
             --property print.key=true \
             --property print.value=true \



 kafka-console-consumer --bootstrap-server kafka-1:19092 \
             --topic customer-total-topic \
             --from-beginning \
             --formatter kafka.tools.DefaultMessageFormatter \
             --property print.key=true \
             --property print.value=true \
             --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
             --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer

kafka-console-consumer --bootstrap-server kafka-1:19092 \
             --topic customer-rolling-total-topic \
             --from-beginning \
             --formatter kafka.tools.DefaultMessageFormatter \
             --property print.key=true \
             --property print.value=true \
             --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
             --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer

