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

prepareVolumes()
{
	echo "${yellow}[Generate directories to map docker volumes.............. Step 1]${reset}"	
	mkdir -p ${DATADISK}/registry/htpasswd
	if [ $? -eq 0 ]; then
		echo "${green}htpasswd directory created...................... [OK]${reset}"
	else
		echo "${red}htpasswd directory created...................... [KO]${reset}"
	fi
	
	mkdir -p ${DATADISK}/registry/images
	if [ $? -eq 0 ]; then
		echo "${green}images directory created...................... [OK]${reset}"
	else
		echo "${red}images directory created...................... [KO]${reset}"
	fi	
	
	mkdir -p ${DATADISK}/registry/nginx
	if [ $? -eq 0 ]; then
		echo "${green}nginx directory created...................... [OK]${reset}"
	else
		echo "${red}nginx directory created...................... [KO]${reset}"
	fi		
	
	mkdir -p ${DATADISK}/registry/certs
	if [ $? -eq 0 ]; then
		echo "${green}certs directory created...................... [OK]${reset}"
	else	
		echo "${red}certs directory created...................... [KO]${reset}"
	fi			
	
}

givePermissions()
{	
	echo "${yellow}[Give permissions to existing folders.................... Step 2]${reset}"
	chmod -R 755 ${DATADISK}/registry/htpasswd
	if [ $? -eq 0 ]; then
		echo "${green}permissions given to htpasswd directory...................... [OK]${reset}"
	else	
		echo "${red}permissions given to htpasswd directory...................... [KO]${reset}"
	fi	
		
	chmod -R 755 ${DATADISK}/registry/images
	if [ $? -eq 0 ]; then
		echo "${green}permissions given to images directory...................... [OK]${reset}"
	else	
		echo "${red}permissions given to images directory...................... [KO]${reset}"
	fi	
		
	chmod -R 755 ${DATADISK}/registry/nginx
	if [ $? -eq 0 ]; then
		echo "${green}permissions given to nginx directory...................... [OK]${reset}"
	else	
		echo "${red}permissions given to nginx directory...................... [KO]${reset}"
	fi		
	
	chmod -R 755 ${DATADISK}/registry/certs	
	if [ $? -eq 0 ]; then
		echo "${green}permissions given to certs directory...................... [OK]${reset}"
	else	
		echo "${red}permissions given to certs directory...................... [KO]${reset}"
	fi			
}

generateCerts()
{	
	echo "${yellow}[Generate self signed certificates....................... Step 3]${reset}"
	openssl req \
    -new \
    -newkey rsa:4096 \
    -days 365 \
    -nodes \
    -x509 \
    -subj "/C=ES/ST=Minsait/L=Madrid/O=Indra/CN="$COMMONNAME \
    -keyout domain.key \
    -out domain.crt	
    
	if [ $? -eq 0 ]; then
		echo "${green}Self signed certs generated...................... [OK]${reset}"
	else
		echo "${red}Self signed certs generated...................... [KO]${reset}"	
	fi	    
}

copyCerts()
{
	echo "${yellow}[Copy key and crt files to nginx folder.................. Step 4]${reset}"
	if [ -f domain.key ]; then
		mv domain.key ${DATADISK}/registry/certs/
		if [ $? -eq 0 ]; then
			echo "${green}key moved to certs folder...................... [OK]${reset}"
		else
			echo "${red}key moved to certs folder...................... [KO]${reset}"	
		fi			
	fi  
	
	if [ -f domain.crt ]; then
		mv domain.crt ${DATADISK}/registry/certs/
		if [ $? -eq 0 ]; then
			echo "${green}certificate moved to certs folder...................... [OK]${reset}"
		else
			echo "${red}certificate moved to certs folder...................... [KO]${reset}"	
		fi			
	fi 	 
}

generateHtpasswd()
{
	echo "${yellow}[Generate htpasswd file with user and pass.................. Step 5]${reset}"
	docker run --entrypoint htpasswd registry:2 -Bbn ${REGISTRYUSER} ${REGISTRYPASS} > ${DATADISK}/registry/htpasswd/.htpasswd
	if [ $? -eq 0 ]; then
		echo "${green}htpasswd file generated...................... [OK]${reset}"
	else
		echo "${red}htpasswd file generated...................... [KO]${reset}"	
	fi		
}

copyNginxConf()
{
	echo "${yellow}[Copy nginx configuration file.................. Step 6]${reset}"
	cp nginx.conf ${DATADISK}/registry/nginx
	if [ $? -eq 0 ]; then
		echo "${green}nginx configuration file copied...................... [OK]${reset}"
	else
		echo "${red}nginx configuration file copied...................... [KO]${reset}"	
	fi		
}

red=`tput setaf 1`
green=`tput setaf 2`
yellow=`tput setaf 3`
reset=`tput sgr0`

# Load configuration file
source .env

prepareVolumes
givePermissions
generateCerts
generateHtpasswd
copyCerts
copyNginxConf

exit 0