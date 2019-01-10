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

# Load configuration file
source config.properties

homepath=$PWD

ocpLogin()
{
	echo "######### Realizando Login en la plataforma OCP con token suministrado"
	
	oc login $OCPINSTANCE --token=$TOKEN
	
	if [ $? -eq 0 ]; then
		echo "Login succeeded!...................... [OK]"
	else
		echo "Cannot login into the platform..................... [KO]"
		exit 1
	fi	
	
	echo "######### Login realizado con éxito"
}

ocpUseProject()
{
	echo "######### Se va a usar el proyecto: "$PROJECT
	oc project $PROJECT
}

ocpToolsMongoExpress()
{
	echo "######### POD Cliente web MongoDB"
	oc new-app $IMAGENAMESPACE/mongoexpress:$INFRA_TAG --name=mongoexpress
}

ocpDeleteToolsMongoExpress()
{
	echo "######### Borrado POD Cliente web MongoDB"
	oc delete all -l app=mongoexpress	
}	

ocpToolsConfigInit()
{
	echo "######### Se procede a realizar la carga de datos de la persistencia de la plataforma"
	oc process -f $homepath/modulestemplates/24-template-configinit.yml -p MODULE_TAG=$MODULE_TAG -p REPLICAS=$REPLICAS -p PROJECT=$PROJECT -p IMAGENAMESPACE=$IMAGENAMESPACE | oc create -f -	
}

ocpDeleteToolsConfigInit()
{
	echo "######### Borrado del POD de carga de la persistencia de la plataforma"
	oc delete all -l app=configinit
}

