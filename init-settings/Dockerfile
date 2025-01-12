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

FROM node:16-alpine3.16 AS builder
WORKDIR /settings
COPY . .
ARG edition
RUN npm install @vikadata/vika
RUN npm install envfile
RUN echo "edition => ${edition}" > /tmp/edition
ARG CACHEBUST=1
RUN node script.localfile.js "${edition}"

FROM node:16-alpine3.16
WORKDIR /settings
COPY --from=builder /settings/custom ./custom 
COPY ./run.sh ./run.sh
