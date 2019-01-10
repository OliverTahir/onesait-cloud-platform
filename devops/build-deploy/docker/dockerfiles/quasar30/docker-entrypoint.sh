#!/bin/sh

echo "Substituting environment variables in Quasar properties"	
	
grep -rl '${REALTIMEDB}' /usr/local/onesait/quasar | xargs sed -i 's/${REALTIMEDB}/'"$REALTIMEDB"'/g'
grep -rl '${REALTIMEDBPORT}' /usr/local/onesait/quasar | xargs sed -i 's/${REALTIMEDBPORT}/'"$REALTIMEDBPORT"'/g'
grep -rl '${AUTHPARAMS}' /usr/local/onesait/quasar | xargs sed -i 's/${AUTHPARAMS}/'"$AUTHPARAMS"'/g'
grep -rl '${AUTHDB}' /usr/local/onesait/quasar | xargs sed -i 's/${AUTHDB}/'"$AUTHDB"'/g'	
grep -rl '${TIMEOUTMS}' /usr/local/onesait/quasar | xargs sed -i 's/${TIMEOUTMS}/'"$TIMEOUTMS"'/g'		
	
echo "Executing Quasar..." 
java $JAVA_OPTS -jar quasar-web-assembly-30.0.0.jar initUpdateMetaStore -c config.json

java $JAVA_OPTS -Dlog4j.configuration=file:/usr/local/onesait/quasar/log4j.properties -jar quasar-web-assembly-30.0.0.jar -c config.json -P plugins/