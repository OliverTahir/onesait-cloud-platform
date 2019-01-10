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
	mkdir -p /${DATADISK}/onesaitplatform/schedulerdb
	if [ $? -eq 0 ]; then
		echo "${green}schedulerdb directory created...................... [OK]${reset}"
	else
		echo "${red}schedulerdb directory created...................... [KO]${reset}"
	fi
	
	mkdir -p /${DATADISK}/onesaitplatform/configdb
	if [ $? -eq 0 ]; then
		echo "${green}configdb directory created...................... [OK]${reset}"
	else
		echo "${red}configdb directory created...................... [KO]${reset}"
	fi	
	
	mkdir -p /${DATADISK}/onesaitplatform/realtimedb
	if [ $? -eq 0 ]; then
		echo "${green}realtimedb directory created...................... [OK]${reset}"
	else
		echo "${red}realtimedb directory created...................... [KO]${reset}"
	fi		
	
	mkdir -p /${DATADISK}/onesaitplatform/elasticdb	
	if [ $? -eq 0 ]; then
		echo "${green}elasticdb directory created...................... [OK]${reset}"
	else	
		echo "${red}elasticdb directory created...................... [KO]${reset}"
	fi	
	
	mkdir -p /${DATADISK}/onesaitplatform/platform-logs
	if [ $? -eq 0 ]; then
		echo "${green}platform-logs directory created...................... [OK]${reset}"
	else
		echo "${red}platform-logs directory created...................... [KO]${reset}"	
	fi		
	
	mkdir -p /${DATADISK}/onesaitplatform/zeppelin/interpreter
	if [ $? -eq 0 ]; then
		echo "${green}zeppelin/interpreter directory created...................... [OK]${reset}"
	else
		echo "${red}zeppelin/interpreter directory created...................... [KO]${reset}"	
	fi		
	
	mkdir -p /${DATADISK}/onesaitplatform/zeppelin/notebook
	if [ $? -eq 0 ]; then
		echo "${green}zeppelin/notebook directory created...................... [OK]${reset}"
	else	
		echo "${red}zeppelin/notebook directory created...................... [KO]${reset}"
	fi		
	
	mkdir -p /${DATADISK}/onesaitplatform/zeppelin/conf
	if [ $? -eq 0 ]; then
		echo "${green}zeppelin/conf directory created...................... [OK]${reset}"
	else	
		echo "${red}zeppelin/conf directory created...................... [KO]${reset}"
	fi		
	
	mkdir -p /${DATADISK}/onesaitplatform/webprojects
	if [ $? -eq 0 ]; then
		echo "${green}webprojects directory created...................... [OK]${reset}"
	else	
		echo "${red}webprojects directory created...................... [KO]${reset}"
	fi	
		
	mkdir -p /${DATADISK}/onesaitplatform/nginx
	if [ $? -eq 0 ]; then
		echo "${green}nginx directory created...................... [OK]${reset}"
	else
		echo "${red}nginx directory created...................... [KO]${reset}"	
	fi		
	
	mkdir -p /${DATADISK}/onesaitplatform/kafka-logs
	if [ $? -eq 0 ]; then
		echo "${green}kafka-logs directory created...................... [OK]${reset}"
	else	
		echo "${red}kafka-logs directory created...................... [KO]${reset}"
	fi		
	
	mkdir -p /${DATADISK}/onesaitplatform/flowengine
	if [ $? -eq 0 ]; then
		echo "${green}flowengine directory created...................... [OK]${reset}"
	else	
		echo "${red}flowengine directory created...................... [KO]${reset}"
	fi
	
	mkdir -p /${DATADISK}/onesaitplatform/rancherdata
	if [ $? -eq 0 ]; then
		echo "${green}rancherdata directory created...................... [OK]${reset}"
	else	
		echo "${red}rancherdata directory created...................... [KO]${reset}"
	fi				
	
}

