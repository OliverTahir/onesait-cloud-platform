FROM apache/zeppelin:0.8.0

# Metadata
LABEL module.maintainer "onesaitplatform@indra.es" \
      module.name="notebooks"

# Create the notebook user and group
RUN groupadd -r notebook -g 433 && useradd -u 431 -r -g notebook -d /opt/notebook -s /sbin/nologin -c "Notebook user" notebook

# OCP anonymous user
RUN chgrp root /etc/passwd && chmod ug+rw /etc/passwd

COPY shiro.ini ${Z_HOME}/conf/
COPY zeppelin-site.xml ${Z_HOME}/conf/
COPY *.jar ${Z_HOME}/interpreter/onesaitplatform/  
COPY zeppelin.sh ${Z_HOME}/bin/    

#RUN chmod -R 777 ${Z_HOME} && \
#    chown -R notebook:notebook ${Z_HOME}

RUN echo "$LOG_TAG Download Spark binary" && \
    wget -O /tmp/spark-2.3.1-bin-hadoop2.7.tgz http://apache.panu.it/spark/spark-2.3.1/spark-2.3.1-bin-hadoop2.7.tgz && \
    tar -zxvf /tmp/spark-2.3.1-bin-hadoop2.7.tgz && \
    rm -rf /tmp/spark-2.3.1-bin-hadoop2.7.tgz && \
    mv spark-2.3.1-bin-hadoop2.7 /spark-2.3.1-bin-hadoop2.7
	
ENV SPARK_HOME=/spark-2.3.1-bin-hadoop2.7
    
RUN apt-get -y update && \
	apt-get install -y libmysqlclient-dev    
	
RUN pip install --upgrade pip && \
	pip install selenium && \
	pip install bs4 && \
	pip install scipy && \
	pip install pandas && \
	pip install scikit-learn && \
	pip install scrapy && \
	pip install pattern && \
	pip install seaborn && \
	pip install bokeh && \
#	pip install basemap && \
	pip install networkX && \
	pip install NLTK && \
	pip install spacy && \
	pip install gensim	    

# USER notebook