FROM maven:3.5.3-jdk-8-alpine

# metadata
LABEL module.maintainer="onesaitplatform@indra.es" \
	  module.name="controlpanel"	

ADD *-exec.jar app.jar

# web projects folder & logs folder
RUN mkdir -p /usr/local/webprojects && \
	mkdir -p /usr/local/themes && \
	mkdir -p /var/log/platform-logs && \
	mkdir ./target

# create sofia user/group
RUN addgroup -S onesait -g 433 && adduser -u 431 -S -g onesait -h /usr/local -s /sbin/nologin onesait 

# Install git
RUN apk update && apk upgrade && \
    apk add --no-cache bash git openssh	

RUN chown -R onesait:onesait /usr/local && \
    chown -R onesait:onesait /usr/share/maven && \
    chown -R onesait:onesait /var/log/platform-logs && \
    chown -R onesait:onesait ./target && \    
    chown onesait:onesait app.jar && \
    chmod -R 777 ./target && \
    chmod -R 777 /var/log && \
    chmod -R 777 /usr/local
    
# libc6-compat package for GNU libc compatibility
# needed for execute ocp binary
ENV BOWTIE2_VERSION 2.2.8

RUN apk add --no-cache \
        perl \
        wget \
        openssl \
        ca-certificates \
        libc6-compat \
        libstdc++ \
    && wget https://downloads.sourceforge.net/project/bowtie-bio/bowtie2/$BOWTIE2_VERSION/bowtie2-$BOWTIE2_VERSION-linux-x86_64.zip \
    && unzip -d /usr/local bowtie2-$BOWTIE2_VERSION-linux-x86_64.zip \
    && rm bowtie2-$BOWTIE2_VERSION-linux-x86_64.zip
    
VOLUME ["/tmp", "/usr/local/webprojects", "/var/log/platform-logs", "/usr/local/themes"]

# Rancher CLI    
COPY rancher /bin
COPY oc /bin
    
USER onesait

EXPOSE 18000 5701

ENV JAVA_OPTS="$JAVA_OPTS -Xms1G -Xmx3G" \
    SERVER_NAME=localhost \
    KAFKAENABLED=false \
    KAFKAHOST=kafka \
    KAFKAPORT=9095 \    
    REALTIMEDBSERVERS=realtimedb:27017 \
    REALTIMEDBAUTHDB=admin \    
	REALTIMEDBUSEAUTH=true \
	REALTIMEDBUSER=platformadmin \
	REALTIMEDBPASS=0pen-platf0rm-2018! \     
    REALTIMEDBWRITECONCERN=UNACKNOWLEDGED \	
    CONFIGDBHOST=configdb \
    CONFIGDBPORT=3306 \
    SCHEDULERDBHOST=schedulerdb \
    SCHEDULERDBPORT=3306 \    
    ELASTICDBHOST=elasticdb \
    ELASTICDBPORT=9200 \    
    QUASARHOST=quasar \
    QUASARPORT=10800 \
    BURROWHOST=burrow \
    BURROWPORT=18400
    
ENTRYPOINT java $JAVA_OPTS -Dspring.application.json=$ONESAIT_PROPERTIES -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=docker -Dloader.path=file:/usr/local/themes/ -jar /app.jar
