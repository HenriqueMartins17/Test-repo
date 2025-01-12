#!/bin/bash

set -e

# APITable Ltd. <legal@apitable.com>
# Copyright (C)  2022 APITable Ltd. <https://apitable.com>
# This code file is part of APITable Enterprise Edition.
# It is subject to the APITable Commercial License and conditional on having a fully paid-up license from APITable.
# Access to this code file or other code files in this `enterprise` directory and its subdirectories does not constitute permission to use this code or APITable Enterprise Edition features.
# Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
# For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.

##
# require  CR_PAT, EDITION_BUILD, QS_BUCKET_NAME, QS_ACCESS_KEY, QS_SECRET_KEY, QS_ACCOUNT

## get image_tag form github
# $1 version.file like backend-server.version
get_image_tag(){
  #get tag from local
  local app=${1%%.*}
  local tag=`get_local_image_tag $app`
  if [[ ! -z "${tag}" ]];then
    echo $tag
    return
  fi 
  #get tag online
  if [[ -z "${CR_PAT}" ]]; then
      echo "[WARNING] Need \$CR_PAT Github Package Personal Access Token Define..."
      read -p "Please enter CR_PAT: " CR_PAT
  fi
  version_file=$1
  ENV_BRANCH=${EDITION_SEMVER}-staging
  tag=`curl https://${CR_PAT}@raw.githubusercontent.com/vikadata/devops/deploy-version/status/${ENV_BRANCH}/${version_file} | jq '.message' | tr -d '"'`
  echo $tag
}

## get image_tag from param
# $1 app like backend-server
# .env.custom 固定版本镜像配置文件,格式与生成 .env 一致. 
get_local_image_tag(){
  local server_name=$1
  local tag=""
  if [ -f .env.custom ];then
      tag=`grep "${server_name}:" .env.custom | awk -F":" '{print $2}'`
  fi
  echo $tag
}

## $1 app_name
## $2 local_image
tar_image(){
    app=${1}
    local_img=${2}

    mkdir -p $DST_IMG_DIR

    remote_img=$(sed "s|registry.local/[a-zA-Z0-9]\+|${REGISTRY_SERVER}/${REGISTRY_NAMESPACE}|" <<< "${local_img}")
    local tar_name="${app}.tar"

    docker run --rm -v "${PWD}/${DST_IMG_DIR}":/opt --workdir /opt quay.io/skopeo/stable:latest copy --retry-times 5 --src-username "${REGISTRY_USERNAME}" --src-password "${REGISTRY_PASSWORD}" docker://"${remote_img}" docker-archive:"${tar_name}":"${local_img}"
    gzip "${DST_IMG_DIR}/${tar_name}"
    sha256sum "${DST_IMG_DIR}/${tar_name}.gz" >> "${DST_IMG_DIR}/sha256sum.txt"

    if [[ "${ENABLE_MIRROR_REGISTRY}" = false ]]; then
        return
    fi

    mirror_img=$(sed "s|registry.local/[a-zA-Z0-9]\+|${MIRROR_REGISTRY_SERVER}/${MIRROR_REGISTRY_NAMESPACE}|" <<< "${local_img}")

    docker run --rm -v "${PWD}/${DST_IMG_DIR}":/opt --workdir /opt quay.io/skopeo/stable:latest copy --all --preserve-digests --retry-times 5 --src-username "${REGISTRY_USERNAME}" --src-password "${REGISTRY_PASSWORD}" --dest-username "${MIRROR_REGISTRY_USERNAME}" --dest-password "${MIRROR_REGISTRY_PASSWORD}" docker://"${remote_img}" docker://"${mirror_img}"
}

download_docker(){
  wget -c  https://download.vika.cn/vika/docker-compose/${ARCH}/docker.tar.gz
  tar -zxvf docker.tar.gz -C ./${EDITION_BUILD}/
}

#upload file to key
# $1 filename
# $2 dst key
_upload_qs(){
    # qshell CLI Tool download
    wget "https://devtools.qiniu.com/qshell-v2.10.0-linux-${ARCH}.tar.gz" -O /tmp/qshell.tar.gz
    bash -c "cd /tmp && tar xzvf /tmp/qshell.tar.gz"

    du -sh $2
    # qshell auth
    /tmp/qshell --version
    /tmp/qshell account $QS_ACCESS_KEY $QS_SECRET_KEY $QS_ACCOUNT --overwrite
    #/tmp/qshell fput bucket_name 目标key路径 被上传文件
    /tmp/qshell rput ${QS_BUCKET_NAME} $1 $2
}

