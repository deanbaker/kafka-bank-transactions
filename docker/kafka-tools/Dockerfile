FROM openjdk:11-jre

ADD https://archive.apache.org/dist/kafka/2.2.0/kafka_2.12-2.2.0.tgz /root
RUN cd /root && tar -xvzf kafka_2.12-2.2.0.tgz

RUN apt-get update && apt-get install -y jq watch

WORKDIR /root/kafka_2.12-2.2.0
