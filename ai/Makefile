.DEFAULT_GOAL := help
SHELL := /bin/bash
UNAME_S := $(shell uname -s)
SEMVER3 := $(shell cat .version)

setup-linux: ## setup the poetry and python environment
	@if ! command -v poetry &> /dev/null; then\
		curl -sSL https://install.python-poetry.org | python3 -;\
	fi

setup-macos: ## setup the poetry and python environment
	@if ! command -v poetry &> /dev/null; then\
		curl -sSL https://install.python-poetry.org | python3 -;\
	fi
	brew install ngrok
	brew install openapi-generator
	brew install swagger-codegen

swagger: ## genereate databus-server python client
	openapi-generator generate -i https://integration.vika.ltd/databus/api-docs/openapi.json -g python -c .swagger-codegen.config.json -o .venv/databus_client
	rsync -av .venv/databus_client/databus_client/ ./databus_client
	@echo 'databus client generated into `/databus_client`, feel free to import databus_client'
#@echo 'databus client generated into .venv/databus_client, and setup install into venv, feel free to import databus_client'
#cd .venv/databus_client && poetry run python setup.py install

copilot: ## split documents and upload snippets to specified datasheet
	poetry run python -m scripts.embedding_copilot_docs -action load

copilot-test: ## test the copilot
	poetry run pytest -k test_copilot_

swagger_npm:
	npx @openapitools/openapi-generator-cli generate -i https://integration.vika.ltd/databus/api-docs/openapi.json -g python -c .swagger-codegen.config.json -o .venv/databus_client


define do_format
	cd $(1) && \
		poetry run black . && \
		poetry run isort . && \
		poetry run ruff . --fix
endef

.PHONY: format
format: ## formating all code
	$(call do_format,ai_server)
	$(call do_format,ai_shared)
	$(call do_format,ai_inference)
	$(call do_format,ai_trainers)
	$(call do_format,tests)

install-python:
	poetry install

install: install-python ## install poetry dependencies
	@echo "install successed"

.PHONY: test
test:
	poetry run pytest

run: ## run the AI server with uvicorn
	poetry run python -m ai_server

public: ## expose the local server 8626 (debug port) to the internet
	ngrok http 8626

pydoc: ## open pydoc
	poetry run python -m pydoc -b

env: ## create the .env file
	@cp .env.template .env ;\
	echo '.env file created!'



build: build-ai-server-base ## Build the docker image called `vika/ai-server`
	docker build  -f Dockerfile . --tag=vika/ai-server

build-nltkpy: ## build docker to apitable/nltkpy
	docker build -f Dockerfile.nltkpy . --tag=apitable/nltkpy

build-ai-server-base: ## build docker vika/ai-server-base for cache layout
	@[ -z "$$CR_PAT" ] && read -p "Enter the CR_PAT value: " CR_PAT ;\
	eval "$$(curl -fsSL https://vikadata.github.io/semver_ci.sh)" ;\
	source scripts/build_ai_server_base.sh


buildpush: build-ai-server-base ## build and push docker to ghcr.io/vikadata/vika/ai-server
	@[ -z "$$CR_PAT" ] && read -p "Enter the CR_PAT value: " CR_PAT ;\
	[ -z "$$REGISTRY_SERVER" ] && read -p "Enter the REGISTRY_SERVER value: " REGISTRY_SERVER ;\
	eval "$$(curl -fsSL https://vikadata.github.io/semver_ci.sh)" ;\
	build_docker ai-server

build-copilot: ## training the AI Copilot into `.data/aitable_copilot`
	@[ -z "$$EDITION" ] && read -p "Enter the EDITION value: " EDITION ;\
	poetry run python -m scripts.embedding_copilot_docs -action train

buildpush-copilot: build-copilot ## build copilot docker
	@[ -z "$$CR_PAT" ] && read -p "Enter the CR_PAT value: " CR_PAT ;\
	[ -z "$$REGISTRY_SERVER" ] && read -p "Enter the REGISTRY_SERVER value: " REGISTRY_SERVER ;\
	[ -z "$$COPILOT_CHROMA_FOLDER" ] && read -p "Enter the COPILOT_CHROMA_FOLDER value(vika_copilot or aitable_copilot): " COPILOT_CHROMA_FOLDER ;\
	eval "$$(curl -fsSL https://vikadata.github.io/semver_ci.sh)" ;\
	export BUILD_ARG="--build-arg COPILOT_CHROMA_FOLDER=${COPILOT_CHROMA_FOLDER}" ;\
	export DOCKERFILE="./Dockerfile.copilot" ;\
	build_docker copilot

.PHONY: help
help:
	@echo "$$ANNOUNCE_BODY"
	@echo ' ';
	@grep -E '^[0-9a-zA-Z-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}';
	@echo '  '
	@read -p "What do you want?>> " command; \
	make $$command;
