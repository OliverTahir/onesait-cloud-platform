#!/bin/bash

#
# Copyright Indra Sistemas, S.A.
# 2013-2018 SPAIN
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#      http://www.apache.org/licenses/LICENSE-2.0
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# ------------------------------------------------------------------------

buildImage()
{
	echo "Docker image generation for onesaitplatform module: "$2
	cp $1/target/*-exec.jar $1/docker/
	docker build -t $USERNAME/$2:$3 .
	rm $1/docker/*.jar
}

buildConfigDB()
{
	echo "ConfigDB image generation with Docker CLI: "
	docker build -t $USERNAME/configdb:$1 .
}

buildSchedulerDB()
{
	echo "SchedulerDB image generation with Docker CLI: "
	docker build -t $USERNAME/schedulerdb:$1 .
}

buildRealTimeDB()
{
	echo "RealTimeDB image generation with Docker CLI: "
	docker build -t $USERNAME/realtimedb:$1 .
}

buildRealTimeDBNoAuth()
{		
	echo "RealTimeDB image generation with Docker CLI: "
	docker build -t $USERNAME/realtimedb:$1 -f Dockerfile.noauth .
}

buildElasticSearchDB()
{
	echo "ElasticSearchDB image generation with Docker CLI: "
	docker build -t $USERNAME/elasticdb:$1 .
}

buildNginx()
{
	echo "NGINX image generation with Docker CLI: "
	docker build -t $USERNAME/nginx:$1 .		
}

buildQuasar()
{
	echo "Quasar image generation with Docker CLI: "
	echo "Step 1: download quasar binary file"
	wget https://github.com/quasar-analytics/quasar/releases/download/v14.2.6-quasar-web/quasar-web-assembly-14.2.6.jar
	
	if [ $? -eq 0 ]; then
		echo "Quasar jar downloaded...................... [OK]"
	else
		echo "Quasar jar downloaded..................... [KO]"
	fi		
	
	echo "Step 2: build quasar image"
	docker build -t $USERNAME/quasar:$1 .	
	
	rm quasar-web-assembly*.jar
}

prepareConfigInitExamples()
{
	echo "Compressing and Copying realtimedb file examples: "
	cp -r $1/src/main/resources/examples/  $1/docker/examples
	if [ $? -eq 0 ]; then
		echo "examples folder compress & copied...................... [OK]"
	else
		echo "examples folder compress & copied..................... [KO]"
	fi	
}

removeConfigInitExamples()
{
	echo "Deleting realtimedb file examples"
	rm -rf $1/docker/examples
	if [ $? -eq 0 ]; then
		echo "examples folder deleted...................... [OK]"
	else
		echo "examples folder deleted..................... [KO]"
	fi		
}

buildPersistence()
{
	echo "++++++++++++++++++++ Persistence layer generation..."
	
	# Generates images only if they are not present in local docker registry
	if [[ "$(docker images -q $USERNAME/configdb 2> /dev/null)" == "" ]]; then
		cd $homepath/dockerfiles/configdb
		buildConfigDB latest
	fi
	
	if [[ "$(docker images -q $USERNAME/schedulerdb 2> /dev/null)" == "" ]]; then
		cd $homepath/dockerfiles/schedulerdb
		buildSchedulerDB latest
	fi
	
	if [[ "$(docker images -q $USERNAME/realtimedb:latest 2> /dev/null)" == "" ]]; then
		cd $homepath/dockerfiles/realtimedb
		buildRealTimeDB latest
	fi
		
	if [[ "$(docker images -q $USERNAME/realtimedb:latest-noauth 2> /dev/null)" == "" ]]; then
		cd $homepath/dockerfiles/realtimedb
		buildRealTimeDBNoAuth latest-noauth
	fi
	
	if [[ "$(docker images -q $USERNAME/elasticdb 2> /dev/null)" == "" ]]; then
		cd $homepath/dockerfiles/elasticsearch
		buildElasticSearchDB latest
	fi	
		
	if [[ "$(docker images -q $USERNAME/quasar 2> /dev/null)" == "" ]]; then
		cd $homepath/dockerfiles/quasar
		buildQuasar latest
	fi	
}


buildConfigInit() 
{
	echo "++++++++++++++++++++ Config init image generation..."
	
	if [[ "$(docker images -q $USERNAME/init 2> /dev/null)" == "" ]]; then
		cd $homepath/../../../sources/modules/config-init/docker
		
		prepareConfigInitExamples $homepath/../../../sources/modules/config-init
	
		buildImage $homepath/../../../sources/modules/config-init configinit latest
	
		removeConfigInitExamples $homepath/../../../sources/modules/config-init		
	fi		
}	

echo "##########################################################################################"
echo "#                                                                                        #"
echo "#   _____             _                                                                  #"              
echo "#  |  __ \           | |                                                                 #"            
echo "#  | |  | | ___   ___| | _____ _ __                                                      #"
echo "#  | |  | |/ _ \ / __| |/ / _ \ '__|                                                     #"
echo "#  | |__| | (_) | (__|   <  __/ |                                                        #"
echo "#  |_____/ \___/ \___|_|\_\___|_|                                                        #"                
echo "#                                                                                        #"
echo "# Docker Image generation                                                                #"
echo "# arg1 (opt) --> -1 if only want to create images for modules layer (skip persistence)   #"
echo "#                                                                                        #"
echo "##########################################################################################"

# Load configuration file
source config.properties

if [ "$PROXY_ON" = true ]; then
	echo "Setting corporate proxy configuration"
	export https_proxy=https://$PROXY_USER:$PROXY_PASS@$PROXY_HOST/
	export http_proxy=http://$PROXY_USER:$PROXY_PASS@$PROXY_HOST/
fi

homepath=$PWD

if [ -z "$1" ]; then
	# Generates images only if they are not present in local docker registry
	if [[ "$(docker images -q $USERNAME/controlpanel 2> /dev/null)" == "" ]]; then
		cd $homepath/../../../sources/modules/control-panel/docker
		buildImage $homepath/../../../sources/modules/control-panel controlpanel latest
	fi	
	
	if [[ "$(docker images -q $USERNAME/iotbroker 2> /dev/null)" == "" ]]; then
		cd $homepath/../../../sources/modules/iot-broker/docker	
		buildImage $homepath/../../../sources/modules/iot-broker iotbroker latest
	fi
	
	if [[ "$(docker images -q $USERNAME/apimanager 2> /dev/null)" == "" ]]; then	
		cd $homepath/../../../sources/modules/api-manager/docker
		buildImage $homepath/../../../sources/modules/api-manager apimanager latest
	fi
	
	if [[ "$(docker images -q $USERNAME/cacheserver 2> /dev/null)" == "" ]]; then	
		cd $homepath/../../../sources/modules/cache-server/docker
		buildImage $homepath/../../../sources/modules/cache-server cacheserver latest
	fi
	
	if [[ "$(docker images -q $USERNAME/router 2> /dev/null)" == "" ]]; then	
		cd $homepath/../../../sources/modules/semantic-inf-broker/docker
		buildImage $homepath/../../../sources/modules/semantic-inf-broker router latest
	fi			
	
	buildConfigInit			
	buildPersistence
fi

# Only creates config init image
if [ ! -z "$1" ]; then
	buildConfigInit
fi

echo "Docker images successfully generated!"

exit 0
