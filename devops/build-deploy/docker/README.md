# Docker

En esta carpeta tenemos los recursos necesatios para lanzar la plataforma en local utilizando únicamente docker y docker-compose.

## data

En esta carpeta esta los .yml para los para desplegar los modulos de bases de datos.

**-docker-compose.yml:** Carga los contenedores de las bases de datos sin volumenes.

**-docker-compose.persistent.yml:** Carga los contenedores de las bases de datos asociandoles volumenes persistentes.

**-docker-compose.initdb.yml:** Carga el contenedor que poblara las bases de datos desplegadas anteriormente.

**-.env:** Aquí podremos definir los tags de los modulos anteriores, las rutas de los volumenes y el repositorio de donde queremos descargar las imagenes.

## dockerfiles

Aquí encontraremos los dockerfiles necesarios para crear las imagenes de cada módulo.

## modules

En esta carpeta tenemos el docker-compose.yml para desplegar el controlpanel de la plataforma. En el .env marcamos el tag de la imagen que queremos usar y la direccion del servidor donde lo queremos desplegar.

## scripts

Aquí se encuentra un script qué, además de generar las imagenes selectivamente, también nos permite subir las imagenes que queramos a un registry. También tenemmos la opción de decicdir si queremos utilizar nuestro registry privado "moaf-nexus.westeurope.cloudapp.azure.com:443" o el que tenemos en OpenShift "docker-registry-default.apps.openp.cwbyminsait.com"

## image-generation.sh & config.properties

Con las config.properties configuramos el proxy si fuera necesario y marcamos el nombre que llevaran delante por defecto las imagenes (onesaitplatform por defecto).

Este script generará todas las imagenes de las bases de datos y los módulos de la plataforma que no tengamos en nuestro repo local.


