FROM frolvlad/alpine-oraclejdk8:slim

# Metadata
LABEL module.maintainer "onesaitplatform@indra.es" \
      module.name="quasar30"

# Create quasar directory
RUN mkdir -p /usr/local/onesait/quasar/plugins

# Create the quasar user and group
RUN addgroup -S quasar -g 433 && adduser -u 431 -S -g quasar -h /usr/local/onesait -s /sbin/nologin quasar

# Copy quasar plugin and config
COPY quasar-mongodb* /usr/local/onesait/quasar/plugins
COPY quasar-web-assembly-30.0.0.jar /usr/local/onesait/quasar

# Copy quasar configuration files
COPY mongo.plugin /usr/local/onesait/quasar/plugins
COPY config.json /usr/local/onesait/quasar
COPY log4j.properties /usr/local/onesait/quasar
COPY docker-entrypoint.sh /usr/local/bin/

RUN chown -R quasar:quasar /usr/local/onesait/quasar/plugins && \
    chown -R quasar:quasar /usr/local/onesait/quasar && \
    chmod 751 /usr/local/onesait/quasar/plugins && \
    chmod 751 /usr/local/onesait/quasar && \
    chmod 644 /usr/local/onesait/quasar/config.json && \
    chmod 644 /usr/local/onesait/quasar/plugins/mongo.plugin && \
    chmod -R 644 /usr/local/onesait/quasar/quasar-* && \
    chmod -R 644 /usr/local/onesait/quasar/plugins/quasar-* && \
    chmod +x /usr/local/bin/docker-entrypoint.sh
    
WORKDIR /usr/local/onesait/quasar

ENV JAVA_OPTS="$JAVA_OPTS -Xms1G -Xmx3G" \
	REALTIMEDB=realtimedb \
	REALTIMEDBPORT=27017 \
	AUTHPARAMS="" \
	AUTHDB="" \
	TIMEOUTMS=5000
	
EXPOSE 10800

USER quasar
ENTRYPOINT ["docker-entrypoint.sh"]
