SHELL := /bin/bash

docker_build_migration:
	eval "$$(curl -fsSL https://vikadata.github.io/semver_ci.sh)"; \
	export BUILD_ARG="--build-arg JAR_PATH=build/libs/*.jar --build-arg PORT=9113"; \
	export DOCKERFILE=Dockerfile; \
	build_docker migration-server
