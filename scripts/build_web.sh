#!/bin/bash

set -e

######
###### build saas vika version product
######
function build_saas_vika {
  export DEFUALT_SEMVER_EDITION="vika"

  _build_saas "https://s4.vika.cn" "${QS_BUCKET_NAME}"
}

######
###### build saas apitable version product
######
function build_saas_apitable {
  export DEFUALT_SEMVER_EDITION="apitable"

  _build_saas "https://s4.aitable.ai" "${QS_APITABLE_BUCKET_NAME}"
}

######
###### build saas version product
######
function _build_saas {
  local next_asset_prefix="$1"
  local upload_qiniu_bucket_name="$2"

  # run the semver_ci.sh script from ops-manager to get the version number
  env_dotversion

  export DOCKERFILE=packaging/Dockerfile.web-server
  export BUILD_ARG="--build-arg SEMVER_FULL=${SEMVER_FULL} --build-arg NEXT_ASSET_PREFIX=${next_asset_prefix} --build-arg NEXT_PUBLIC_ASSET_PREFIX=\$NEXT_ASSET_PREFIX/_next/public"
  build_docker_unableack dotversion web-server

  # copy static resources ready upload
  mkdir -p /tmp/app/"$SEMVER_FULL"/static /tmp/app/"$SEMVER_FULL"/public
  TEMP_CP_DOCKER_IMAGE=${DOCKER_IMAGE_NAME_FULL}:${DOCKER_IMAGE_TAG}_build${BUILD_NUM}
  echo "[INFO] copy docker image product -> $TEMP_CP_DOCKER_IMAGE"

  # shellcheck disable=SC2046
  docker cp $(docker create --name next_tmp "${TEMP_CP_DOCKER_IMAGE}"):/app/packages/datasheet/web_build/static /tmp/app/"$SEMVER_FULL" &&
    docker cp next_tmp:/app/packages/datasheet/public /tmp/app/"$SEMVER_FULL" &&
    docker rm next_tmp
  # shellcheck disable=SC2010
  echo "[INFO] copy docker image product complete，[/tmp/app/$SEMVER_FULL/] number of files -> $(ls -lR /tmp/app/"$SEMVER_FULL"/ | grep -c "^-")"

  _upload_source_map "https://sentry.vika.ltd" "${SENTRY_AUTH_TOKEN_VIKA}" "sentry"

  _upload_qiniu "${upload_qiniu_bucket_name}"

  # manual trigger rolling update
  _on_build_success

  # clean up uploading temporary files
  echo "[INFO] clean temporary directory -> /tmp/app/$SEMVER_FULL"
  rm -fr /tmp/app/"$SEMVER_FULL"
}

######
###### build private version product
######
function build_op {
  env_dotversion

  OP_SEMVER_FULL="v${SEMVER_NUMBER}-op_build$BUILD_NUM"

  export DOCKERFILE=packaging/Dockerfile.web-server
  export TARGET_DOCKER_TAGS="latest-op ${OP_SEMVER_FULL}"
  export BUILD_ARG="--build-arg SEMVER_FULL=${OP_SEMVER_FULL} --build-arg NEXT_ASSET_PREFIX= --build-arg NEXT_PUBLIC_ASSET_PREFIX="
  build_docker_unableack dotversion web-server
}

######
###### upload qiniu cnd
######
function _upload_qiniu {
  local bucket_name="$1"

  # qshell CLI Tool download
  wget "https://devtools.qiniu.com/qshell-v2.10.0-linux-${ARCH}.tar.gz" -O /tmp/qshell.tar.gz
  bash -c "cd /tmp && tar xzvf /tmp/qshell.tar.gz"

  # qshell auth
  /tmp/qshell --version
  /tmp/qshell account "$QS_ACCESS_KEY" "$QS_SECRET_KEY" "$QS_ACCOUNT" --overwrite

  # upload files using qshell
  for i in {1..15}; do
    if /tmp/qshell qupload2 --src-dir=/tmp/app/"$SEMVER_FULL"/public --skip-path-prefixes=static/ --bucket="${bucket_name}" --key-prefix=_next/public/ --thread-count=8 --overwrite=true --rescan-local=true; then
      break
    fi
    if (( i == 15 )); then
        false
    fi
    sleep 3
  done
  for i in {1..15}; do
    if /tmp/qshell qupload2 --src-dir=/tmp/app/"$SEMVER_FULL"/static --bucket="${bucket_name}" --key-prefix=_next/static/ --thread-count=8 --overwrite=true --rescan-local=true; then
      break
    fi
    if (( i == 15 )); then
        false
    fi
    sleep 3
  done
}

######
###### Upload source map to sentry platform
######
function _upload_source_map {
  local sentry_project="web-server"
  local sentry_url="$1"
  local sentry_auth_token="$2"
  local sentry_org_slug="$3"

  curl -sL https://sentry.io/get-cli/ | SENTRY_CLI_VERSION=2.4.1 bash || sentry-cli --version
  # shellcheck disable=SC2088
  sentry-cli --url "${sentry_url}" --auth-token "${sentry_auth_token}" \
  releases --project "${sentry_project}" --org "${sentry_org_slug}" \
  files "$SEMVER_FULL" upload-sourcemaps /tmp/app/"$SEMVER_FULL"/ --url-prefix "~/_next/static" || echo "$SEMVER_FULL"
}

# add new cmd entry here
cmds=(
  build_saas_vika
  build_saas_apitable
  build_op
)

function do_command() {
  case $1 in
  build_saas_vika)
    build_saas_vika
    ;;
  build_saas_apitable)
    build_saas_apitable
    ;;
  build_op)
    build_op
    ;;
  *)
    echo "No command matched here."
    ;;
  esac
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

ARCH=$([ "$(uname -m)" == "x86_64" ] && echo "amd64" || echo "arm64")
export ARCH

main "$@"
