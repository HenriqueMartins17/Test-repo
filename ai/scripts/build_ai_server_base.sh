#!/bin/bash

set -e

REGISTRY_NAMESPACE=${REGISTRY_NAMESPACE:-vikadata}
SEMVER_EDITION=${DEFUALT_SEMVER_EDITION:-vika}
SHA_TAG=$(shasum pyproject.toml | awk '{print $1}')
VERSION=$(cat .version)
export TAG_ONLY="0"

function build_local_base {
  docker build  -f Dockerfile.base . --tag=vika/ai-server-base
}

function build_push_base {
  if [ "${TAG_ONLY}" == "0" ];then
    # ai-server-base not push docker.vika.ltd
    REGISTRY_SERVER_ORIGIN=${REGISTRY_SERVER}
    REGISTRY_USERNAME_ORIGIN=${REGISTRY_USERNAME}
    REGISTRY_PASSWORD_ORIGIN=$REGISTRY_PASSWORD}
    CR_PAT_ORIGIN=${CR_PAT}
    export CR_PAT=""
    export REGISTRY_SERVER="ghcr.io"
    export REGISTRY_USERNAME=${REGISTRY_NAMESPACE}
    export REGISTRY_PASSWORD=${CR_PAT_ORIGIN}
    export TARGET_DOCKER_TAGS="latest ${SHA_TAG} ${VERSION}"
    export DOCKERFILE=Dockerfile.base
    # run the semver_ci.sh script from ops-manager to get the version number
    env_dotversion
    build_docker_unableack dotversion ai-server-base
    
    #export origin REGISTRY_SERVER 
    export REGISTRY_SERVER=${REGISTRY_SERVER_ORIGIN}
    export REGISTRY_USERNAME=${REGISTRY_USERNAME_ORIGIN}
    export REGISTRY_PASSWORD=${REGISTRY_PASSWORD_ORIGIN}
    export CR_PAT=${CR_PAT_ORIGIN}
 
  fi
  #tag to vika/ai-server-base
  docker tag ghcr.io/${REGISTRY_NAMESPACE}/${SEMVER_EDITION}/ai-server-base:${SHA_TAG} vika/ai-server-base:latest
}


#main 
if [ -z "${BUILD_LOCAL}" ];then
   build_local_base
   exit 0
fi

if echo "${CR_PAT}" | docker login ghcr.io -u "${REGISTRY_NAMESPACE}" --password-stdin; then
    sleep 3
    docker pull ghcr.io/${REGISTRY_NAMESPACE}/${SEMVER_EDITION}/ai-server-base:${SHA_TAG} && export TAG_ONLY="1"
    build_push_base
fi