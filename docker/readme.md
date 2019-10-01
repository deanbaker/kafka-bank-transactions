
docker exec -it mongo bash
mongo 
use kafka_bank

# Kafka Connect

List connectors:
```sh
curl localhost:8083/connectors
curl -X POST -H "Content-Type: application/json" -d @connect-mongodb-sink.json localhost:8083/connectors
curl -X POST -H "Content-Type: application/json" -d @connect-mongodb-rolling-sink.json localhost:8083/connectors
curl -X POST -H "Content-Type: application/json" -d @connect-mongodb-total-sink.json localhost:8083/connectors
```


org.apache.kafka.connect.storage.StringConverter
org.apache.kafka.connect.json.JsonConverter
