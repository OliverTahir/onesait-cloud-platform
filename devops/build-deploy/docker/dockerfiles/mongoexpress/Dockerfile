FROM mongo-express:latest

# Metadata
LABEL module.maintainer "onesaitplatform@indra.es" \
      module.name="mongoexpress"

# create onesait user/group
RUN groupadd -r onesait -g 433 && useradd -u 431 -r -g onesait -d /opt/onesait -s /sbin/nologin -c "Onesait user" onesait
      
ENV ME_CONFIG_MONGODB_SERVER=realtimedb \
	ME_CONFIG_MONGODB_PORT=27017 \
	ME_CONFIG_SITE_BASEURL=/mongo \
	ME_CONFIG_BASICAUTH_USERNAME=onesait \
	ME_CONFIG_BASICAUTH_PASSWORD=0nesa1t-2018-!

USER onesait
	      
EXPOSE 8081      