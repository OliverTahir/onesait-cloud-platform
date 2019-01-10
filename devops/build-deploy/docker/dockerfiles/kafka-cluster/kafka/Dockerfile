FROM confluentinc/cp-kafka:4.1.0

# Metadata
LABEL module.maintainer "onesaitplatform@indra.es" \
      module.name="kafkabroker"

# Default hostname
ENV SERVER_NAME=localhost \
    ZOOKEEPER_SERVER=localhost

# Copy kafka plugin and config
COPY kafka-server-start /usr/bin/kafka-server-start
# COPY jolokia-jvm-1.5.0-agent.jar /usr/bin/jolokia-jvm-1.5.0-agent.jar
COPY server.properties /etc/kafka/server.properties
COPY kafka_server.jaas /etc/kafka/kafka_server.jaas
COPY onesaitplatform-kafka-login*.jar /usr/share/java/kafka/onesaitplatform-kafka-login.jar

RUN chmod -R 777 /var/log
      
EXPOSE 9093 9092 9999 7778 9095

ENTRYPOINT ["kafka-server-start", "/etc/kafka/server.properties"]
