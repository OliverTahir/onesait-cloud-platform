pipeline {
   
   // Execute the Pipeline, or stage, on any available agent	
   agent { node { label 'sofia2master' } }

   options { 
   	  buildDiscarder(logRotator(numToKeepStr:'1'))
      disableConcurrentBuilds()
   }
      
   tools { 
   	  maven 'maven'
   	  jdk 'java8'
   }   

   environment {          
      // Base sources path 
      SYSTEMCONFIG = 'sources/modules/config-init' 
      SOURCESPATH = 'sources'
      IMAGEGENPATH = 'devops/build-deploy/docker'
      DOCKERCONFIG = 'devops/build-deploy/docker/data'   
	  BRANCHNAME = 'develop' 	
   }
   
   triggers {
   	  // some time between 12:00 AM (midnight) to 2:59 AM
   	  cron('H H(0-2) * * *')
   }   
   
   stages {
	   
	   stage('Build Artifacts') {
            when {
                branch "${env.BRANCHNAME}"
            }

	   		steps {					
				// Starts configdb, realtimedb, elasticdb and Quasar	
				dir("${env.DOCKERCONFIG}") {
					sh "docker-compose up -d || true"
				}
				
		    	// Only compile and generate artifacts
		    	dir("${env.SOURCESPATH}") {
	        		sh "mvn clean install -P integration,no-test"
		    	}
		    	
	   			// Generates config init image	   		
	   		    dir("${env.IMAGEGENPATH}") {
	   		    	sh "./image-generation.sh -1"
	   		    }	
	   		    
				dir("${env.DOCKERCONFIG}") {
					// -d flag not necessary, this container die by itself
					sh "docker-compose -f docker-compose.cicd-initdb.yml up || true"				
				}	   		    		    		   						
	   			
	   			// Execute tests
	   			dir("${env.SOURCESPATH}") {
	   				sh "mvn verify -P integration,unit-test"
	   				//sh "mvn verify -P integration,unit-test,integration-test"
					
					sh "mvn sonar:sonar"
	   			}
	   		}
	   }	 
   
   }
   
   post {
        always {
			dir("${env.DOCKERCONFIG}") {
				echo "Stopping Docker containers"
				sh "docker-compose down || true"
				
				echo "Removing stopped Docker containers"
				sh "docker rm \$(docker ps -aq) || true"				
				
        		echo "Removing orphan volumes"
        		sh "docker volume rm \$(docker volume ls -qf dangling=true) || true"	
        		
        		echo "Removing config init image"
        		sh "docker rmi -f \$(docker images | grep onesaitplatform/ | awk '{print \$3}' | uniq) || true"			
			}        
			
        	echo 'Clean up workspace...'
        	deleteDir()       		
        }   
	    success {	    
	        echo "Pipeline: '${currentBuild.fullDisplayName}' completado satisfactoriamente" 
			emailext attachLog: true, 
			body: 'La compilación de la rama $BRANCH_NAME del proyecto $PROJECT_NAME se ha completado satisfactoriamente. url del Build: $BUILD_URL', 
			compressLog: true, 
			subject: '[SUCCESSFUL!] La compilación de la rama $BRANCH_NAME del proyecto $PROJECT_NAME se ha completado satisfactoriamente. id del Build: $BUILD_NUMBER', 
			to: 'mmoran@minsait.com'
 		}
	    failure {   
	    	echo "El pipeline: '${currentBuild.fullDisplayName}' ha fallado: '${env.BUILD_URL}' se procede a enviar notificación por correo"
			emailext attachLog: true, 
			body: 'Ha ocurrido un error al compilar los fuentes de la rama $BRANCH_NAME del proyecto $PROJECT_NAME.url del Build: $BUILD_URL', 
			compressLog: true, 
			subject: '[ERROR!] Ha ocurrido un error al compilar los fuentes de la rama $BRANCH_NAME del proyecto $PROJECT_NAME. Se adjuntan los logs de la compilación. id del Build: $BUILD_NUMBER', 
			to: 'lmgracia@minsait.com,mmoran@minsait.com, cfsanchez@minsait.com'
			//to: 'ialonsoc@minsait.com, alanton@minsait.com, plantona@minsait.com, rbarrio@minsait.com, aclaramonte@minsait.com, jfgpimpollo@minsait.com, cfsanchez@indra.es, lfernandezsa@minsait.com, pgmarquina@minsait.com, fjgcornejo@minsait.com, lmgracia@minsait.com, rlgiron@minsait.com, jjmorenoa@minsait.com, mmourino@minsait.com, dsanteodoro@indra.es, ljsantos@minsait.com, rtvachet@minsait.com, mmoran@minsait.com'
	    }
   }      
}