#!/bin/bash

L10N_EDITION_DIR="$1"

# reset /enterprise/core/config/billing.auto.json
if [ -e "./enterprise/core/config/billing.auto.json" ]; then
    echo "billing.auto.json file exists"
else
    ts-node "./apitable/packages/i18n-lang/src/mergeJsonFile.ts" "enterprise/core/config/billing.auto.json" "billing" "init-settings/base" "$L10N_EDITION_DIR"

    if [ -e "enterprise/core/config/billing.auto.json" ]; then
      echo "successfully generate billing.auto.json"
    else
        echo "not find the file ${L10N_GEN_DIR}/notification.json"
    fi
fi