ocpWizardPersistenceDeploy()
{
	if [ "$PERSISTENT" = true ]; then
		echo "¿Desea desplegar configDB? y/n: "
		read confirmation
		
		if [ "$confirmation" == "y" ]; then	
			oc process -f $homepath/persistencetemplates/11-template-configdb-persistent.yml -p PERSISTENCE_TAG=$PERSISTENCE_TAG -p REPLICAS=$REPLICAS -p PROJECT=$PROJECT -p IMAGENAMESPACE=$IMAGENAMESPACE | oc create -f - 	
		fi		
	
		echo "¿Desea desplegar schedulerDB? y/n: "
		read confirmation
		
		if [ "$confirmation" == "y" ]; then	
			oc process -f $homepath/persistencetemplates/12-template-schedulerdb-persistent.yml -p PERSISTENCE_TAG=$PERSISTENCE_TAG -p REPLICAS=$REPLICAS -p PROJECT=$PROJECT -p IMAGENAMESPACE=$IMAGENAMESPACE | oc create -f - 
		fi
				
		echo "¿Desea desplegar realtimeDB? y/n: "
		read confirmation
		
		if [ "$confirmation" == "y" ]; then				
			oc process -f $homepath/persistencetemplates/13-template-realtimedb-persistent.yml -p PERSISTENCE_TAG_MONGODB=$PERSISTENCE_TAG_MONGODB -p REPLICAS=$REPLICAS -p PROJECT=$PROJECT -p IMAGENAMESPACE=$IMAGENAMESPACE | oc create -f - 
		fi
				
		echo "¿Desea desplegar elasticDB? y/n: "
		read confirmation
		
		if [ "$confirmation" == "y" ]; then			
			oc process -f $homepath/persistencetemplates/14-template-elasticdb-persistent.yml -p PERSISTENCE_TAG=$PERSISTENCE_TAG -p REPLICAS=$REPLICAS -p PROJECT=$PROJECT -p IMAGENAMESPACE=$IMAGENAMESPACE | oc create -f -		
		fi	
		
		echo "¿Desea desplegar Zookeeper? y/n: "
		read confirmation
		
		if [ "$confirmation" == "y" ]; then			
			oc process -f $homepath/persistencetemplates/15-template-zookeeper.yml -p PERSISTENCE_TAG=$PERSISTENCE_TAG -p REPLICAS=$REPLICAS -p PROJECT=$PROJECT -p IMAGENAMESPACE=$IMAGENAMESPACE | oc create -f -	
		fi	
		
		echo "¿Desea desplegar Kafka? y/n: "
		read confirmation
		
		if [ "$confirmation" == "y" ]; then			
			oc process -f $homepath/persistencetemplates/16-template-kafka.yml -p PERSISTENCE_TAG=$PERSISTENCE_TAG -p REPLICAS=$REPLICAS -p PROJECT=$PROJECT -p IMAGENAMESPACE=$IMAGENAMESPACE | oc create -f -	
		fi			 
	else
		echo "¿Desea desplegar configDB? y/n: "
		read confirmation
		
		if [ "$confirmation" == "y" ]; then						
			oc process -f $homepath/persistencetemplates/11-template-configdb.yml -p PERSISTENCE_TAG=$PERSISTENCE_TAG -p REPLICAS=$REPLICAS -p PROJECT=$PROJECT -p IMAGENAMESPACE=$IMAGENAMESPACE | oc create -f -  
		fi
		
		echo "¿Desea desplegar schedulerDB? y/n: "
		read confirmation
		
		if [ "$confirmation" == "y" ]; then					
			oc process -f $homepath/persistencetemplates/12-template-schedulerdb.yml -p PERSISTENCE_TAG=$PERSISTENCE_TAG -p REPLICAS=$REPLICAS -p PROJECT=$PROJECT -p IMAGENAMESPACE=$IMAGENAMESPACE | oc create -f -
		fi		
		
		echo "¿Desea desplegar realtimeDB? y/n: "
		read confirmation
		
		if [ "$confirmation" == "y" ]; then			
			oc process -f $homepath/persistencetemplates/13-template-realtimedb.yml -p PERSISTENCE_TAG_MONGODB=$PERSISTENCE_TAG_MONGODB -p REPLICAS=$REPLICAS -p PROJECT=$PROJECT -p IMAGENAMESPACE=$IMAGENAMESPACE | oc create -f -
		fi		
		
		echo "¿Desea desplegar elasticDB? y/n: "
		read confirmation
		
		if [ "$confirmation" == "y" ]; then			
			oc process -f $homepath/persistencetemplates/14-template-elasticdb.yml -p PERSISTENCE_TAG=$PERSISTENCE_TAG -p REPLICAS=$REPLICAS -p PROJECT=$PROJECT -p IMAGENAMESPACE=$IMAGENAMESPACE | oc create -f -	
		fi
		
		echo "¿Desea desplegar Zookeeper? y/n: "
		read confirmation
		
		if [ "$confirmation" == "y" ]; then			
			oc process -f $homepath/persistencetemplates/15-template-zookeeper.yml -p PERSISTENCE_TAG=$PERSISTENCE_TAG -p REPLICAS=$REPLICAS -p PROJECT=$PROJECT -p IMAGENAMESPACE=$IMAGENAMESPACE | oc create -f -	
		fi	
		
		echo "¿Desea desplegar Kafka? y/n: "
		read confirmation
		
		if [ "$confirmation" == "y" ]; then			
			oc process -f $homepath/persistencetemplates/16-template-kafka.yml -p PERSISTENCE_TAG=$PERSISTENCE_TAG -p REPLICAS=$REPLICAS -p PROJECT=$PROJECT -p IMAGENAMESPACE=$IMAGENAMESPACE | oc create -f -	
		fi				
	fi	
}

