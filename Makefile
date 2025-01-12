.DEFAULT_GOAL := help
SHELL := /bin/bash
UNAME_S := $(shell uname -s)
SEMVER3 := $(shell cat .version)
define ANNOUNCE_BODY
        _ _             _       _
       (_) |           | |     | |
 __   ___| | ____ _  __| | __ _| |_ __ _
 \ \ / / | |/ / _` |/ _` |/ _` | __/ _` |
  \ V /| |   < (_| | (_| | (_| | || (_| |
   \_/ |_|_|\_\__,_|\__,_|\__,_|\__\__,_|


Vikadata Makefile $(SEMVER3)
================================================================

endef
export ANNOUNCE_BODY

DEVENV_PROJECT_NAME := vikadata-devenv
export DEVENV_PROJECT_NAME

DATA_PATH := ../
export DATA_PATH

ENV_FILE := $(shell pwd)/.env
export ENV_FILE

_check_env: ## check if .env files exists
# check if env file exists
	@FILE=$$ENV_FILE ;\
	if [ ! -f "$$FILE" ]; then \
			echo "$$FILE does not exist. Please 'make env' first" ;\
			exit 1 ;\
	fi

.PHONY: setup
setup: ## setup basic local envinroment softwares
ifeq ($(UNAME_S),Darwin)
	make setup-mac
else ifeq ($(UNAME_S),Linux)
	make setup-linux
endif


define TRANSLATE_TEXT
Translate Command:
  1) Translate all untranslated keys
  2) Translate all newly added keys
  3) Translate UI
endef
export TRANSLATE_TEXT

translate: ## translate content based on `apitable/packages/l10n/base/strings.en-US.json` (add content before translate)
	@if [ ! -d .data ]; then \
		mkdir -p .data; \
    fi; \
	if [ ! -f .data/trans.0.1.5.jar ]; then \
		printf "File .data/trans.0.1.5.jar does not exist, downloading...\n"; \
		curl -o .data/trans.0.1.5.jar https://s1.vika.cn/space/2023/08/01/ed8f011b8f5f44c5af030bcd5d35e060?attname=trans.0.1.5.jar; \
	fi; \
	printf "%s\n" "$$TRANSLATE_TEXT"; \
	read -p "ENTER THE NUMBER: " ENV_NUMBER; \
	if [ "$$ENV_NUMBER" = "1" ]; then \
		java -jar .data/trans.0.1.5.jar config=./i18n.trans.config.json diffSource=1; \
	fi; \
	if [ "$$ENV_NUMBER" = "2" ]; then \
		java -jar .data/trans.0.1.5.jar config=./i18n.trans.config.json translateAll=1; \
	fi; \
	if [ "$$ENV_NUMBER" = "3" ]; then \
		java -jar .data/trans.0.1.5.jar config=./i18n.trans.config.json ui=1; \
	fi;

setup-mac:
	cd scripts ;\
	pip3 install -r requirements.txt
	# support macOS M1 ARM to run x64 images
	softwareupdate --install-rosetta --agree-to-license
	# support node-canvas
	brew install pkg-config cairo pango libpng jpeg giflib librsvg pixman
	brew install python3 kubectl
	brew install --cask 1password/tap/1password-cli
	brew install openapi-generator

swagger: ## genereate databus-server java and nodejs client
	openapi-generator generate -i https://integration.aitable.ai/databus/api-docs/openapi.json -g typescript-axios -c scripts/.swagger-codegen.ts.config.json -o apitable/packages/databus-client

swagger-java: ## genereate databus-server java and nodejs client
	rm -rf ./apitable/backend-server/shared/starters/databus/src/main/java/com/apitable/starter/databus/client && \
	openapi-generator generate -i https://integration.aitable.ai/databus/api-docs/openapi.json -g java -c scripts/.swagger-codegen.java.config.json -o \
	./apitable/backend-server/shared/starters/databus --ignore-file-override=./apitable/backend-server/shared/starters/databus/.openapi-generator-ignore

swagger-nestjs-databus-client:
	rm -rf apitable/packages/room-server/tmp/databus-client/**
	openapi-generator generate -g typescript-nestjs \
    -o apitable/packages/room-server/tmp/databus-client \
	-i http://127.0.0.1:8625/databus/api-docs/openapi.json \
	--additional-properties=npmName=databus-client,fileNaming=kebab-case

swagger-nestjs-backend-client:
	rm -rf apitable/packages/room-server/tmp/backend-client/**
	openapi-generator generate -g typescript-nestjs \
    -o apitable/packages/room-server/tmp/backend-client \
	-i http://127.0.0.1:8081/api/v1/v3/api-docs/ \
	--additional-properties=npmName=backend-client,fileNaming=kebab-case

define CONFIGURE_ENV_CHOOSE
Which .env(envinroment) do you want to configure?
  1) intergration
  2) test
endef
export CONFIGURE_ENV_CHOOSE

api-codegen: ##
	@echo "$$CONFIGURE_ENV_CHOOSE"
	@read -p "ENTER THE NUMBER: " ENV_NUMBER ;\
 	if [ "$$ENV_NUMBER" = "" ]; then make api-codegen-integration; fi ;\
 	if [ "$$ENV_NUMBER" = "1" ]; then make api-codegen-integration; fi ;\
 	if [ "$$ENV_NUMBER" = "2" ]; then make api-codegen-test; fi;

api-codegen-test:
	touch apitable/packages/api-client/package.json
	cp apitable/packages/api-client/package.json /tmp/api-client_package.json.bak
	rm -rf apitable/packages/api-client/**
	openapi-generator generate --skip-validate-spec -g typescript -i https://test.vika.ltd/api/v1/v3/api-docs/  --additional-properties=stringEnums=false \
       --additional-properties=npmName=@apitable/api-client  \
       --additional-properties=npmVersion=0.0.1 \
       --additional-properties=useObjectParameters=true  --additional-properties=prependFormOrBodyParameters=true  -o  ./apitable/packages/api-client
	cp -rf /tmp/api-client_package.json.bak  apitable/packages/api-client/package.json
	@echo "api client has been generated from integration"
	pnpm run build:api-client

api-codegen-integration:
	touch apitable/packages/api-client/package.json
	cp apitable/packages/api-client/package.json /tmp/api-client_package.json.bak
	rm -rf apitable/packages/api-client/**
	openapi-generator generate --skip-validate-spec -g typescript -i https://integration.vika.ltd/api/v1/v3/api-docs/  --additional-properties=stringEnums=false \
       --additional-properties=npmName=@apitable/api-client  \
       --additional-properties=npmVersion=0.0.1 \
       --additional-properties=useObjectParameters=true  --additional-properties=prependFormOrBodyParameters=true  -o  ./apitable/packages/api-client
	cp -rf /tmp/api-client_package.json.bak  apitable/packages/api-client/package.json
	@echo "api client has been generated from integration"
	pnpm run build:api-client

#openapi-generator generate -i https://integration.vika.ltd/databus/api-docs/openapi.json -g java -c scripts/.swagger-codegen.java.config.json -o databus/databus-java-client && \
#openapi-generator generate -i https://integration.vika.ltd/databus/api-docs/openapi.json -g python -c scripts/.swagger-codegen.python.config.json -o databus/databus-python-client && \
#swagger-codegen generate -i https://integration.vika.ltd/databus/api-docs/openapi.json -l java -c scripts/.swagger-codegen.java.config.json -o enterprise/backend-server/databus-client && \


define CONFIGURE_TEXT
Which .env(envinroment) do you want to configure?
  1) local-db: local app services + local dataenv(default)
  2) docker-all: local docker app services + local docker datacenter
  3) remote-db: local app services + remote cloud development datacenter
  4) integration: remote integration envinroment connect
  5) testing: remote testing envinroment connect + integration databases
endef
export CONFIGURE_TEXT

.PHONY: env
env: ## configure the .env
	@echo "$$CONFIGURE_TEXT"
	@read -p "ENTER THE NUMBER: " ENV_NUMBER ;\
 	if [ "$$ENV_NUMBER" = "" ]; then make _env-local-db; fi ;\
 	if [ "$$ENV_NUMBER" = "1" ]; then make _env-local-db; fi ;\
 	if [ "$$ENV_NUMBER" = "2" ]; then make _env-docker-all; fi ;\
 	if [ "$$ENV_NUMBER" = "3" ]; then make _env-remote-db; fi;\
 	if [ "$$ENV_NUMBER" = "4" ]; then make _env-integration; fi;\
 	if [ "$$ENV_NUMBER" = "5" ]; then make _env-test; fi;

_env-local-db:
	cat env/base.env.template > .env
	cat env/local-db.env.template >> .env
	cat .edition >> .env
	cat env/integration.env.web.common.template > apitable/packages/datasheet/.env.development
	cat env/local.env.web.template >> apitable/packages/datasheet/.env.development

_env-docker-all:
	cat env/base.env.template > .env
	cat env/docker-all.env.template >> .env
	cat .edition >> .env

_env-remote-db:
	cat env/base.env.template > .env
	op inject -i env/remote-db.env.template >> .env

_env-integration:
	cat env/base.env.template > .env
	cat env/integration.env.template >> .env
	cat env/integration.env.web.common.template > apitable/packages/datasheet/.env.development
	cat env/integration.env.web.template >> apitable/packages/datasheet/.env.development

_env-test:
	cat env/integration.env.web.common.template > apitable/packages/datasheet/.env.development
	cat env/test.env.web.template >> apitable/packages/datasheet/.env.development

# op inject -i env/docker-all.env.template -o .env
# make ports-kill
# make ports

define DEVENV_TXT
Which devenv do you want to start run?
  0) ALL
  1) backend-server
  2) room-server
  3) web-server
endef
export DEVENV_TXT


run: ## run local development environment
	cd apitable ;\
	make run

run-perf: ## run room-server with local programming language envinroment for performance profiling
	cd apitable ;\
	make run-perf

run-databus: ## run databus server with root .env
	cargo run --manifest-path=databus/databus-server/Cargo.toml

run-down:
	cd apitable ;\
	make devenv-down


define PORTS_TXT

Port forward development service (need `kubectl` and its ~/.kube/kubeconfig first)

  1) mysql-development:53306
  3) redis-development:56379
  4) rabbitmq-development:55672
  6) minio-development:59000,59001
  11) backend-server-integration:58081,58083
  12) room-server-integration:53333,53334


endef
export PORTS_TXT
ports: ## ports forward the development envinronment cloud service
	@echo "$$PORTS_TXT"
	kubectl port-forward services/mysql-primary-headless 53306:3306 -n vika-datacenter &
	kubectl port-forward services/redis-headless 56379:6379 -n vika-datacenter &
	kubectl port-forward services/rabbitmq-headless 55672:5672 15672:15672 4369:4369 25672:25672 -n vika-datacenter &
	kubectl port-forward services/minio 59000:9000 59001:9001 -n vika-datacenter &
	kubectl port-forward services/backend-server 58081:8081 58083:8083 -n vika-integration &
	kubectl port-forward services/room-server 53333:3333 53334:3334 -n vika-integration &

define PORTS_PRO_TXT
Port forward development service (need `kubectl` and its ~/.kube/kubeconfig first)

  1) mysql-integration:33306
  6) mysql-production:43306
  4) redis-production:46379
  5) rabbit-production:45672
  7) mysql-production-readonly:53306
endef
export PORTS_PRO_TXT
ports-pro:
	@echo "$$PORTS_PRO_TXT"
	kubectl port-forward services/socat-0 33306:3306 -n vika-opsbase &
	kubectl port-forward services/socat-1 43306:3306 -n vika-opsbase &
	kubectl port-forward services/socat-2 46379:6379 -n vika-opsbase &
	kubectl port-forward services/socat-3 45672:5672 -n vika-opsbase &
	kubectl port-forward services/socat-4 53306:3306 -n vika-opsbase &

ports-kill: ## kill all ports forwarding by kubectl
	pkill -9 -f kubectl || true

ports-ls: ## list all ports forwarding
	ps aux | grep kubectl

dataenv: ## start database services environment
	cd apitable ;\
	make dataenv ;\
	cd .. ;\
	sleep 10
	make db_adminer ;\
	make db_edition_apply

DATAENV_HOSTS_SECTION := APITable Dev Setup

dataenv-setup: ## [sudo needed] setup hosts file for database hosts (for mysql/minio)
	@echo "Please 'sudo make dataenv-setup'"
	sudo printf "\n# Added by ${DATAENV_HOSTS_SECTION}\n127.0.0.1 mysql\n127.0.0.1 minio\n# End of section\n" >> /etc/hosts

dataenv-down: ## shutdown database services environment
	cd apitable ;\
	make dataenv-down

dataenv-ps:
	cd apitable ;\
	make dataenv-ps

.PHONY: patch
patch:
	docker run --rm -it --user $(shell id -u):$(shell id -g) -v "$(shell pwd):/app" ghcr.io/vikadata/vika/bumpversion:latest bumpversion patch ;\
	cd apitable ;\
	make patch

.PHONY: minor
minor:
	docker run --rm -it --user $(shell id -u):$(shell id -g) -v "$(shell pwd):/app" ghcr.io/vikadata/vika/bumpversion:latest bumpversion minor;\
	cd apitable ;\
	make minor

.PHONY: major
major:
	docker run --rm -it --user $(shell id -u):$(shell id -g) -v "$(shell pwd):/app" ghcr.io/vikadata/vika/bumpversion:latest bumpversion major;\
	cd apitable ;\
	make major


define BUMPVERSION_TXT
Which version number do you want to bump?
  1) patch
  2) minor
  3) major

endef
export BUMPVERSION_TXT
bumpversion: ## bumpversion, patch? minor? major?
	@echo "$$BUMPVERSION_TXT" ;\
	read -p "NUMBER>>" NUMBER ;\
	if [ "$$NUMBER" = "1" ]; then make patch; fi ;\
	if [ "$$NUMBER" = "2" ]; then make minor; fi ;\
	if [ "$$NUMBER" = "3" ]; then make major; fi ;\

.PHONY: pull
pull: ## pull all containers and ready to up
ifndef CR_PAT
	read -p "Please enter CR_PAT: " CR_PAT ;\
	echo $$CR_PAT | docker login ghcr.io -u vikadata --password-stdin ;\
	cd apitable && make pull
endif
ifdef CR_PAT
	echo $$CR_PAT | docker login ghcr.io -u vikadata --password-stdin ;\
	cd apitable && make pull
endif

.PHONY: up
up: _check_env ## start vikadata enterprise in docker
	cd apitable ;\
	make up

down: ## shutdown docker
	cd apitable ;\
	make down
ps:
	cd apitable ;\
	make ps

.PHONY: up-amd64
up-amd64: ## make up and force to amd64 platform
	DOCKER_DEFAULT_PLATFORM=linux/amd64 make up

.PHONY: up-arm64
up-arm64: ## make up and force to amd64 platform
	DOCKER_DEFAULT_PLATFORM=linux/arm64 make up

.PHONY: buildpush
buildpush: ## build vikadata enterprise all services with `docker buildx bake`
	cd apitable ;\
	make buildpush

.PHONY: buildpush-init-settings
buildpush-init-settings: ## build init settings template
	cp .version init-settings/ ;\
	mkdir -p init-settings/apitable/packages/l10n/ ;\
	cp -r apitable/packages/l10n/base init-settings/apitable/packages/l10n/ ;\
	cd init-settings ;\
	eval "$$(curl -fsSL https://vikadata.github.io/semver_ci.sh)" ;\
	export BUILD_ARG="--build-arg CACHEBUST=$$(date +%s) --build-arg edition=${EDITION}" ;\
	echo ${DEFUALT_SEMVER_EDITION} ;\
	build_docker init-settings

.PHONY: buildpush-private
buildpush-private: ## build private package and push qs
	cd scripts/build-private/ ;\
	bash build.sh ${EDITION}


# .PHONY: registry
# registry: build ## make a offline registry save with build all
# 	rm -rf /tmp/make_registry ;\
# 	mkdir /tmp/make_registry ;\
# 	docker rm -f make_registry ;\
# 	docker run -d -p 5002:5000 --name make_registry -v /tmp/make_registry:/var/lib/registry registry ;\
# 	docker tag docker.vikadata.com/vikadata/vika/init-db localhost:5002/vikadata/vika/init-db ;\
# 	docker push localhost:5002/vikadata/vika/init-db ;\
# 	cd /tmp ;\
# 	tar -czvf vikadata_registry.tar.gz make_registry/ ;\
# 	ls -al /tmp/vikadata_registry.tar.gz # TODO semver version number to add

.PHONY: install
install: ## install all dependencies witch docker devenv
	make install-local

.PHONY: install-docker
install-docker: _check_env ## install all dependencies witch docker devenv
	cd apitable ;\
	make install-docker

.PHONY: install-local
install-local: ## install all dependencies with local programming language environment
	pnpm install;
	cd apitable ;\
	make install-local

.PHONY: _install-databus
_install-databus:
	cd databus && cargo build
	cd databus/databus-wasm && make install


################################################################ make gen

define GENERATE_TXT

`make gen` is a command to generate code/settings/assets from APITable API.

What do you want to generate?
  1) settings: generate edition-specified files to build init-settings image
  2) proto: generate protobufs' types files
  3) design: design icons
  4) design: design colors

endef
export GENERATE_TXT
gen: ## generate code/settings/assets from datasheet API.
	@echo "$$GENERATE_TXT" ;\
	read -p "NUMBER>>" NUMBER ;\
	if [ "$$NUMBER" = "1" ]; then make _settings; fi ;\
	if [ "$$NUMBER" = "2" ]; then make _proto; fi ;\
	if [ "$$NUMBER" = "3" ]; then make _design-icons; fi  ;\
	if [ "$$NUMBER" = "4" ]; then make _design-colors; fi  ;\

_settings:
	make _l10n-settings
	make _settings-tscode-ce
	make edition-settings

_proto:
	cd apitable;\
	export TS_PROTO_OUT_PATH=packages/room-server/src/grpc/generated/;\
	docker compose --env-file $$ENV_FILE -p $$DEVENV_PROJECT_NAME -f docker-compose.devenv.yaml run --rm protoc sh scripts/compile.proto.sh;\
	export JAVA_PROTO_OUT_PATH=backend-server/application/src/main/proto;\
	docker compose --env-file $$ENV_FILE -p $$DEVENV_PROJECT_NAME -f docker-compose.devenv.yaml run --rm protoc sh scripts/compile.proto.sh;\

_design-icons:
	# 制作 icons
	@echo "Reading: https://integration.vika.ltd/workbench/dstVsMC4R1rljHAPqH/viw5n4DvlvAY2"
	@echo "Writing: package @apitable/icons"
	python3 ./scripts/design_token/make_icon.py
	cd apitable ;\
	pnpm --filter @apitable/icons run format
	@echo "Design Colors and Icons Generated"

_design-colors:
	# 根据 design token 表导出 colors
	@echo "Writing: apitable/packages/datasheet/src/pc/styles/lib_colors.css"
	python3 ./scripts/design_token/sync_colors.py


_settings-tscode-ce:
	cd apitable && \
	npx quicktype ./packages/i18n-lang/src/config/strings.json --just-types && \
	npx quicktype ./packages/core/src/config/system_config.source.json -o ./packages/core/src/config/system_config.interface.ts --just-types && \
	npx quicktype ./packages/core/src/config/api_tip_config.source.json -o ./packages/core/src/config/api_tip_config.interface.ts --just-types --no-maps



define EDITIONS_TXT
`make edition-settings` is a command to generate settings towards edition.

Which edition settings do you want to generate?
  1) apitable-ce
  2) apitable-ee
  3) apitable-saas
  4) vika-saas
  5) vika-computer-nest
  6) vika-ee
  7) vika-ee-baidu-ruliu
  8) vika-ee-linxing
  9) vika-ee-nanwang
  10) vika-ee-yantian
  11) vika-ee-tengxun-teg
  12) vika-ee-yonghang
endef
export EDITIONS_TXT
edition-settings: ## make settings to match edition
	@echo "$$EDITIONS_TXT" ;\
	read -p "NUMBER>>" NUMBER ;\
	if [ "$$NUMBER" = "1" ]; then EDITION=apitable-ce; fi ;\
	if [ "$$NUMBER" = "2" ]; then EDITION=apitable-ee; fi ;\
	if [ "$$NUMBER" = "3" ]; then EDITION=apitable-saas; fi ;\
	if [ "$$NUMBER" = "4" ]; then EDITION=vika-saas; fi  ;\
	if [ "$$NUMBER" = "5" ]; then EDITION=vika-computer-nest; fi  ;\
	if [ "$$NUMBER" = "6" ]; then EDITION=vika-ee; fi  ;\
	if [ "$$NUMBER" = "7" ]; then EDITION=vika-ee-baidu-ruliu; fi ;\
	if [ "$$NUMBER" = "8" ]; then EDITION=vika-ee-linxing; fi ;\
	if [ "$$NUMBER" = "9" ]; then EDITION=vika-ee-nanwang; fi ;\
	if [ "$$NUMBER" = "10" ]; then EDITION=vika-ee-yantian; fi ;\
	if [ "$$NUMBER" = "11" ]; then EDITION=vika-ee-tengxun-teg; fi ;\
	if [ "$$NUMBER" = "12" ]; then EDITION=vika-ee-yonghang; fi ;\
	mkdir -p init-settings/apitable/packages/l10n/ ;\
	cp -r apitable/packages/l10n/base init-settings/apitable/packages/l10n/ ;\
	cd init-settings ;\
	rm -rf "./custom"/* ;\
	npm install @vikadata/vika ;\
	npm install envfile ;\
	node script.localfile.js $$EDITION;\
	bash run.sh ../apitable/packages/datasheet


_l10n-settings: ## make settings to match edition
	cd apitable && \
	bash ./scripts/language-generate.sh ./packages/i18n-lang/src ./packages/l10n/gen ./packages/l10n/base ./packages/i18n-lang/src ./ && \
	bash ./scripts/l10n.sh ./packages/i18n-lang/src ./packages/l10n/gen ./packages/l10n/base ../init-settings/l10n-apitable-ee ./ && \
	pnpm run build:i18n

################################################################ make edition

define EDITION_TXT

`make edition` is a command to setup the specific edition environment.

Current Edition: $(shell if [ -a .devenv-edition ]; then cat .devenv-edition; else echo 'apitable-ce(default)';fi)

What edition do you want to setup?

  1) apitable-ce: (unlink) APITable Community Edition, Open-Source.[default]
  2) apitable-ee: (ln -s) APITable Enterprise Edition, also SaaS deployment.
  3) vika-ee: (ln -s) vika.cn Enterprise Edition, also SaaS deployment.
  4) vika-ee-rsync: (rsync) vika.cn Enterprise Edition, also SaaS deployment. Use for docker build and github action
  5) apitable-ee-rsync: (rsync) APITable Enterprise Edition, also SaaS deployment. Use for docker build and github action
  6) vika-ee-copy: (copy) vika.cn Enterprise Edition, also SaaS deployment. Use for docker build and github action
  7) apitable-ee-copy: (copy) APITable Enterprise Edition, also SaaS deployment. Use for docker build and github action
  8) edition-settings: custom settings edition for any customers.
  9) cancellation: cancellation of edition development.

endef
export EDITION_TXT

.PHONY: e
e: ## make e = make edition + make env + make edition-settings
	@echo 'make edition & make env'
	make edition
	make env
	make edition-settings

.PHONY: edition
edition: ## setup specific edition environment
	@echo "$$EDITION_TXT" ;\
	read -p "NUMBER>>" NUMBER ;\
	if [ "$$NUMBER" = "1" ]; then make _edition-apitable-ce; fi ;\
	if [ "$$NUMBER" = "2" ]; then make _edition-apitable-ee; fi ;\
	if [ "$$NUMBER" = "3" ]; then make _edition-vika-ee; fi  ;\
	if [ "$$NUMBER" = "4" ]; then make _edition-vika-ee-rsync; fi  ;\
	if [ "$$NUMBER" = "5" ]; then make _edition-apitable-ee-rsync; fi  ;\
	if [ "$$NUMBER" = "6" ]; then make _edition-vika-ee-copy; fi  ;\
	if [ "$$NUMBER" = "7" ]; then make _edition-apitable-ee-copy; fi  ;\
	if [ "$$NUMBER" = "8" ]; then make edition-settings; fi ;\
	if [ "$$NUMBER" = "9" ]; then make _enterprise-unlink; fi

_edition-apitable-ce: _enterprise-unlink _enterprise-empty
	@echo 'apitable-ce' > .devenv-edition
	@echo 'apitable' > .distro
	@echo 'EDITION=apitable-ce' > .edition

_edition-apitable-ee: _enterprise-unlink _enterprise-link
	@echo 'apitable-ee' > .devenv-edition
	@echo 'apitable' > .distro
	@echo 'EDITION=apitable-ee' > .edition
	@echo 'apitable-saas' > enterprise/.env

_edition-apitable-ee-rsync: _enterprise-rsync
	@echo 'apitable-ee-rsync' > .devenv-edition
	@echo 'apitable' > .distro
	@echo 'EDITION=apitable-ee' > .edition
_edition-apitable-ee-copy: _enterprise-copy
	@echo 'apitable-ee-copy' > .devenv-edition
	@echo 'apitable' > .distro
	@echo 'EDITION=apitable-ee' > .edition

_edition-vika-ee: _enterprise-unlink _enterprise-link
	@echo 'vika-ee' > .devenv-edition
	@echo 'vika' > .distro
	@echo 'EDITION=vika-ee' > .edition
	@echo 'vika-saas' > enterprise/.env

_edition-vika-ee-rsync: _enterprise-rsync
	@echo 'vika-ee-rsync' > .devenv-edition
	@echo 'vika' > .distro
	@echo 'EDITION=vika-ee' > .edition

_edition-vika-ee-copy: _enterprise-copy
	@echo 'vika-ee-copy' > .devenv-edition
	@echo 'vika' > .distro
	@echo 'EDITION=vika-ee' > .edition

_enterprise-unlink: ## delete or unlink all enterprise codes from apitable
	@rm -rf apitable/packages/core/src/modules/enterprise
	@rm -rf apitable/packages/room-server/src/enterprise
	@rm -rf apitable/packages/datasheet/src/modules/enterprise
	@rm -rf apitable/packages/i18n-lang/src/enterprise
	@rm -rf apitable/backend-server/application/src/main/java/com/apitable/enterprise
	@rm -rf apitable/backend-server/application/src/main/resources/enterprise
	@rm -rf apitable/backend-server/application/src/main/resources/templates/notification/enterprise
	@rm -rf apitable/backend-server/application/src/test/java/com/apitable/enterprise
	@rm -rf apitable/backend-server/application/src/test/resources/enterprise
	@rm -rf apitable/scripts/protos/enterprise
	@rm -rf apitable/backend-server/application/src/main/proto/enterprise
	@rm -rf apitable/packages/air-agent
	@rm -rf apitable/packages/ai-components/src
	cat env/integration.env.web.template > apitable/packages/datasheet/.env.local
ifeq ($(UNAME_S),Darwin)
	@sed -i '' '/IS_ENTERPRISE=true/d' apitable/packages/datasheet/.env.local
	@sed -i '' '/IS_ENTERPRISE=true/d' apitable/packages/datasheet/.env
	@sed -i '' '/ENABLE_HOCUSPOCUS=true/d' apitable/.env.devenv
else ifeq ($(UNAME_S),Linux)
	@sed -i '/IS_ENTERPRISE=true/d' apitable/packages/datasheet/.env.local
	@sed -i '/IS_ENTERPRISE=true/d' apitable/packages/datasheet/.env
	@sed -i '/ENABLE_HOCUSPOCUS=true/d' apitable/.env.devenv
endif
	@echo 'Unlink all enterprise folder'

_enterprise-link: ## make symlink use for local development environment
	ln -s $$PWD/enterprise/room-server apitable/packages/room-server/src/enterprise
	ln -s $$PWD/enterprise/backend-server/main/java apitable/backend-server/application/src/main/java/com/apitable/enterprise
	ln -s $$PWD/enterprise/backend-server/main/resources apitable/backend-server/application/src/main/resources/enterprise
	ln -s $$PWD/enterprise/email-templates apitable/backend-server/application/src/main/resources/templates/notification/enterprise
	ln -s $$PWD/enterprise/backend-server/test/java apitable/backend-server/application/src/test/java/com/apitable/enterprise
	ln -s $$PWD/enterprise/backend-server/test/resources apitable/backend-server/application/src/test/resources/enterprise
	ln -s $$PWD/enterprise/scripts/protos apitable/scripts/protos/enterprise
	ln -s $$PWD/enterprise/scripts/protos apitable/backend-server/application/src/main/proto/enterprise
	ln -s $$PWD/enterprise/core apitable/packages/core/src/modules/enterprise
	ln -s $$PWD/enterprise/datasheet apitable/packages/datasheet/src/modules/enterprise
	ln -s $$PWD/enterprise/air-agent apitable/packages/air-agent
	ln -s $$PWD/enterprise/ai-components/src apitable/packages/ai-components/src
	@printf "\nIS_ENTERPRISE=true" >> apitable/packages/datasheet/.env.local
	@printf "\nIS_ENTERPRISE=true" >> apitable/packages/datasheet/.env
	@printf "\nENABLE_HOCUSPOCUS=true" >> apitable/.env.devenv
	@echo 'Linked all enterprise folder'

define SMART_RSYNC # args: $1-source, $2-destination
	@if [ -d $2 ]; then \
		echo "Directory exists, reverse sync"; \
		rsync -avz --update --delete $2 $1 ;\
	else \
		echo "Directory does not exist, create $2"; \
		mkdir -p $2 ;\
	fi ;\
	rsync -avz --update --delete $1 $2
endef

_enterprise-rsync:
	if [ -e $$PWD/apitable/packages/core/src/modules/enterprise/index.ts ]; then \
		rm -rf $$PWD/apitable/packages/core/src/modules/enterprise ;\
	fi
	if [ -e $$PWD/apitable/packages/i18n-lang/src/enterprise/generateLang.ts ]; then \
		rm -rf $$PWD/apitable/packages/i18n-lang/src/enterprise/ ;\
	fi
	$(call SMART_RSYNC,$$PWD/enterprise/core/,$$PWD/apitable/packages/core/src/modules/enterprise/)
	$(call SMART_RSYNC,$$PWD/enterprise/room-server/,$$PWD/apitable/packages/room-server/src/enterprise/)
	$(call SMART_RSYNC,$$PWD/enterprise/datasheet/,$$PWD/apitable/packages/datasheet/src/modules/enterprise/)
	$(call SMART_RSYNC,$$PWD/enterprise/backend-server/main/java/,$$PWD/apitable/backend-server/application/src/main/java/com/apitable/enterprise/)
	$(call SMART_RSYNC,$$PWD/enterprise/backend-server/main/resources/,$$PWD/apitable/backend-server/application/src/main/resources/enterprise/)
	$(call SMART_RSYNC,$$PWD/enterprise/email-templates/,$$PWD/apitable/backend-server/application/src/main/resources/templates/notification/enterprise/)
	$(call SMART_RSYNC,$$PWD/enterprise/backend-server/test/java/,$$PWD/apitable/backend-server/application/src/test/java/com/apitable/enterprise/)
	$(call SMART_RSYNC,$$PWD/enterprise/backend-server/test/resources/,$$PWD/apitable/backend-server/application/src/test/resources/enterprise/)
	$(call SMART_RSYNC,$$PWD/enterprise/scripts/protos/,$$PWD/apitable/scripts/protos/enterprise/)
	$(call SMART_RSYNC,$$PWD/enterprise/scripts/protos/,$$PWD/apitable/backend-server/application/src/main/proto/enterprise/)
	$(call SMART_RSYNC,$$PWD/enterprise/air-agent/,$$PWD/apitable/packages/air-agent/)
	$(call SMART_RSYNC,$$PWD/enterprise/ai-components/src/,$$PWD/apitable/packages/ai-components/src/)
	@printf "\nIS_ENTERPRISE=true" >> apitable/packages/datasheet/.env.local
	@printf "\nENABLE_HOCUSPOCUS=true" >> apitable/.env.devenv
	@echo 'rsync all enterprise folder'

_enterprise-copy: _enterprise-unlink ## copy enterprise folder to apitable, it is useful for Github Action and Dockerfile (build docker COPY command doesn't suport follow symlinks)
	cp -rf $$PWD/enterprise/core apitable/packages/core/src/modules/enterprise
	cp -rf $$PWD/enterprise/room-server apitable/packages/room-server/src/enterprise
	cp -rf $$PWD/enterprise/datasheet apitable/packages/datasheet/src/modules/enterprise
	cp -rf $$PWD/enterprise/backend-server/main/java apitable/backend-server/application/src/main/java/com/apitable/enterprise
	cp -rf $$PWD/enterprise/backend-server/main/resources apitable/backend-server/application/src/main/resources/enterprise
	cp -rf $$PWD/enterprise/email-templates apitable/backend-server/application/src/main/resources/templates/notification/enterprise
	cp -rf $$PWD/enterprise/backend-server/test/java apitable/backend-server/application/src/test/java/com/apitable/enterprise
	cp -rf $$PWD/enterprise/backend-server/test/resources apitable/backend-server/application/src/test/resources/enterprise
	cp -rf $$PWD/enterprise/scripts/protos apitable/scripts/protos/enterprise
	cp -rf $$PWD/enterprise/scripts/protos apitable/backend-server/application/src/main/proto/enterprise
	cp -rf $$PWD/enterprise/air-agent apitable/packages/air-agent
	cp -rf $$PWD/enterprise/ai-components/src apitable/packages/ai-components/src
	@printf "\nIS_ENTERPRISE=true" >> apitable/packages/datasheet/.env.local
	@printf "\nIS_ENTERPRISE=true" >> apitable/packages/datasheet/.env
	@echo 'Copy all vika saas folder'

_enterprise-empty:
	@mkdir apitable/packages/core/src/modules/enterprise
	@cp $$PWD/enterprise/core/index-ce.ts apitable/packages/core/src/modules/enterprise/index.ts
	@mkdir apitable/packages/i18n-lang/src/enterprise
	@echo "export {};" >apitable/packages/i18n-lang/src/enterprise/index.ts

_edition-custom:
	@echo 'TODO'


############# build and push

buildpush-webserver-vika: ## ghcr.io/vikadata/vika/web-server
	cd apitable ;\
	eval "$$(curl -fsSL https://vikadata.github.io/semver_ci.sh)";\
	source ../scripts/build_web.sh build_saas_vika

buildpush-webserver-apitable:
	cd apitable ;\
	eval "$$(curl -fsSL https://vikadata.github.io/semver_ci.sh)";\
	source ../scripts/build_web.sh build_saas_apitable

buildpush-webserver-op: ## ghcr.io/vikadata/vika/web-server
	cd apitable ;\
	eval "$$(curl -fsSL https://vikadata.github.io/semver_ci.sh)";\
  	source ../scripts/build_web.sh build_op

buildpush-airagent-web: ## ghcr.io/vikadata/vika/airagent-web
	cd apitable ;\
	eval "$$(curl -fsSL https://vikadata.github.io/semver_ci.sh)";\
  	source ../scripts/build_airagent_web.sh build_airagent_web

# for open source project
buildpush-init-db: ## build and push the `init-db`container
	cd apitable/init-db ;\
	eval "$$(curl -fsSL https://vikadata.github.io/semver_ci.sh)"; \
	build_docker init-db

# actually it was init-db-enterprise
buildpush-init-db-enterprise: ## build and push the `init-db`container
	cd enterprise/init-db ;\
	eval "$$(curl -fsSL https://vikadata.github.io/semver_ci.sh)"; \
	build_docker init-db-enterprise

buildpush-roomserver: ## ghcr.io/vikadata/vika/room-server
	eval "$$(curl -fsSL https://vikadata.github.io/semver_ci.sh)";\
	cd apitable ;\
	export DOCKERFILE=./packaging/Dockerfile.room-server;\
	build_docker room-server

buildpush-componentdoc: ## ghcr.io/vikadata/vika/component-doc
	eval "$$(curl -fsSL https://vikadata.github.io/semver_ci.sh)"; \
	cd apitable ;\
	build_docker component-doc


# export DOCKERFILE=./packaging/Dockerfile.backend-server;\
# build_docker backend-server

buildpush-backendserver: ## ghcr.io/vikadata/vika/backend-server
	eval "$$(curl -fsSL https://vikadata.github.io/semver_ci.sh)";\
	cd apitable ;\
	env_dotversion;\
	export BUILD_ARG="--build-arg BUILD_VERSION=$${SEMVER_FULL} --build-arg JAR_PATH=backend-server/application/build/libs/*.jar --build-arg PORT=8081"; \
	export DOCKERFILE=./packaging/Dockerfile.backend-server; \
	build_docker backend-server

buildpush-dingtalkserver:
	eval "$$(curl -fsSL https://vikadata.github.io/semver_ci.sh)"; \
	cd enterprise/customers/vika-saas/dingtalk-server ;\
	export BUILD_ARG="--build-arg JAR_PATH=enterprise/customers/vika-saas/dingtalk-server/build/libs/*.jar --build-arg PORT=9091 --build-arg PORT=9092"; \
	build_docker dingtalk-server

buildpush-space-job-executor:
	eval "$$(curl -fsSL https://vikadata.github.io/semver_ci.sh)"; \
	cd enterprise/customers/vika-saas/scheduler/space-job ;\
	export BUILD_ARG="--build-arg JAR_PATH=enterprise/customers/vika-saas/scheduler/space-job/build/libs/*.jar --build-arg PORT=9111"; \
	build_docker space-job-executor

######################################## init-db
local-db-apply: ## init-db update database structure (use .env)
	cd apitable/init-db ;\
	docker build -f Dockerfile . --tag=apitable/init-db
	docker run --rm --env-file $$ENV_FILE \
		-e DB_HOST=mysql -e DB_PORT=3306 -e DB_NAME=apitable \
		-e DB_USERNAME=root -e DB_PASSWORD=apitable@com \
		-e ACTION=update \
		--network apitable_default apitable/init-db

INIT_DB_DOCKER_PATH=apitable/init-db-enterprise
local-db-vika-enterprise-apply: ## init-db update database structure (use .env)
	cd enterprise/init-db ;\
	docker build -f Dockerfile . --tag=${INIT_DB_DOCKER_PATH}
	docker run --rm --env-file $$ENV_FILE \
		-e DB_HOST=mysql -e DB_PORT=3306 -e DB_NAME=apitable \
		-e DB_USERNAME=root -e DB_PASSWORD=apitable@com \
		-e EDITION=vika-saas -e ACTION=update \
		--network apitable_default ${INIT_DB_DOCKER_PATH}

local-db-apitable-enterprise-apply: ## init-db for apitable update database structure (use .env)
	cd enterprise/init-db ;\
	docker build -f Dockerfile . --tag=${INIT_DB_DOCKER_PATH}
	docker run --rm --env-file $$ENV_FILE \
		-e DB_HOST=mysql -e DB_PORT=3306 -e DB_NAME=apitable \
		-e DB_USERNAME=root -e DB_PASSWORD=apitable@com \
		-e EDITION=apitable-saas -e ACTION=update \
		--network apitable_default ${INIT_DB_DOCKER_PATH}

db_adminer:
	docker-compose --env-file $$ENV_FILE -f enterprise/dockers/docker-compose.yaml up -d adminer

db_edition_apply:
	@ENTERPRISE_EDITION=$(shell cat enterprise/.env) ;\
	if [ -n "$$ENTERPRISE_EDITION" ]; then \
			echo "apply enterprise db changeset" ;\
			docker-compose --env-file $$ENV_FILE -f enterprise/dockers/docker-compose.yaml run \
				-u $(shell id -u):$(shell id -g) \
				--rm init-db-vika-saas ;\
			docker-compose --env-file $$ENV_FILE -f enterprise/dockers/docker-compose.yaml run \
				-u $(shell id -u):$(shell id -g) \
				--rm init-db-apitable-saas ;\
	fi

### scripts
# sync-design-token:
# 	python3 ./scripts/design_token/sync_design_token.py
# sync-icon:
# 	python3 ./scripts/design_token/make_icon.py && yarn workspace @vikadata/icons format
# sync-color:
# 	python3 ./scripts/design_token/sync_colors.py
# make-changelog:
# 	python3 ./scripts/changelog/make_changelog.py
# sync-colors:
# 	python3 ./scripts/design_token/sync_colors.py
# sync-icons:
# 	python3 ./scripts/design_token/make_icon.py && yarn workspace @vikadata/icons format

# makeconfig:
# 	make makeconfig-json && make makeconfig-tscode
# 	cd apitable ;\
# 	yarn build:i18n
# makeconfig-private:
# 	REACT_APP_DEPLOYMENT_MODELS=PRIVATE make makeconfig

# update-design:
# 	sh ./scripts/upload_design.sh
# generate-colors:
# 	ts-node ./scripts/generate_color.ts

test: ## do test
	@echo "TIPS: ln -s have some symlinks problem with Jest, please use 'make edition' switch to xxx-copy mode edition"
	cd apitable ;\
	make test

build: ## do build
	cd apitable ;\
	make build


ADDLICENSE := docker run -it --rm -v $(shell pwd):/src ghcr.io/google/addlicense
addlicense: ## add licenses to code files
	$(ADDLICENSE) -c "APITable Ltd. <https://apitable.com>" -f licenses/header-apitable-agpl3.txt "apitable/backend-server/"
	$(ADDLICENSE) -c "APITable Ltd. <https://apitable.com>" -f licenses/header-apitable-agpl3.txt "apitable/init-db/"
	$(ADDLICENSE) -c "APITable Ltd. <https://apitable.com>" -f licenses/header-apitable-agpl3.txt "apitable/scripts/"
	$(ADDLICENSE) -c "APITable Ltd. <https://apitable.com>" -f licenses/header-apitable-agpl3.txt "apitable/packages/"
	$(ADDLICENSE) -c "APITable Ltd. <https://apitable.com>" -f licenses/header-apitable-enterprise.txt ./enterprise
	$(ADDLICENSE) -c "APITable Ltd. <https://apitable.com>" -f licenses/header-apitable-enterprise.txt ./init-settings
# $(ADDLICENSE) -c "Vika, inc. <https://vikadata.com>" -f licenses/header-vika-enterprise.txt ./enterprise
# $(ADDLICENSE) -c "Vika, inc. <https://vikadata.com>" -f licenses/header-vika-enterprise.txt ./init-db

changelog: ## make changelog with github api
	@read -p "GITHUB_TOKEN: " GITHUB_TOKEN;\
	read -p "FROM[default:latest-tag]: " GIT_FROM ;\
	read -p "TO[default:HEAD]: " GIT_TO ;\
	if [ "$$GIT_FROM" = "" ]; then GIT_FROM=$(shell git describe --tags $$(git rev-list --tags --max-count=1)) ; fi ;\
	if [ "$$GIT_TO" = "" ]; then GIT_TO=HEAD ; fi ;\
	echo "" ;\
	echo "FROM: $$GIT_FROM" ;\
	echo "TO: $$GIT_TO" ;\
	npx github-changelog-builder --token $$GITHUB_TOKEN -o vikadata -r vikadata -f $$GIT_FROM -t $$GIT_TO -a CHANGELOG.md

clean: ## clean and delete git files
	@read -p "Are you sure to clean and delete?(yes/no) " ANSWER;\
 	if [ "$$ANSWER" = "yes" ]; then git clean -fxd; fi


### backend-server Makefile


###### 【backend server unit test】 ######

_test_clean: ## clean the docker in test step
	cd apitable/backend-server ;\
	docker rm -fv $$(docker ps -a --filter "name=test-.*-"$${CI_GROUP_TAG:-0} --format "{{.ID}}") || true

_test_dockers: ## run depends container in test step
	cd apitable/backend-server ;\
	docker-compose -f ../docker-compose.unit-test.yaml run -d --name test-mysql-$${CI_GROUP_TAG:-0} test-mysql ;\
	docker-compose -f ../docker-compose.unit-test.yaml run -d --name test-redis-$${CI_GROUP_TAG:-0} test-redis ;\
	docker-compose -f ../docker-compose.unit-test.yaml run -d --name test-rabbitmq-$${CI_GROUP_TAG:-0} test-rabbitmq

_test_init_db: ## Initialize the apitable ce database in test step
	cd apitable ;\
	docker-compose -f ./docker-compose.unit-test.yaml run --rm \
		-e DB_HOST=test-mysql-$${CI_GROUP_TAG:-0} test-init-db

_test_init_db_vika_saas: ## Initialize the vika saas database in test step
	docker-compose -f ./enterprise/dockers/docker-compose.unit-test.yaml run --rm \
		-e DB_HOST=test-mysql-$${CI_GROUP_TAG:-0} test-init-db-vika-saas

_test_init_db_apitable_saas: ## Initialize the apitable saas database in test step
	docker-compose -f ./enterprise/dockers/docker-compose.unit-test.yaml run --rm \
		-e DB_HOST=test-mysql-$${CI_GROUP_TAG:-0} test-init-db-apitable-saas

_test_backend_unit_test: ## unittest and codecov upload
	cd apitable/backend-server ;\
	docker-compose -f ../docker-compose.unit-test.yaml run -u $(shell id -u):$(shell id -g) --rm \
		-e MYSQL_HOST=test-mysql-$${CI_GROUP_TAG:-0} \
		-e REDIS_HOST=test-redis-$${CI_GROUP_TAG:-0} \
		-e RABBITMQ_HOST=test-rabbitmq-$${CI_GROUP_TAG:-0} \
		-e BACKEND_GRPC_PORT=-1 \
		unit-test-backend

_test_codecov:
	if [ "$$(uname -m)" = "aarch64" ]; then distro=aarch64; else distro=linux; fi; \
	curl "https://uploader.codecov.io/v0.5.0/$$distro/codecov" -o /tmp/codecov ;\
	chmod +x /tmp/codecov
ifndef CODECOV_TOKEN
	read -p "Please enter CODECOV_TOKEN: " CODECOV_TOKEN ;\
	/tmp/codecov -f apitable/backend-server/code-coverage-report/build/reports/jacoco/testCodeCoverageReport/testCodeCoverageReport.xml -t $$CODECOV_TOKEN
endif
ifdef CODECOV_TOKEN
	/tmp/codecov -f apitable/backend-server/code-coverage-report/build/reports/jacoco/testCodeCoverageReport/testCodeCoverageReport.xml -t $$CODECOV_TOKEN
endif

###### 【backend server enterprise unit test】 ######

test-ut-backend-docker-enterprise:
	@echo "$$(docker-compose --version)"
	make _test_clean
	make _test_dockers
	sleep 20
	make _test_init_db
	make _test_init_db_vika_saas
	make _test_init_db_apitable_saas
	make _test_backend_unit_test
	make _test_codecov
	@echo "finished unit test，clean up images..."
	make _test_clean

###### 【backend server enterprise unit test】 ######

###### 【room server enterprise unit test】 ######
_test_room_unit_test: ## unittest and codecov upload
	cd apitable ;\
	docker compose -f docker-compose.unit-test.yaml build unit-test-room ;\
	docker compose -f docker-compose.unit-test.yaml run --rm \
		-e MYSQL_HOST=test-mysql-$${CI_GROUP_TAG:-0} \
		-e REDIS_HOST=test-redis-$${CI_GROUP_TAG:-0} \
		-e RABBITMQ_HOST=test-rabbitmq-$${CI_GROUP_TAG:-0} \
    		unit-test-room pnpm run test:ut:room:cov

test-ut-room-docker-enterprise:
	@echo "$$(docker-compose --version)"
	make _test_clean
	make _test_dockers
	sleep 20
	make _test_init_db
	make _test_init_db_vika_saas
	make _test_init_db_apitable_saas
	make _test_room_unit_test
	@echo "finished room unit test，clean up images..."
	make _test_clean
###### 【room server enterprise unit test】 ######

############# APITable open source project

define APITABLE_TXT

`make apitable` is a command to help process with the APITable open source project.

RUN THIS COMMAND IN `develop` branch only !!!
DO NOT PULL REQUEST THIS COMMAND RESULT !!!

What do you want to setup?

  1) pull: pull the APITable `develop` branch to local subtree.
  2) copy: copy local `apitable/` to `../apitable` folder.
  3) push: push the subtree `apitable/` to APITable `sync/hosted` branch.
  4) init: init the APITable remote and subtree
  5) add: add the APITable remote and subtree (DO NOT USE)
  6) copy2: copy parent `../apitable/` to local `apitable` folder. (DO NOT USE)

endef
export APITABLE_TXT
.PHONY: apitable
apitable: ## help process with the APITable open source project.
	@echo "$$APITABLE_TXT" ;\
	read -p "NUMBER>>" NUMBER ;\
	if [ "$$NUMBER" = "1" ]; then git subtree pull --prefix=apitable apitable develop; fi ;\
	if [ "$$NUMBER" = "2" ]; then rsync -rtvP --delete --exclude .git apitable/ ../apitable; fi  ;\
	if [ "$$NUMBER" = "3" ]; then git subtree push --prefix=apitable apitable sync/hosted; fi  ;\
	if [ "$$NUMBER" = "4" ]; then git remote remove apitable || true; git remote add -f apitable git@github.com:apitable/apitable.git; fi ;\
	if [ "$$NUMBER" = "5" ]; then git subtree add --prefix=apitable apitable develop; fi ;\
	if [ "$$NUMBER" = "6" ]; then rsync -rtvP --delete --exclude .git ../apitable/ apitable; fi


wizard: ## wizard update
	npx ts-node ./scripts/wizard/wizard-update.ts

### help
.PHONY: search
search:
	@echo " "
	echo  && grep -E "$$s.*?## .*" $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}';
	@echo ' ';
	@read -p "What do you want?>>" command; \
	make $$command;

.PHONY: help
help:
	@echo "$$ANNOUNCE_BODY"
	@echo ' ';
	@grep -E '^[0-9a-zA-Z-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}';
	@echo '  '
	@read -p "What do you want?>> " command; \
	make $$command;

