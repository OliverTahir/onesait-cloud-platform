FROM maven:3.5.3-jdk-8-alpine

# metadata
LABEL module.maintainer="onesaitplatform@indra.es" \
	  module.name="cache-server"	

ADD *-exec.jar app.jar

# logs folder
RUN mkdir -p /var/log/platform-logs && \
	mkdir ./target

# create sofia user/group
RUN addgroup -S onesait -g 433 && adduser -u 431 -S -g onesait -h /usr/local -s /sbin/nologin onesait 	

RUN chown -R onesait:onesait /usr/local && \
    chown -R onesait:onesait /var/log/platform-logs && \
    chown -R onesait:onesait ./target && \    
    chown onesait:onesait app.jar && \
    chmod -R 777 ./target && \
    chmod -R 777 /var/log && \
    chmod -R 777 /usr/local
    
VOLUME ["/tmp", "/var/log/platform-logs"]
    
USER onesait

EXPOSE 20001 5701

ENV JAVA_OPTS="$JAVA_OPTS -Xms1G -Xmx3G" \
    SERVER_NAME=localhost

ENTRYPOINT java $JAVA_OPTS -Dspring.application.json=$ONESAIT_PROPERTIES -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=docker -jar /app.jar
