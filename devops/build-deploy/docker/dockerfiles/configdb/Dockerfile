FROM mysql/mysql-server:5.7

# Metadata
LABEL module.maintainer="onesaitplatform@indra.es" \
	  module.name="configdb"	

# MySQL environment
ENV MYSQL_ROOT_PASSWORD=changeIt! \
 	MYSQL_ROOT_HOST=% \
 	MYSQL_USER=indra \ 
 	MYSQL_PASSWORD=onesait2018! \
	MYSQL_DATABASE=onesaitplatform_config 

# Ignore case
RUN echo "lower_case_table_names = 1" >> /etc/my.cnf

# Allow large files
RUN echo "max_allowed_packet=536870912" >> /etc/my.cnf

# Allow large files
RUN echo "max_connections = 351" >> /etc/my.cnf

EXPOSE 3306