ocpWizardModuleDeploy()
{
	echo "¿Desea desplegar el motor de consultas? y/n: "
	read confirmation
	
	if [ "$confirmation" == "y" ]; then	
		oc process -f $homepath/modulestemplates/11-template-quasar.yml -p INFRA_TAG=$INFRA_TAG -p REPLICAS=$REPLICAS -p PROJECT=$PROJECT -p IMAGENAMESPACE=$IMAGENAMESPACE -p AUTHDB=$AUTHDB -p AUTHPARAMS=$AUTHPARAMS | oc create -f -
	fi
		
	echo "¿Desea desplegar el módulo Control Panel? y/n: "
	read confirmation
	
	if [ "$confirmation" == "y" ]; then	
		if [ "$PERSISTENT" = true ]; then
			oc process -f $homepath/modulestemplates/12-template-controlpanel-persistent.yml -p MODULE_TAG=$MODULE_TAG -p REPLICAS=$REPLICAS -p PROJECT=$PROJECT -p SERVER_NAME=$SERVER_NAME -p IMAGENAMESPACE=$IMAGENAMESPACE -p KAFKAENABLED=$KAFKAENABLED -p REALTIMEDBUSEAUTH=$REALTIMEDBUSEAUTH | oc create -f -			
		else
			oc process -f $homepath/modulestemplates/12-template-controlpanel.yml -p MODULE_TAG=$MODULE_TAG -p REPLICAS=$REPLICAS -p PROJECT=$PROJECT -p SERVER_NAME=$SERVER_NAME -p IMAGENAMESPACE=$IMAGENAMESPACE -p KAFKAENABLED=$KAFKAENABLED -p REALTIMEDBUSEAUTH=$REALTIMEDBUSEAUTH | oc create -f -	
		fi
	fi
	
	echo "¿Desea desplegar el módulo Cache Server? y/n: "
	read confirmation
	
	if [ "$confirmation" == "y" ]; then	
		oc process -f $homepath/modulestemplates/21-template-cacheserver.yml -p MODULE_TAG=$MODULE_TAG -p REPLICAS=$REPLICAS -p PROJECT=$PROJECT -p SERVER_NAME=$SERVER_NAME -p IMAGENAMESPACE=$IMAGENAMESPACE -p REALTIMEDBUSEAUTH=$REALTIMEDBUSEAUTH | oc create -f -
	fi
	
	echo "¿Desea desplegar el módulo Semantic Inf Broker? y/n: "
	read confirmation
	
	if [ "$confirmation" == "y" ]; then	
		oc process -f $homepath/modulestemplates/22-template-semanticinfbroker.yml -p MODULE_TAG=$MODULE_TAG -p REPLICAS=$REPLICAS -p PROJECT=$PROJECT -p SERVER_NAME=$SERVER_NAME -p IMAGENAMESPACE=$IMAGENAMESPACE -p REALTIMEDBUSEAUTH=$REALTIMEDBUSEAUTH | oc create -f -
	fi
	
	echo "¿Desea desplegar el módulo Oauth Server? y/n: "
	read confirmation
	
	if [ "$confirmation" == "y" ]; then	
		oc process -f $homepath/modulestemplates/23-template-oauthserver.yml -p MODULE_TAG=$MODULE_TAG -p REPLICAS=$REPLICAS -p PROJECT=$PROJECT -p SERVER_NAME=$SERVER_NAME -p IMAGENAMESPACE=$IMAGENAMESPACE -p REALTIMEDBUSEAUTH=$REALTIMEDBUSEAUTH | oc create -f - 
	fi
	
	
	echo "¿Desea desplegar el módulo API Manager? y/n: "
	read confirmation
	
	if [ "$confirmation" == "y" ]; then	
		oc process -f $homepath/modulestemplates/13-template-apimanager.yml -p MODULE_TAG=$MODULE_TAG -p REPLICAS=$REPLICAS -p PROJECT=$PROJECT -p SERVER_NAME=$SERVER_NAME -p IMAGENAMESPACE=$IMAGENAMESPACE -p REALTIMEDBUSEAUTH=$REALTIMEDBUSEAUTH | oc create -f -	
	fi	
	
	echo "¿Desea desplegar el módulo IoT Broker? y/n: "
	read confirmation
	
	if [ "$confirmation" == "y" ]; then	
		oc process -f $homepath/modulestemplates/14-template-iotbroker.yml -p MODULE_TAG=$MODULE_TAG -p REPLICAS=$REPLICAS -p PROJECT=$PROJECT -p SERVER_NAME=$SERVER_NAME -p IMAGENAMESPACE=$IMAGENAMESPACE -p KAFKAENABLED=$KAFKAENABLED -p REALTIMEDBUSEAUTH=$REALTIMEDBUSEAUTH | oc create -f -	
	fi	
	
	echo "¿Desea desplegar el módulo Device Simulator? y/n: "
	read confirmation
	
	if [ "$confirmation" == "y" ]; then		
		oc process -f $homepath/modulestemplates/15-template-devicesimulator.yml -p MODULE_TAG=$MODULE_TAG -p REPLICAS=$REPLICAS -p PROJECT=$PROJECT -p SERVER_NAME=$SERVER_NAME -p IMAGENAMESPACE=$IMAGENAMESPACE -p REALTIMEDBUSEAUTH=$REALTIMEDBUSEAUTH | oc create -f -	
	fi	
	
	echo "¿Desea desplegar el módulo Digital Twin? y/n: "
	read confirmation
	
	if [ "$confirmation" == "y" ]; then		
		oc process -f $homepath/modulestemplates/16-template-digitaltwin.yml -p MODULE_TAG=$MODULE_TAG -p REPLICAS=$REPLICAS -p PROJECT=$PROJECT -p SERVER_NAME=$SERVER_NAME -p IMAGENAMESPACE=$IMAGENAMESPACE -p REALTIMEDBUSEAUTH=$REALTIMEDBUSEAUTH | oc create -f -	
	fi	
	
	echo "¿Desea desplegar el módulo Flow Engine? y/n: "
	read confirmation		
	
	if [ "$confirmation" == "y" ]; then		
		if [ "$PERSISTENT" = true ]; then
			oc process -f $homepath/modulestemplates/17-template-flowengine-persistent.yml -p MODULE_TAG=$MODULE_TAG -p REPLICAS=$REPLICAS -p PROJECT=$PROJECT -p SERVER_NAME=$SERVER_NAME -p IMAGENAMESPACE=$IMAGENAMESPACE -p REALTIMEDBUSEAUTH=$REALTIMEDBUSEAUTH | oc create -f -			
		else
			oc process -f $homepath/modulestemplates/17-template-flowengine.yml -p MODULE_TAG=$MODULE_TAG -p REPLICAS=$REPLICAS -p PROJECT=$PROJECT -p SERVER_NAME=$SERVER_NAME -p IMAGENAMESPACE=$IMAGENAMESPACE -p REALTIMEDBUSEAUTH=$REALTIMEDBUSEAUTH | oc create -f -		
		fi	
	fi		
	
	echo "¿Desea desplegar el módulo Dashboard Engine? y/n: "
	read confirmation		
	
	if [ "$confirmation" == "y" ]; then		
		oc process -f $homepath/modulestemplates/18-template-dashboardengine.yml -p MODULE_TAG=$MODULE_TAG -p REPLICAS=$REPLICAS -p PROJECT=$PROJECT -p SERVER_NAME=$SERVER_NAME -p IMAGENAMESPACE=$IMAGENAMESPACE -p REALTIMEDBUSEAUTH=$REALTIMEDBUSEAUTH | oc create -f -	
	fi				
	
	echo "¿Desea desplegar el módulo Monitoring UI? y/n: "
	read confirmation		
	
	if [ "$confirmation" == "y" ]; then		
		oc process -f $homepath/modulestemplates/19-template-monitoringui.yml -p MODULE_TAG=$MODULE_TAG -p REPLICAS=$REPLICAS -p PROJECT=$PROJECT -p SERVER_NAME=$SERVER_NAME -p IMAGENAMESPACE=$IMAGENAMESPACE -p REALTIMEDBUSEAUTH=$REALTIMEDBUSEAUTH | oc create -f -	
	fi	
	
	echo "¿Desea desplegar el módulo Notebook? y/n: "
	read confirmation		
	
	if [ "$confirmation" == "y" ]; then		
		oc process -f $homepath/modulestemplates/25-template-notebook.yml -p MODULE_TAG=$MODULE_TAG -p REPLICAS=$REPLICAS -p PROJECT=$PROJECT -p SERVER_NAME=$SERVER_NAME -p IMAGENAMESPACE=$IMAGENAMESPACE | oc create -f -	
	fi	
	
	echo "¿Desea desplegar el módulo Load Balancer? y/n: "
	read confirmation		
	
	if [ "$confirmation" == "y" ]; then	
		if [ "$PERSISTENT" = true ]; then
			oc process -f $homepath/modulestemplates/20-template-loadbalancer-persistent.yml -p INFRA_TAG=$INFRA_TAG -p REPLICAS=$REPLICAS -p PROJECT=$PROJECT -p SERVER_NAME=$SERVER_NAME -p IMAGENAMESPACE=$IMAGENAMESPACE | oc create -f -			
		else
			oc process -f $homepath/modulestemplates/20-template-loadbalancer.yml -p INFRA_TAG=$INFRA_TAG -p REPLICAS=$REPLICAS -p PROJECT=$PROJECT -p SERVER_NAME=$SERVER_NAME -p IMAGENAMESPACE=$IMAGENAMESPACE | oc create -f -			
		fi	
	fi	
		
}

