(Deployment) ¿Cómo desplegar onesait Cloud Platform en local con Docker y docker-compose?

Para levantar la plataforma en un entorno local, deberemos tener en cuenta los siguiente pre requisitos:

1- Tener instalado Docker en local, versión Community Edition:

https://docs.docker.com/install/

2- Tener instalado Docker compose en local:

https://docs.docker.com/compose/install/

Una vez tengamos instalado Docker, en entornos Windows o MacOS será necesario aumentar la memoria asignada al servicio de Docker,
recomendamos al menos 8GB de memoria y 2GB de swap.

<pantallazo de mac y windows>

- Paso 1: descargar los ficheros de docker-compose necesarios para levantar los servicios de la plataforma, estos se encuentran
en el repositorio de la plataforma en GitLab.

git clone https://onesait-git.cwbyminsait.com/onesait-platform/onesait-cloud-platform.git

- Paso 2: Nos situamos en el directorio donde se encuentra el docker-compose.yml encargado de levantar los servicios de bases de datos:

cd onesait-clod-platform/devops/build-deploy/docker/data

En este directorio veremos varios ficheros:

/.env  

Nos aseguramos que las claves tienen los siguientes valores:

REPOSITORY=registry.onesaitplatform.com/
PERSISTENCE_TAG=latest
MODULE_TAG=1.0.0-rc22
MONGO_TAG=latest-noauth

/docker-compose.yml: En el caso de que queramos levantar las bases de datos sin persistencia, ejecutaremos desde un terminal 
el siguiente comando en el mismo directorio donde se encuentra el fichero: 

> docker-compose up -d

/docker-compose.persistent.yml:  En el caso de que queramos persistir las bases de datos, en el fichero .env pondremos los directorios de nuestra máquina donde queramos alojar los datos,
por defecto no tienen valor asignado, como ejemplo podremos poner:

REALTIME_VOLUME=/Users/devopsuser/realtimedbdata
CONFIGDB_VOLUME=/Users/devopsuser/configdbdata
SCHEDULERDB_VOLUME=/Users/devopsuser/schedulerdbdata
ELASTICDB_VOLUME=/Users/devopsuser/elasticdbdata

Y desde el terminal ejecutaremos el comando:

> docker-compose -f docker-compose.persistent.yml up -d

Una vez levantadas las bases de datos podremos ver en que estado se encuentran con el siguiente comando:

> docker ps

Si queremos ver los logs de un contenedor de base de datos, ejecutaremos:

> docker logs <container_name> o > docker logs -f <container_name> (-f equivale a un tail)

- Paso 3: Poblar de datos las bases de datos, para ellos en este mismo directorio 
(onesait-clod-platform/devops/build-deploy/docker/data) hay otro docker-compose encargado de levantar el servicio de carga inicial de
datos, como anteriormente hemos hecho ejecutamos:

> docker-compose -f docker-compose.initdb.yml up

En este caso no incluímos el flag -d (detached mode) ya que el propio servicio se detiene una vez acabada su tarea.

- Paso 4: Levantamos los distintos módulos de la plataforma con docker-compose, por orden de arranque:

/router-cacheserver --> modúlo de enrutamiento, y servidor de caché
/control-panel --> consola web de la plataforma
/flowengine-iotbroker --> Motor de flujos y broker IoT de la plataforma
/api-manager --> módulo gestor de API Rest
/oauth-server --> servidor de autenticación

Cada directorio contiene:

fichero /.env con las siguientes variables de entorno: 

REPOSITORY=registry.onesaitplatform.com/ --> registro que contiene la imagen del servicio
SERVERNAME=localhost --> hostname de la máquina anfitriona
MODULE_TAG=1.0.0-rc22 --> Tag de la imagen del servicio

fichero docker-compose.yml, que contiene la descripción del servicio, mapeo de volúmenes, puertos, subredes, etc...

Para levantar cada uno de estos módulos es necesario, desde linea de comandos, posicionarse en el directorio del módulo y ejecutar el comando:

> docker-compose up -d

- Paso 5: Levantamos un contenedor con el servicio de NGINX, este lleva mapeado un volumen con el fichero de configuración necesario 
para redireccionar las peticiones que lleguen al control panel. (nginx.conf)

En el caso de levantar otros módulos se proporciona un fichero de nginx completo: nginx.conf.all

Ambos ficheros es necesario editarlos y sustituir la cadena ${SERVER_NAME} por el hostname de la máquina anfitriona de Docker. 
Se puede obtener desde linea de comando ejecutando el comando "hostname"



