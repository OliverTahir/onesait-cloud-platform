# Step1 - nodejs binaries
FROM node:8.11.4-alpine AS nodebase

# Step2 - openjdk base image
FROM openjdk:8-jdk-alpine

# Metadata
LABEL module.maintainer="onesaitplatform@indra.es" \
	  module.name="flowengine"	

ENV JAVA_OPTS="$JAVA_OPTS -Xms1G -Xmx3G" \
	SERVERNAME=localhost \
    REALTIMEDBSERVERS=realtimedb:27017 \
    REALTIMEDBAUTHDB=admin \    
	REALTIMEDBUSEAUTH=true \
	REALTIMEDBUSER=platformadmin \
	REALTIMEDBPASS=0pen-platf0rm-2018! \     
    REALTIMEDBWRITECONCERN=UNACKNOWLEDGED \	
    CONFIGDBHOST=configdb \
    CONFIGDBPORT=3306 \
    ELASTICDBHOST=elasticdb \
    ELASTICDBPORT=9200 \    
    QUASARHOST=quasar \
    QUASARPORT=10800

COPY --from=nodebase /usr/local/bin/node /bin

RUN mkdir -p /opt/nodeRed && \
	mkdir -p /tmp/logs/flowEngine && \
    mkdir -p /var/log/platform-logs && \
	mkdir ./target	

ADD *-exec.jar app.jar
ADD Flow-Engine-Manager /opt/nodeRed/Flow-Engine-Manager

# create onesait user/group
RUN addgroup -S onesait -g 433 && adduser -u 431 -S -g onesait -h /usr/local -s /sbin/nologin onesait 

RUN chown -R onesait:onesait /usr/local && \
    chown -R onesait:onesait /var/log/platform-logs && \
    chown -R onesait:onesait ./target && \
    chown -R onesait:onesait /opt/nodeRed && \
    chown onesait:onesait app.jar && \
    chmod -R 777 ./target && \
    chmod -R 777 /var/log && \
    chmod -R 777 /usr/local && \
    chmod -R 777 /opt/nodeRed
    
VOLUME ["/tmp", "/var/log/platform-logs"]
    
USER onesait

EXPOSE 10000 20100 5050

COPY docker-entrypoint.sh /usr/local/bin/
ENTRYPOINT ["docker-entrypoint.sh"]
