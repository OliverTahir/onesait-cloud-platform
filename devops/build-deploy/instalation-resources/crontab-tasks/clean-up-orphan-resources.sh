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
PATH=/sbin:/bin:/usr/sbin:/usr/bin:/usr/local/bin
export PATH

echo "1- Remove Orphan volumes..."
docker volume rm $(docker volume ls -qf dangling=true)

echo "2- Remove Untagged images"
docker rmi -f $(docker images -f dangling=true -q)

exit 0
