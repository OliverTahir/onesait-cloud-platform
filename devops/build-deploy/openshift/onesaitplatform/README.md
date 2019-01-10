# Script para deplegar la plataforma en Openshift

## Antes de lanzar el script 

Lo primero que debemos hacer es instalar el cli de OpenShift en la máquina que vayamos a ejecutar el script. Para realizar el despliegue desde cero en OpenShift tenemos que solicitar al administrador del clúster que nos cree un proyecto. Sabremos que el nuevo proyecto estará creado y tendremos acceso a él si el proyecto aparece en la lista supeior derecha de la consola de openshift.

<p align="center">
    <img src='resources/images/onesait-platform-ocp-projects.png'/>
</p>

Una vez nuestro proyecto sea visible, debemos solicitar al admin del sistema un persistence volume (PV) y los claim persistence volumes asociados (PVC's). La configuración estándar para los PVC's es la siguiente.  

    pv + pvc-configdb --> 10GB (RWO)
    pv + pvc-elasticdb --> 100GB (RWO)
    pv + pvc-realtimedb --> 100GB (RWO)
    pv + pvc-schedulerdb --> 10GB (RWO)
    pv + pvc-flowengine --> 1GB (RWX)
    pv + pvc-webprojects --> 10GB (RWX)
    pv + pvc-logs-platform --> 100GB (RWX)
    pv + pvc-notebooks --> 5GB (RWX)
    pv + pvc-kafka-logs --> 100GB (RWX)
    
Una vez tengamos los PVC's operativos deberíamos de ver algo similar a:
    
<p align="center">
    <img src='resources/images/onesait-platform-ocp-storage.png'/>
</p>

También tenemos que comunicarle a administrador del sistema las quotes que nuestro proyecto va a utilizar. Típicamente lo recursos que se han de solicitar son:

    CPU: 12/8 Cores
    Memoria: 64/32 Gb
    
Desde la pestaña de Resources>Quota podremos ver si la configuración que hemos solicitado es la correcta.

<p align="center">
    <img src='resources/images/onesait-platform-ocp-quota.png'/>
</p>
    
Con esto ya tendremos un proyecto en OpenShift listo para desplegar nuestra plataforma.

## Configuración del config.properties

En primer lugar, obtenemos el token para autenticarnos. Para ello ejecutamos:

    oc login https://ocp.openp.cwbyminsait.com
    oc whoami -t
    
Lo que nos devolverá un token que nos permitirá utilizar los distintos comandos de OpenShift. Ahora es momento de adaptar las config.properties a nuestro proyecto.

-**OCPINSTANCE:** URL del clúster de OpenShift, "https://ocp.openp.cwbyminsait.com" hasta el momento.

-**TOKEN:** Token de acceso obtenido en el paso anterior.

-**PROJECT:** Nombre de nuestro proyecto.

-**IMAGENAMESPACE:** Nombre del registro del que vamos a bajar las imagenes docker. "onesait" para el registro en OpenShift y "onesaitplatform" para nuestro registro privado.

-**MODULE_TAG:** Tag para las imagenes de los modulos de la plataforma. (controlpanel, iotbroker, apimanager...)

-**INFRA_TAG:** Tag para las imagenes de la infrastructura de la plataforma. (zeppelin, streamsets, nginx, nodered...)

-**PERSISTENCE_TAG:** Tag para las imagenes de la capa de persistencia de la plataforma. (configdb, realtimedb, quasar...)

-**PERSISTENCE_TAG_MONGODB:** Tag para la imagen de mongodb. Este tag está separado del resto porque podemos añadirle "-noauth" al nombre del tag para que mongodb no requiera de autorizacion para logarnos, por el contrario si no ponemos "-noauth" mongodb requerirá authorización para poder logarnos.

-**SERVER_NAME:** URL asociada a nuestro proyecto, generada por OpenShift automáticamente. A no ser que se especifique lo contrario siempre tendrá el valor "<PROJECT>-onesait.apps.openp.cwbyminsait.com"

-**KAFKA_ENABLED:** True para habilitar Kafka en el despliegue, false para deshabilitarlo.

-**REALTIMEDBUSEAUTH:** True si queremos que mongodb requiera autenficación, false para lo contrario.

-**AUTHDB:** Dejar en blanco si mongodb no requiere autorización, fijar a "admin" en caso contrario.

-**AUTHPARAMS:** Dejar en blanco si mongodb no requiere autorización, fijar a "platformadmin:0pen-platf0rm-2018!@" en caso controario.

-**REPLICAS:** Numero de replicas con las que se lanzaran los pods. Es recomendable marcar este parámetro como cero y lanzar cada pod por separado desde la consola de OpenShift una vez se haya ejecutado el script.

-**PERSISTENT:** True para que los pods utilicen PVC's, false para lo contrario.

Una vez hayamos definido estos parámetros podremos lanzar el script "onesaitplatform.sh"

## Script onesaitplatform.sh

Con los config.properties ajustados para nuestro despliegue, ya estamos preparados para lanzar el script. Una vez ejecutado, nos recordará que es necesario introducir el token en el config.properties para que el script funcione.

<p align="center">
    <img src='resources/images/onesait-platform-ocp-advice.png'/>
</p>

Pulsamos "y" y aparece el siguiente cuadro de decisión.

<p align="center">
    <img src='resources/images/onesait-platform-ocp-script.png'/>
</p>

Este script nos permite:

-**1:** Nos permite un despliegue selectivo de los modulos de persistencia.

-**2:** Lanza el "configinit" que puebla las bases de datos.

-**3:** Nos permite desplegar uno a uno los módulos de la plataforma.

-**4:** Despliega "mongoexpress" un cliente web para mongodb.

-**5:** Nos va preguntando que PODs queremos eliminar del despliegue.

-**6:** Nos pregunta que PV's y PVC's queremos eliminar del despliegue.

-**7:** Elimina el POD de mongoexpress.

-**8:** Elimina el POD del configinit.

-**9:** Detiene la ejecución del script.

Para desplegar la plataforma desde cero deberíamos, primero lanzar los módulos de persistencia; acto seguido debemos poblar las bases de datos con el configinit y por último, desplegaremos los módulos de la plataforma.
    