## release version to private log
# $1 version
# $2 version desc message
# $3 package location
_release_version(){
  if [[ -z "${VIKA_PRODUCTION_API_TOKEN}" ]]; then
      echo "[WARNING] Need \$VIKA_PRODUCTION_API_TOKEN vika Package Personal Access Token Define..."
      read -p "Please enter VIKA_PRODUCTION_API_TOKEN: " VIKA_PRODUCTION_API_TOKEN
  else
      echo "Found \VIKA_PRODUCTION_API_TOKEN. "
  fi

  version=${1}
  message=${2}
  package=${3}

  curl -X POST "https://api.vika.cn/fusion/v1/datasheets/dstNNrAAhmo8h1WzgK/records?viewId=viwN7fAj3FvNR&fieldKey=name"  \
    -H "Authorization: Bearer ${VIKA_PRODUCTION_API_TOKEN}" \
    -H "Content-Type: application/json" \
    --data '{
    "records": [
      {
        "fields": {
          "安装模式": [
            "POC"
          ],
          "版本号": "'${version}'",
          "更新记录": "'${message}'",
          "是否定制版": "否",
          "安装包下载(七牛云)": "'${package}'"
        }
      }
    ]
    }'
}

# The functional codes for this shell start from here, you can ignore
function select_cmd() {
  echo "Please select what you want to do:"
  select CMD in "${cmds[@]}"; do
    if [[ $(in_array "$CMD" "${cmds[@]}") = 0 ]]; then
      do_command "$CMD"
      break
    fi
  done
}

build_private_vika(){
    #
    export EDITION_BUILD="vikadata"
    export EDITION_SEMVER="vika"
    export DST_IMG_DIR=${EDITION_BUILD}/images
    mkdir -p ${DST_IMG_DIR}
    source .env.vikadata

    download_docker
    # get image
    versions=`grep "\.version$" .env.vikadata`
    for i in $versions
    do
       version_file=`echo $i | awk -F'/' '{print $NF}'`
       echo $i "->>> $version_file"
       tag=`get_image_tag $version_file`
       app=`echo $version_file | awk -F'.' '{print $1}'`
       # web-server replace to op
       [ "${app}" == "web-server" ] && tag=`echo $tag | sed -r 's/-(.*)_build/-op_build/g'`
       #echo $IMAGE_REGISTRY
       image=`echo ${i#*=} | sed -n "s/${version_file}/${app}:${tag}/p"`
       echo $image
       # download image
       tar_image $app $IMAGE_REGISTRY/$image

       #replace .env.vikadata
       image_key=`echo $i | awk -F'=' '{print $1}'`
       #for linux
       sed -i "/^${image_key}=/c${image_key}=${image}" .env.vikadata
    done

    ## 特殊处理镜像
    tar_image init-appdata $IMAGE_REGISTRY/$IMAGE_INIT_APPDATA
    tar_image init-settings $IMAGE_REGISTRY/$IMAGE_INIT_SETTINGS
    tar_image openresty $IMAGE_REGISTRY/$IMAGE_GATEWAY
    tar_image imageproxy $IMAGE_REGISTRY/$IMAGE_IMAGEPROXY_SERVER

    # infra
    tar_image minio $IMAGE_MINIO
    tar_image mysql $IMAGE_MYSQL
    tar_image rabbitmq $IMAGE_RABBITMQ
    tar_image redis $IMAGE_REDIS

    # gen .env.template
    cat ../../apitable/.env .env.vikadata > ${EDITION_BUILD}/.env.template
    touch ${EDITION_BUILD}/.env.local
    # package gateway
    cp -a ../../apitable/gateway ${EDITION_BUILD}/
    # package docker-compose
    cp -a docker-compose.yaml ${EDITION_BUILD}/
    cp -a install.sh ${EDITION_BUILD}/

    semver_version=$(cat ../../.version)
    package=$(echo ${EDITION_BUILD}-${semver_version}-$(date +%s)-${BUILD_NUM}-${ARCH})
    tar -zcvf "${package}-offline.tar.gz" "${EDITION_BUILD}"

    sed -i "s/registry.local/${MIRROR_REGISTRY_SERVER}/g" ${EDITION_BUILD}/.env.template
    sed -i "s/\<vikadata\>/${MIRROR_REGISTRY_NAMESPACE}/g" ${EDITION_BUILD}/.env.template
    rm -fr "${DST_IMG_DIR}"
    mkdir -p "${DST_IMG_DIR}"
    tar -zcvf "${package}.tar.gz" "${EDITION_BUILD}"

    #upload qs
    export QS_BUCKET_NAME=vk-private
    _upload_qs "poc/${semver_version}/${package}-offline.tar.gz" "${package}-offline.tar.gz"
    _upload_qs "poc/${semver_version}/${package}.tar.gz" "${package}.tar.gz"

    #release
    local message=$(sed  '/^#/d' .env.vikadata | sed 's/$/\\n/' | tr -d '\n')
    _release_version ${package} "${message}" poc/${semver_version}/${package}.tar.gz
}

