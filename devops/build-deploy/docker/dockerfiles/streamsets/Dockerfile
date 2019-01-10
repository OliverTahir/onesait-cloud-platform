FROM streamsets/datacollector:3.3.0

# Metadata
LABEL module.maintainer "onesaitplatform@indra.es" \
      module.name="streamsets"

USER root

# create onesait user/group
RUN addgroup -S onesait -g 433 && adduser -u 431 -S -g onesait -h /usr/local -s /sbin/nologin onesait 

EXPOSE 18630

ADD sdc /etc/sdc
ADD onesaitplatform-streamsets /opt/streamsets-datacollector-user-libs/onesaitplatform-streamsets

RUN chown -R onesait:onesait /etc/sdc && \
    chmod -R 777 /etc/sdc && \
	chown -R onesait:onesait /logs && \
    chmod -R 777 /logs && \    
	chown -R onesait:onesait /data && \
    chmod -R 777 /data && \        
	chown -R onesait:onesait /opt/streamsets-datacollector-user-libs/onesaitplatform-streamsets && \
    chmod -R 777 /opt/streamsets-datacollector-user-libs/onesaitplatform-streamsets && \
	chown -R onesait:onesait /opt/streamsets-datacollector-3.3.0/streamsets-libs && \
    chmod -R 777 /opt/streamsets-datacollector-3.3.0/streamsets-libs

USER onesait

VOLUME ["sdc-stagelibs", "sdc-data"]
