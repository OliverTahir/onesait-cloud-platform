# Instalation-resosurces

## crontab-tasks/clean-up-orphan-resources.sh

Este script sirve para eliminar los volúmenes huerfanos y las imágenes que se han quedado sin tag. La idea es incluir este script en el crotnab de la máquina que contenga los contenedores para liberar espacio a medida que las imagenes vayan cambiando de versión.

## enviroment-scripts/prepare-environment.sh

Este script prepara la máquina para la instalación de la plataforma. Esencialmente, esta preparación consiste en:

-Crear directorios en el host que se utilizaran para el mapeo de volumenes

-Dar permisos a estos directorios para que sean accesibles por los contenedores

-Genera certificados autofirmados (server.key & platform.crt)

-Copia los certificados al contenedor de balanceador de carga (nginx).

A parte, este script carga los siguientes paramátros desde config.properties:
	
-**DATADISK**: Aquí debemos escribir el directorio en el que esta montado el disco de nuestra máquina (persistencia de volumenes)
    
-**COMMONAME**: Aqui debemos escribir la URL de nuestra máquina. Este COMMONAME se utiliza a la hora de crear los certificados y asociarlos a nuestra máquina.
    
## exportimages-scripts/exportimport-images.sh

Este script guarda imagenes en formato .tar o dispone imágenes que estén en formato .tar para su uso. Para su configuración, es necesario editar el archivo config.properties del mismo directorio. Los parámetros configurables son:

-**IMAGENAMESPACE**: Nombre del proyecto asociado a las imagenes. Este parámetro es un directorio, debemos definirlos con una barra "/" al final.

-**DATAFOLDER**: Ruta del directorio en el que queremos guardar las imágenes .tar, o por el contrario el directorio de donde queremos importar los .tar (Esto depende de los siguientes dos parámetros)

-**SAVEIMAGES**: Marcar como true si lo que queremos hacer es un import de las imagenes, false si queremos exportar.

-**LOADIMAGES**: Marcar como true si lo que queremos hacer es un export de las imagenes, false si queremos importar.