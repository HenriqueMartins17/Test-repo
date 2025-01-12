#!/bin/bash

set -euo pipefail

REMOTE_REGISTRY=docker.vika.ltd/vikadata
LOCAL_REGISTRY=registry.local/vikadata

mkdir -p images

for i in $(docker compose config --images); do
    image=$(awk -F / '{ print $NF }' <<< "${i}")
    service=$(cut -d : -f 1 <<< "${image}")
    strip_front=$(cut -d / -f 3- <<< "${i}")
    docker image pull "${REMOTE_REGISTRY}/${strip_front}"
    docker image tag "${REMOTE_REGISTRY}/${strip_front}" "${LOCAL_REGISTRY}/${strip_front}"
    docker save "${LOCAL_REGISTRY}/${strip_front}" | gzip > "images/${service}.tar.gz"
done
