FROM konradkleine/docker-registry-frontend:v2

# Metadata
LABEL module.maintainer="onesaitplatform@indra.es" \
	  module.name="registry-ui"
	  
ENV ENV_DOCKER_REGISTRY_HOST=moaf-nexus.westeurope.cloudapp.azure.com \
	ENV_DOCKER_REGISTRY_PORT=443 \
	ENV_DOCKER_REGISTRY_USE_SSL=1 \
	ENV_MODE_BROWSE_ONLY=false \
	ENV_DEFAULT_REPOSITORIES_PER_PAGE=50	  
	
# Ignore ssl verification	
COPY docker-site.conf /etc/apache2/sites-available/docker-site.conf

EXPOSE 25000	