ocpDeletePersistence()
{
	echo "WARNING! se procederán a borrar los módulos de persistencia del proyecto: "$PROJECT
	echo "¿Desea continuar? y/n:"
	read confirmation
	
	if [ "$confirmation" == "y" ]; then
		echo "######### Comienzo del borrado de los recursos de persistencia"
		
		echo "¿Desea borrar Config DB? y/n: "
		read confirmation
		
		if [ "$confirmation" == "y" ]; then		
			echo "########################### 1 - Borrado de recursos de la Config DB"
			oc delete all -l app=configdb
		fi		
	
		echo "¿Desea borrar Scheduler DB? y/n: "
		read confirmation
		
		if [ "$confirmation" == "y" ]; then		
			echo "############################# 2 - Borrado de recursos de la Scheduler DB"
			oc delete all -l app=schedulerdb
		fi			

		echo "¿Desea borrar Real Time DB? y/n: "
		read confirmation
		
		if [ "$confirmation" == "y" ]; then		
			echo "############################### 3 - Borrado de recursos de la Real Time DB"
			oc delete all -l app=realtimedb
		fi		

		echo "¿Desea borrar Elastic Search DB? y/n: "
		read confirmation
		
		if [ "$confirmation" == "y" ]; then		
			echo "################################# 4 - Borrado de recursos de Elastic Search DB"
			oc delete all -l app=elasticdb
		fi			

		echo "¿Desea borrar Kafka? y/n: "
		read confirmation
		
		if [ "$confirmation" == "y" ]; then		
			echo "################################### 5 - Borrado de recursos de Kafka"
			oc delete all -l app=kafka	
		fi			

		echo "¿Desea borrar Zookeeper? y/n: "
		read confirmation
		
		if [ "$confirmation" == "y" ]; then		
			echo "##################################### 6 - Borrado de recursos de Zookeeper"
			oc delete all -l app=zookeeper
		fi							
		
		echo "######### Borrado realizado con éxito #########"
	fi	
}