givePermissions()
{	
	echo "${yellow}[Give permissions to existing folders.................... Step 2]${reset}"
	chmod -R 777 /${DATADISK}/onesaitplatform/elasticdb
	if [ $? -eq 0 ]; then
		echo "${green}permissions given to elasticdb directory...................... [OK]${reset}"
	else	
		echo "${red}permissions given to elasticdb directory...................... [KO]${reset}"
	fi	
		
	chmod -R 777 /${DATADISK}/onesaitplatform/flowengine
	if [ $? -eq 0 ]; then
		echo "${green}permissions given to flowengine directory...................... [OK]${reset}"
	else	
		echo "${red}permissions given to flowengine directory...................... [KO]${reset}"
	fi	
		
	chmod -R 777 /${DATADISK}/onesaitplatform/webprojects
	if [ $? -eq 0 ]; then
		echo "${green}permissions given to webprojects directory...................... [OK]${reset}"
	else	
		echo "${red}permissions given to webprojects directory...................... [KO]${reset}"
	fi		
	
	chmod -R 777 /${DATADISK}/onesaitplatform/platform-logs	
	if [ $? -eq 0 ]; then
		echo "${green}permissions given to platform-logs directory...................... [OK]${reset}"
	else	
		echo "${red}permissions given to platform-logs directory...................... [KO]${reset}"
	fi	
	
	chmod -R 777 /${DATADISK}/onesaitplatform/realtimedb	
	if [ $? -eq 0 ]; then
		echo "${green}permissions given to realtimedb directory...................... [OK]${reset}"
	else	
		echo "${red}permissions given to realtimedb directory...................... [KO]${reset}"
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
    -keyout server.key \
    -out platform.crt	
    
	if [ $? -eq 0 ]; then
		echo "${green}Self signed certs generated...................... [OK]${reset}"
	else
		echo "${red}Self signed certs generated...................... [KO]${reset}"	
	fi	    
}

copyCerts()
{
	echo "${yellow}[Copy key and crt files to nginx folder.................. Step 4]${reset}"
	if [ -f server.key ]; then
		mv server.key /${DATADISK}/onesaitplatform/nginx/
		if [ $? -eq 0 ]; then
			echo "${green}key moved to nginx folder...................... [OK]${reset}"
		else
			echo "${red}key moved to nginx folder...................... [KO]${reset}"	
		fi			
	fi  
	
	if [ -f platform.crt ]; then
		mv platform.crt /${DATADISK}/onesaitplatform/nginx/
		if [ $? -eq 0 ]; then
			echo "${green}certificate moved to nginx folder...................... [OK]${reset}"
		else
			echo "${red}certificate moved to nginx folder...................... [KO]${reset}"	
		fi			
	fi 	 
}

install-nmon()
{
	yum -y install nmon
	if [ $? -eq 0 ]; then
		echo "${green}installing nmon perfomance monitor...................... [OK]${reset}"
	else
		echo "${red}installing nmon perfomance monitor...................... [KO]${reset}"	
	fi	
	
	mkdir /root/monitor	
	if [ $? -eq 0 ]; then
		echo "${green}creating nmon directory folder...................... [OK]${reset}"
	else
		echo "${red}creating nmon directory folder...................... [KO]${reset}"	
	fi	
	
	sudo touch /root/monitor/monitor.sh
	if [ $? -eq 0 ]; then
		echo "${green}creating nmon crontab script file...................... [OK]${reset}"
	else
		echo "${red}creating nmon crontab script file...................... [KO]${reset}"	
	fi	
	
	printf "#!/bin/bash\nHOST=$(uname -n)\nDATE=$(date +%d-%h-%y)\n/usr/local/bin/nmon -F /var/log/nmon/$HOST"-"$DATE.nmon -T -s 60 -c 1440"	>> /root/monitor/monitor.sh
	if [ $? -eq 0 ]; then
		echo "${green}creating nmon crontab script...................... [OK]${reset}"
	else
		echo "${red}creating nmon crontab script...................... [KO]${reset}"	
	fi		

	chmod u+x /root/monitor/monitor.sh
	if [ $? -eq 0 ]; then
		echo "${green}changing permission to monitor.sh...................... [OK]${reset}"
	else
		echo "${red}changing permission to monitor.sh...................... [KO]${reset}"	
	fi		
	
	mkdir /var/log/nmon/
	if [ $? -eq 0 ]; then
		echo "${green}creating log dir for nmon...................... [OK]${reset}"
	else
		echo "${red}creating log dir for nmon...................... [KO]${reset}"	
	fi		
			
}

red=`tput setaf 1`
green=`tput setaf 2`
yellow=`tput setaf 3`
reset=`tput sgr0`

# Load configuration file
source config.properties

prepareVolumes
givePermissions
generateCerts
copyCerts

exit 0