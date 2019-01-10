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

if [ "$SAVEIMAGES" = true ]; then
	echo "Saving all "$IMAGENAMESPACE " images to directory: "$DATAFOLDER
	for image in $(docker images | grep $IMAGENAMESPACE | awk '{print $1}'); 
	do 
	    imagename=${image////-}
	    echo "Image name: "$imagename
		docker save $image > $DATAFOLDER/$imagename.tar; 
	done
fi

if [ "$LOADIMAGES" = true ]; then
	echo "Loading all "$IMAGENAMESPACE " images from directory: "$DATAFOLDER
	for i in ls $DATAFOLDER/*; do echo $i; done
	for imagetar in $DATAFOLDER/*; 
	do 
	    docker load --input $imagetar
	done
fi

exit 0
