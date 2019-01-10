FROM openjdk:8-jdk-alpine

# Metadata
LABEL module.maintainer="onesaitplatform@indra.es" \
	  module.name="rtdbmaintainer"	

ENV JAVA_OPTS="$JAVA_OPTS -Xms1G -Xmx3G" \
	SERVER_NAME=localhost \    
    CONFIGDBHOST=configdb \
    CONFIGDBPORT=3306 \
    REALTIMEDBSERVERS=realtimedb:27017 \
    REALTIMEDBAUTHDB=admin \
	REALTIMEDBUSEAUTH=true \
	REALTIMEDBUSER=platformadmin \
	REALTIMEDBPASS=0pen-platf0rm-2018! \     
    REALTIMEDBWRITECONCERN=UNACKNOWLEDGED \	
    SCHEDULERDBHOST=schedulerdb \
    SCHEDULERDBPORT=3306 \ 
    ELASTICDBHOST=elasticdb \
    ELASTICDBPORT=9200 \          
    QUASARHOST=quasar \
    QUASARPORT=10800     

RUN mkdir -p /var/log/platform-logs && \
    mkdir -p /tmp/export && \
    mkdir -p /tmp/rtdbhdb && \
    mkdir -p /usr/local/files && \
	mkdir ./target	

ADD *-exec.jar app.jar

# create onesait user/group
RUN addgroup -S onesait -g 433 && adduser -u 431 -S -g onesait -h /usr/local -s /sbin/nologin onesait 

# Install mongodb tools and node-npm
RUN apk add --no-cache mongodb-tools && \
	apk add --update nodejs nodejs-npm

# Install elasticdump globally
RUN npm install elasticdump -g	

RUN chown -R onesait:onesait /var/log/platform-logs && \
	chown -R onesait:onesait /tmp/export && \
	chown -R onesait:onesait /usr/local/files && \
    chown -R onesait:onesait ./target && \
    chown onesait:onesait app.jar && \
    chmod -R 777 ./target && \
    chmod -R 777 /var/log && \
    chmod -R 777 /tmp/export && \
    chmod -R 777 /usr/local/files
    
VOLUME ["/tmp", "/var/log/platform-logs", "/usr/local/files"]
    
USER onesait

ENTRYPOINT java $JAVA_OPTS -Dspring.application.json=$ONESAIT_PROPERTIES -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=docker -jar /app.jar