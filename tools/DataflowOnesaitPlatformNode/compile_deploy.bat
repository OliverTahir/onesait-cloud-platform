: Change docker container ID in both places with your dataflow docker contaniner id
call mvn clean package -DskipTests
call docker cp .\target\onesaitplatform-streamsets-1.0.tar.gz 33533264254b:/opt/streamsets-datacollector-user-libs/onesaitplatform-streamsets-1.0.tar.gz
call docker exec -u root -it 33533264254b tar xvfz /opt/streamsets-datacollector-user-libs/onesaitplatform-streamsets-1.0.tar.gz -C /opt/streamsets-datacollector-user-libs
call docker restart 33533264254b