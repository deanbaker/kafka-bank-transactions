
docker exec -it mongo bash
mongo 
use kafka_bank

# Kafka Connect

List connectors:
```sh
curl localhost:8083/connectors
curl -d @connect-mongodb-sink.json -X POST http://localhost:8083/connectors
```


org.apache.kafka.connect.storage.StringConverter
org.apache.kafka.connect.json.JsonConverter
