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

modulesst=$MODULES

modulesarray=(${modulesst//:/ })

echo "############################ 1 Module image migration to new registry"

for i in "${!modulesarray[@]}"
do
	if [[ "$(docker images -q $REGISTRY_SOURCE_PROJECT/${modulesarray[i]} 2> /dev/null)" == "" ]]; then

		sourceimage=$REGISTRY_SOURCE_HOST/$REGISTRY_SOURCE_PROJECT/${modulesarray[i]}:$MODULE_TAG
		targetimage=$REGISTRY_TARGET_HOST/$REGISTRY_TARGET_PROJECT/${modulesarray[i]}:$MODULE_TAG
		echo "Pulling image from source registry: "$sourceimage
		docker pull $sourceimage
		
		echo "Tagging image for pushing to target registry: "$targetimage
		docker tag $sourceimage $targetimage
		           
		echo "Pushing image to registry: "$REGISTRY_TARGET_HOST           
		docker push $targetimage           
	fi
done

infrast=$INFRA

infraarray=(${infrast//:/ })

echo "############################ 2 Infrastructure image migration to new registry"

for i in "${!infraarray[@]}"
do
	if [[ "$(docker images -q $REGISTRY_SOURCE_PROJECT/${infraarray[i]} 2> /dev/null)" == "" ]]; then

		sourceimage=$REGISTRY_SOURCE_HOST/$REGISTRY_SOURCE_PROJECT/${infraarray[i]}:$INFRA_TAG
		targetimage=$REGISTRY_TARGET_HOST/$REGISTRY_TARGET_PROJECT/${infraarray[i]}:$INFRA_TAG
		echo "Pulling image from source registry: "$sourceimage
		docker pull $sourceimage
		
		echo "Tagging image for pushing to target registry: "$targetimage
		docker tag $sourceimage $targetimage
		           
		echo "Pushing image to registry: "$REGISTRY_TARGET_HOST           
		docker push $targetimage           
	fi
done

persistencest=$PERSISTENCE

persistencearray=(${persistencest//:/ })

echo "############################ 3 Persistence image migration to new registry"

for i in "${!persistencearray[@]}"
do
	if [[ "$(docker images -q $REGISTRY_SOURCE_PROJECT/${persistencearray[i]} 2> /dev/null)" == "" ]]; then

		sourceimage=$REGISTRY_SOURCE_HOST/$REGISTRY_SOURCE_PROJECT/${persistencearray[i]}:$PERSISTENCE_TAG
		targetimage=$REGISTRY_TARGET_HOST/$REGISTRY_TARGET_PROJECT/${persistencearray[i]}:$PERSISTENCE_TAG
		echo "Pulling image from source registry: "$sourceimage
		docker pull $sourceimage
		
		echo "Tagging image for pushing to target registry: "$targetimage
		docker tag $sourceimage $targetimage
		           
		echo "Pushing image to registry: "$REGISTRY_TARGET_HOST           
		docker push $targetimage           
	fi
done

exit 0