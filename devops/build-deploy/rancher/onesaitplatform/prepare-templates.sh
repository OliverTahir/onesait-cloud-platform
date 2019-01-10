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
clear

echo "##########################################################################################"
echo "#                                                                                        #"
echo "#   _____             _                                                                  #"              
echo "#  |  __ \           | |                                                                 #"            
echo "#  | |  | | ___   ___| | _____ _ __                                                      #"
echo "#  | |  | |/ _ \ / __| |/ / _ \ '__|                                                     #"
echo "#  | |__| | (_) | (__|   <  __/ |                                                        #"
echo "#  |_____/ \___/ \___|_|\_\___|_|                                                        #"                
echo "#                                                                                        #"
echo "# Sofia2 Rancher templates generation                                                    #"
echo "# arg1 (obl) --> WORKER2DEPLOY --> Tag host name to deploy                               #"
echo "# arg2 (obl) --> DOMAIN_NAME --> Domain host name                                        #"
echo "# arg3 (obl) --> IMAGE_TAG --> Image Tag version                                         #"
echo "#                                                                                        #"
echo "##########################################################################################"

source config.properties
echo "** Rancher node tag to deploy --> "$WORKER2DEPLOY
echo "** Domain host name --> "$DOMAIN_NAME
echo "** Docker image tag to deploy --> "$IMAGE_TAG

if [ -z "$WORKER2DEPLOY" ]; then
	echo "Tag host name needed!!"
	exit 1
fi

if [ -z "$DOMAIN_NAME" ]; then
	echo "Domain host name needed!!"
	exit 1
fi

if [ -z "$IMAGE_TAG" ]; then
	echo "Image tag needed!!"
	exit 1
fi

homepath=$PWD
echo "#############"
echo "###############"
echo "################ Environment variables substitution..."	
echo "#################"
echo "##################"

rm -rf $PROJECTNAME-$WORKER2DEPLOY
mkdir $PROJECTNAME-$WORKER2DEPLOY

sed -e 's/${WORKER2DEPLOY}/'"$WORKER2DEPLOY"'/g' -e 's/${DOMAIN_NAME}/'"$DOMAIN_NAME"'/g' -e 's/${IMAGE_TAG}/'"$IMAGE_TAG"'/g' docker-compose.yml > $PROJECTNAME-$WORKER2DEPLOY/docker-compose-$WORKER2DEPLOY.yml 
sed -e 's/${WORKER2DEPLOY}/'"$WORKER2DEPLOY"'/g' -e 's/${DOMAIN_NAME}/'"$DOMAIN_NAME"'/g' -e 's/${IMAGE_TAG}/'"$IMAGE_TAG"'/g' rancher-compose.yml > $PROJECTNAME-$WORKER2DEPLOY/rancher-compose-$WORKER2DEPLOY.yml 

echo "################### Rancher deployment files successfully generated!"
echo "####################"
echo "#####################"

exit 0