build_private_apitable(){
    #
    export EDITION_BUILD="apitable"
    export EDITION_SEMVER="aitable"
    export DST_IMG_DIR=${EDITION_BUILD}/images
    mkdir -p ${DST_IMG_DIR}
    source .env.apitable

    download_docker
    # get image
    versions=`grep "\.version$" .env.apitable`
    for i in $versions
    do
       version_file=`echo $i | awk -F'/' '{print $NF}'`
       echo $i "->>> $version_file"
       tag=`get_image_tag $version_file`
       app=`echo $version_file | awk -F'.' '{print $1}'`
       # web-server replace to op
       [ "${app}" == "web-server" ] && tag=`echo $tag | sed -r 's/-(.*)_build/-op_build/g'`
       #echo $IMAGE_REGISTRY
       image=`echo ${i#*=} | sed -n "s/${version_file}/${app}:${tag}/p"`
       echo $image
       # download image
       tar_image $app $IMAGE_REGISTRY/$image

       #replace .env.vikadata
       image_key=`echo $i | awk -F'=' '{print $1}'`
       #for linux
       sed -i "/^${image_key}=/c${image_key}=${image}" .env.apitable
    done

    ## 特殊处理镜像
    tar_image init-appdata $IMAGE_REGISTRY/$IMAGE_INIT_APPDATA
    tar_image init-settings $IMAGE_REGISTRY/$IMAGE_INIT_SETTINGS
    tar_image openresty $IMAGE_REGISTRY/$IMAGE_GATEWAY
    tar_image imageproxy $IMAGE_REGISTRY/$IMAGE_IMAGEPROXY_SERVER

    # infra
    tar_image minio $IMAGE_MINIO
    tar_image mysql $IMAGE_MYSQL
    tar_image rabbitmq $IMAGE_RABBITMQ
    tar_image redis $IMAGE_REDIS

    # gen .env.template
    cat ../../apitable/.env .env.apitable > ${EDITION_BUILD}/.env.template
    touch ${EDITION_BUILD}/.env.local
    # package gateway
    cp -a ../../apitable/gateway ${EDITION_BUILD}/
    # package docker-compose
    cp -a docker-compose.yaml ${EDITION_BUILD}/
    cp -a install.sh ${EDITION_BUILD}/

    semver_version=$(cat ../../.version)
    package=$(echo ${EDITION_BUILD}-${semver_version}-$(date +%s)-${BUILD_NUM}-${ARCH})
    tar -zcvf "${package}-offline.tar.gz" "${EDITION_BUILD}"

    sed -i "s/registry.local/${MIRROR_REGISTRY_SERVER}/g" ${EDITION_BUILD}/.env.template
    rm -fr "${DST_IMG_DIR}"
    mkdir -p "${DST_IMG_DIR}"
    tar -zcvf "${package}.tar.gz" "${EDITION_BUILD}"

    #upload qs
    export QS_BUCKET_NAME=apitable-private
    _upload_qs "poc/${semver_version}/${package}-offline.tar.gz" "${package}-offline.tar.gz"
    _upload_qs "poc/${semver_version}/${package}.tar.gz" "${package}.tar.gz"

    #release
    local message=$(sed  '/^#/d' .env.apitable | sed 's/$/\\n/' | tr -d '\n')
    _release_version ${package} "${message}" poc/${semver_version}/${package}.tar.gz
}

# compute-nest version
build_private_compute_nest(){

    echo "build aliyun computnest version"
    export EDITION_BUILD="apitable"
    export EDITION_SEMVER="aitable"
    export DST_IMG_DIR=${EDITION_BUILD}/images
    mkdir -p ${DST_IMG_DIR}

    # ...
    semver_version=`cat ../../.version`
    package=`echo ${EDITION_BUILD}-${semver_version}-$(date +%s)-${BUILD_NUM}-${ARCH}`
}

_initialize(){
  export ARCH=$( [ `uname -m` == "x86_64" ] && echo "amd64" || echo "arm64")
  export CI_NAME=$(if [ "$CIRCLE_BUILD_NUM" ]; then echo "circleci"; \
                        elif [ "$GITHUB_RUN_NUMBER" ]; then echo "githubaction"; \
                        else echo "local"; fi)
  export BUILD_NUM=$(if [ "$CI_NAME" = "circleci" ]; then echo "$CIRCLE_BUILD_NUM"; \
                      elif [ "$CI_NAME" = "githubaction" ]; then echo "$GITHUB_RUN_NUMBER"; \
                      else echo "0"; fi)
  #write .env.custom
  if [[ ! -z "${CUSOM_ENV}" ]];then
    #wrap string
    wrap_string=$(echo "$CUSOM_ENV" | tr ' ' '\n')
    echo -n "$wrap_string" > .env.custom
  fi
}

# add new cmd entry here
cmds=(
  vika
  compute-nest
  apitable
)

function do_command() {
  case $1 in
  vika)
    build_private_vika
    ;;
  compute-nest)
    build_private_compute_nest
    ;;
  apitable)
    build_private_apitable
    ;;
  *)
    echo "No command matched here."
    ;;
  esac
}

function in_array() {
  TARGET=$1
  shift 1
  for ELEMENT in "$@"; do
    if [[ "$TARGET" == "$ELEMENT" ]]; then
      echo 0
      return 0
    fi
  done
  echo 1
  return 1
}

function main() {
  if [[ $1 != "" && $(in_array "$1" "${cmds[@]}") = 0 ]]; then
    do_command "$@"
  else
    select_cmd
  fi
}

_initialize
main "$@"
