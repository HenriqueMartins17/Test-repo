#!/bin/bash
# APITable Ltd. <legal@apitable.com>
# Copyright (C)  2022 APITable Ltd. <https://apitable.com>
#
# This code file is part of APITable Enterprise Edition.
#
# It is subject to the APITable Commercial License and conditional on having a fully paid-up license from APITable.
#
# Access to this code file or other code files in this `enterprise` directory and its subdirectories does not constitute permission to use this code or APITable Enterprise Edition features.
#
# Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
#
# For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.

###
#  setting resource to web-server
#  @date 2022/10/28 14:14
###

datasheetDir=${1:-/app/packages/datasheet/}

SCRIPT_DIR=$(dirname $0)

# sync custom
cp -rf ${SCRIPT_DIR}/custom/* ${datasheetDir}/public/custom
cp -rf ${SCRIPT_DIR}/custom/.env  ${datasheetDir}/.env.development
cp -rf ${SCRIPT_DIR}/custom/.env  ${datasheetDir}/.env.production

