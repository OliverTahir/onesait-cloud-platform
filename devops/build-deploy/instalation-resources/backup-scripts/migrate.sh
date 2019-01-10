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

source config.properties
#export DOCKER_HOST=localhost:2375/

configdbBackupAndRestore()
{
	docker exec ${CONFIGDB} mysqldump --max_allowed_packet=512M -u ${SQLUSER} --password=${SQLPASS} onesaitplatform_config > "/tmp/onesaitplatform_config.sql"
	echo "`date +"%d-%m-%Y"T"%H:%M:%S"`.000+0000 Transferring configdb data.."
	docker cp /tmp/onesaitplatform_config.sql ${ENDING_CONFIGDB}:/tmp/
	echo "`date +"%d-%m-%Y"T"%H:%M:%S"`.000+0000 Done!"
	echo "`date +"%d-%m-%Y"T"%H:%M:%S"`.000+0000 Restoring..."
	docker exec ${ENDING_CONFIGDB} bash -c 'mysql -u root --password=changeIt! onesaitplatform_config < /tmp/onesaitplatform_config.sql'
	echo "`date +"%d-%m-%Y"T"%H:%M:%S"`.000+0000 Done!"
}

configdbBackup()
{
    docker exec ${CONFIGDB} mysqldump --max_allowed_packet=512M -u ${SQLUSER} --password=${SQLPASS} onesaitplatform_config > "/tmp/onesaitplatform_config.sql"
}

realtimedbBackupAndRestore()
{
	docker exec ${REALTIMEDB} mongodump --db onesaitplatform_rtdb --out /tmp
	echo "`date +"%d-%m-%Y"T"%H:%M:%S"`.000+0000 Transferring realtimedb data.."
	docker cp ${REALTIMEDB}:/tmp/onesaitplatform_rtdb /tmp/
	docker cp /tmp/onesaitplatform_rtdb ${ENDING_REALTIMEDB}:/tmp/
	echo "`date +"%d-%m-%Y"T"%H:%M:%S"`.000+0000 Done!"
	echo "`date +"%d-%m-%Y"T"%H:%M:%S"`.000+0000 Restoring..."
	docker exec ${ENDING_REALTIMEDB} mongorestore --db onesaitplatform_rtdb --drop /tmp/onesaitplatform_rtdb
	echo "`date +"%d-%m-%Y"T"%H:%M:%S"`.000+0000 Done!"

}

realtimedbBackup()
{
    docker exec ${REALTIMEDB} mongodump --db onesaitplatform_rtdb --out /tmp
    echo "`date +"%d-%m-%Y"T"%H:%M:%S"`.000+0000 Transferring realtimedb data.."
    docker cp ${REALTIMEDB}:/tmp/onesaitplatform_rtdb /tmp/
echo "`date +"%d-%m-%Y"T"%H:%M:%S"`.000+0000 Done!"

}

printMenu()
{
        echo "#####################################################################################"
        echo "#                                                                                   #"
        echo "# ¿Qué desea realizar?                                                              #"
        echo "# --------------------                                                              #"
        echo "#                                ===============                                    #"
        echo "#                                =   BACKUPS   =                                    #"
        echo "#                                ===============                                    #"
        echo "#                                                                                   #"
        echo "# Módulos:                                                                          #"
        echo "# --------------------------                                                        #"
        echo "# 1- Backup de la configdb y restauracion de la misma en un nuevo contenedor        #"
        echo "# 2- Backup de la realtimedb y restauración de la misma en un nuevo contenedor      #"
	echo "# 3- Backup de la configdb en local --> /tmp/onesaitplatform_config.sql             #"
	echo "# 4- Backup de la realtimedb en local --> /tmp/onesaitplatform_rtdb                 #"
        echo "#                                                                                   #"
        echo "# 9- SALIR                                                                          #"
        echo "#                                                                                   #"
        echo "#####################################################################################"
        echo "Introduzca opción: "
}

clear
echo "#####################################################################################"
echo "#            Script para hacer backups de BBDD y restauración de la misma           #"
echo "#####################################################################################"
echo "¿Desea continuar? y/n: "

read confirmation

if [ "$confirmation" != "y" ]; then
        exit 1
fi

while true; do
        printMenu
        read option
        case $option in
                1)      configdbBackupAndRestore
                        ;;
                2)      realtimedbBackupAndRestore
                        ;;
		3)      configdbBackup
                        ;;
                4)      realtimedbBackup
                        ;;
		9)      exit 0
                        ;;
                *)      echo "¡La opción seleccionada es incorrecta!"
                        clear
                        ;;
        esac
done

exit 0
