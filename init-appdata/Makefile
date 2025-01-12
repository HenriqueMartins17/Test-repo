.DEFAULT_GOAL := help
SHELL := /bin/bash

install: ## install dependencies and build project
	./gradlew clean build -x test
	@echo 'Install Finished'

define EDITION_TEXT
Which edition do you want?
  1) apitable-ce
  2) apitable-ee
  3) apitable-saas
  4) vika-ce(not configured)
  5) vika-ee
  6) vika-saas
  7) vika-compute-nest
endef
export EDITION_TEXT

buildpush: ## generate data and build docker
	make _generator && make _build_docker

_build_docker:
	eval "$$(curl -fsSL https://vikadata.github.io/semver_ci.sh)" ;\
	build_docker_unableack dotversion init-appdata

switch: ## switch edition(= generator + loader)
	make generator; make loader;

_unlink-sysconfig:
	@rm -rf src/main/resources/sysconfig

_link-apitable-ce-sysconfig: _unlink-sysconfig
	ln -s $$PWD/../enterprise/init-appdata/sysconfig/apitable/ce src/main/resources/sysconfig
	@echo 'Linked all apitable ce sysconfig'

_link-apitable-ee-sysconfig: _unlink-sysconfig
	ln -s $$PWD/../enterprise/init-appdata/sysconfig/apitable/ee src/main/resources/sysconfig
	@echo 'Linked all apitable ee sysconfig'

_link-apitable-saas-sysconfig: _unlink-sysconfig
	ln -s $$PWD/../enterprise/init-appdata/sysconfig/apitable/saas src/main/resources/sysconfig
	@echo 'Linked all apitable saas sysconfig'

_link-vika-ce-sysconfig: _unlink-sysconfig
	ln -s $$PWD/../enterprise/init-appdata/sysconfig/vika/ce src/main/resources/sysconfig
	@echo 'Linked all vika ce sysconfig'

_link-vika-ee-sysconfig: _unlink-sysconfig
	ln -s $$PWD/../enterprise/init-appdata/sysconfig/vika/ee src/main/resources/sysconfig
	@echo 'Linked all vika ee sysconfig'

_link-vika-compute-nest-sysconfig: _unlink-sysconfig
	ln -s $$PWD/../enterprise/init-appdata/sysconfig/vika/compute-nest src/main/resources/sysconfig
	@echo 'Linked all vika compute nest sysconfig'

_link-baidu-ruliu-sysconfig: _unlink-sysconfig
	ln -s $$PWD/../enterprise/init-appdata/sysconfig/vika/ruliu src/main/resources/sysconfig
	@echo 'Linked all ruliu sysconfig'

_env-apitable-ce:
	cat env/generator.env.template > .env
	cat env/apitable-ce.env.template >> .env

_env-apitable-ee:
	cat env/generator.env.template > .env
	cat env/apitable-ee.env.template >> .env

_env-apitable-saas:
	cat env/generator.env.template > .env
	cat env/apitable-saas.env.template >> .env

_env-vika-ce:
	cat env/generator.env.template > .env
	cat env/vika-ce.env.template >> .env

_env-vika-ee:
	cat env/generator.env.template > .env
	cat env/vika-ee.env.template >> .env

_env-vika-saas:
	cat env/generator.env.template > .env
	cat env/vika-saas.env.template >> .env

_env-vika-compute-nest:
	cat env/generator.env.template > .env
	cat env/vika-compute-nest.env.template >> .env

_env-baidu-ruliu:
	cat env/generator.env.template > .env
	cat env/baidu-ruliu.env.template >> .env

.PHONY: generator
generator: ## generate data source data to local file
	@echo "$$EDITION_TEXT"
	read -p "NUMBER>>" NUMBER ;\
	if [ "$$NUMBER" = "" ]; then make _env-apitable-ce; make _link-apitable-ce-sysconfig; fi ;\
	if [ "$$NUMBER" = "1" ]; then make _env-apitable-ce; make _link-apitable-ce-sysconfig; fi ;\
	if [ "$$NUMBER" = "2" ]; then make _env-apitable-ee; make _link-apitable-ee-sysconfig; fi ;\
	if [ "$$NUMBER" = "3" ]; then make _env-apitable-saas; make _link-apitable-saas-sysconfig; fi ;\
	if [ "$$NUMBER" = "4" ]; then make _env-vika-ce; make _link-vika-ce-sysconfig; fi ;\
	if [ "$$NUMBER" = "5" ]; then make _env-vika-ee; make _link-vika-ee-sysconfig; fi ;\
	if [ "$$NUMBER" = "6" ]; then make _env-vika-saas; make _link-vika-saas-sysconfig; fi ;\
	if [ "$$NUMBER" = "7" ]; then make _env-vika-compute-nest; make _link-vika-compute-nest-sysconfig; fi
	make install
	source scripts/export-env.sh .env;\
	java -jar ./build/libs/init-appdata.jar generate
	@echo 'Generate Finish'

_generator: ## generate data source data to local file
	source scripts/export-env.sh .env;\
	java -jar ./build/libs/init-appdata.jar generate
	@echo 'Generate Finish'

.PHONY: loader
loader: ## load local file to target db and oss
	cat env/loader.env.template > .env
	source scripts/export-env.sh .env;\
	java -jar ./build/libs/init-appdata.jar load
	@echo 'Load Finish'

.PHONY: init-config-space
init-config-space: ## create configuration center space
	cat env/initializer.env.template > .env
	source scripts/export-env.sh .env;\
	java -jar ./build/libs/init-appdata.jar init-config-space
	@echo 'Init Config Space Finish';\

.PHONY: init-user
init-user: ## create initialization account
	cat env/initializer.env.template > .env
	source scripts/export-env.sh .env;\
	java -jar ./build/libs/init-appdata.jar init-user
	@echo 'Init User Finish'

.PHONY: help build
help:
	@echo ' ';
	@grep -E '^[0-9a-zA-Z-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}';
	@echo '  '
	@read -p "What do you want?>> " command; \
	make $$command;