ocpDeleteModules()
{
	echo "WARNING! se procederán a borrar los módulos del proyecto: "$PROJECT
	oc get deploymentconfigs
	echo "¿Desea continuar? y/n:"
	read confirmation
	
	if [ "$confirmation" == "y" ]; then
		echo "######### Comienzo del borrado de los recursos de los módulos onesait platform en OCP"
		
		echo "¿Desea borrar Load Balancer? y/n: "
		read confirmation
		
		if [ "$confirmation" == "y" ]; then		
			echo "##### 1 - Borrado de recursos del Load Balancer"
			oc delete all -l app=loadbalancerservice
		fi
					
		echo "¿Desea borrar Control Panel? y/n: "
		read confirmation
		
		if [ "$confirmation" == "y" ]; then		
			echo "######### 2 - Borrado de recursos del Control Panel"
			oc delete all -l app=controlpanelservice		
		fi		
				
		echo "¿Desea borrar API Manager? y/n: "
		read confirmation
		
		if [ "$confirmation" == "y" ]; then	
			echo "########### 3 - Borrado de recursos del API Manager"
			oc delete all -l app=apimanagerservice			
		fi		
		
		echo "¿Desea borrar IoTBroker? y/n: "
		read confirmation
		
		if [ "$confirmation" == "y" ]; then		
			echo "############# 4 - Borrado de recursos del IoTBroker"
			oc delete all -l app=iotbrokerservice
		fi		
				
		echo "¿Desea borrar Device Simulator? y/n: "
		read confirmation
		
		if [ "$confirmation" == "y" ]; then		
			echo "############### 5 - Borrado de recursos de Device Simulator"
			oc delete all -l app=devicesimulator
		fi		
		
		echo "¿Desea borrar Digital Twin? y/n: "
		read confirmation
		
		if [ "$confirmation" == "y" ]; then		
			echo "################# 6 - Borrado de recursos de Digital Twin"
			oc delete all -l app=digitaltwinbrokerservice		
		fi		
		
		echo "¿Desea borrar Flow Engine? y/n: "
		read confirmation
		
		if [ "$confirmation" == "y" ]; then	
			echo "################### 7 - Borrado de recursos de Flow Engine"
			oc delete all -l app=flowengineservice				
		fi		
		
		echo "¿Desea borrar Dashboard Engine? y/n: "
		read confirmation
		
		if [ "$confirmation" == "y" ]; then	
			echo "##################### 8 - Borrado de recursos del Dashboard Engine"
			oc delete all -l app=dashboardengineservice			
		fi		
		
		echo "¿Desea borrar Monitoring UI? y/n: "
		read confirmation
		
		if [ "$confirmation" == "y" ]; then	
			echo "####################### 9 - Borrado de recursos de Monitoring UI"
			oc delete all -l app=monitoringuiservice			
		fi		
							
		echo "¿Desea borrar Quasar? y/n: "
		read confirmation
		
		if [ "$confirmation" == "y" ]; then		
			echo "######################### 10 - Borrado de recursos de Quasar"
			oc delete all -l app=quasar		
		fi		

		echo "¿Desea borrar Cache? y/n: "
		read confirmation
		
		if [ "$confirmation" == "y" ]; then	
			echo "######################### 10 - Borrado de recursos de Cache"
			oc delete all -l app=cacheservice			
		fi

		echo "¿Desea borrar Router? y/n: "
		read confirmation
		
		if [ "$confirmation" == "y" ]; then	
			echo "######################### 10 - Borrado de recursos de Router"
			oc delete all -l app=routerservice			
		fi
		
		echo "¿Desea borrar Oauth Server? y/n: "
		read confirmation
		
		if [ "$confirmation" == "y" ]; then	
			echo "######################### 10 - Borrado de recursos de Oauth Server"
			oc delete all -l app=oauthserver			
		fi		
		
		echo "######### Borrado realizado con éxito #########"
	fi	
}

printMenu()
{
	echo "#####################################################################################"
	echo "#                                                                                   #"
	echo "# ¿Qué desea realizar?                                                              #"
	echo "# --------------------                                                              #"
	echo "#                                ==============                                     #"
	echo "#                                = DESPLIEGUE =                                     #"
	echo "#                                ==============                                     #"
	echo "#                                                                                   #"				
	echo "# Persistencia:                                                                     #"
	echo "# --------------------------                                                        #"		
	echo "# 1- Despliegue selectivo de los servicios de persistencia de onesait platform      #"
	echo "# 2- Despliegue POD de carga de base de datos                                       #"	
	echo "#                                                                                   #"	
	echo "# Módulos de la plataforma:                                                         #"
	echo "# --------------------------------------                                            #"		
	echo "# 3- Despliegue selectivo de módulos de onesait platform                            #"	
	echo "#                                                                                   #"	
	echo "# Herramientas:                                                                     #"
	echo "# --------------------------                                                        #"
	echo "# 4- Despliegue POD cliente web MongoDB -mongoexpress-                              #"			
	echo "#                                                                                   #"
	echo "#                                ==============                                     #"
	echo "#                                =  BORRADO   =                                     #"
	echo "#                                ==============                                     #"	
	echo "#                                                                                   #"		
	echo "# PERSISTENCIA/MÓDULOS/HERRAMIENTAS:                                                #"
	echo "# --------------------------------------------                                      #"			
	echo "# 5- Borrado selectivo de módulos de la plataforma                                  #"
	echo "# 6- Borrado selectivo de la persistencia de la plataforma                          #"
	echo "# 7- Borrado POD cliente web MongoDB                                                #"	
	echo "# 8- Borrado POD de carga de bases de datos                                         #"		
	echo "#                                                                                   #"	
	echo "# 9- SALIR                                                                          #"	
	echo "#                                                                                   #"			
	echo "#####################################################################################"
	echo "Introduzca opción: "	
}

clear
echo "#####################################################################################"
echo "# Se va a generar una instancia de onesait platform en Openshift Container Platform #"
echo "#                                                                                   #"
echo "# IMPORTANTE: Es necesario proporcionar el token de usuario de Openshift para poder #"
echo "# operar en la plataforma, para obtenerlo: oc whoami -t                             #"
echo "#                                                                                   #"
echo "#####################################################################################"
echo "¿Desea continuar? y/n: "

read confirmation

if [ "$confirmation" != "y" ]; then
	exit 1
fi

if [ -z "$TOKEN" ]; then
	exit 1
fi

echo "Login a la plataform con token: "$TOKEN
ocpLogin
ocpUseProject
echo  "Se va a desplegar la plataforma sobre el proyecto: "$PROJECT

while true; do
	printMenu
	read option
	case $option in
		1) 	ocpWizardPersistenceDeploy 
			;;
		2) 	ocpToolsConfigInit 
			;;			
		3)  ocpWizardModuleDeploy
		    ;;
		4)	ocpToolsMongoExpresS
			;;
		5)	ocpDeleteModules 
			;;		
		6)  ocpDeletePersistence	
			;;
		7) 	ocpDeleteToolsMongoExpress
			;;	
		8) 	ocpDeleteToolsConfigInit
			;;	
		9)	exit 0
			;;								
		*)	echo "¡La opción seleccionada es incorrecta!"
			clear
			;;
	esac
done

exit 0
