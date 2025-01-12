SHELL := /bin/bash

install:
	cargo build
	cd databus-wasm && make install

#build-node:
#	cd databus-nodejs && yarn build

.PHONY: docs
docs:
	cargo doc
	mkdir -p target/docs_pub
	cp -rf target/doc/databus_core target/docs_pub
	cp -rf target/doc/databus_wasm target/docs_pub
	cp -rf target/doc/static.files target/docs_pub
	cp -rf target/doc/json0 target/docs_pub
	echo "<a href='./databus_core/index.html'>databus_core</a> \
	<a href='./databus_python/index.html'>databus_python</a> \
	<a href='./databus_wasm/index.html'>databus_wasm</a> \
	<a href='./databus_nodejs/index.html'>databus_nodejs</a> \
	<a href='./databus_java/index.html'>databus_java</a> \
	<a href='https://integration.vika.ltd/databus/docs/'>databus_server</a>" \
	> target/docs_pub/index.html
opendocs: docs ## docs
	open target/docs_pub/index.html

build-python:
	poetry run maturin build -m databus-python/Cargo.toml -r -o ../ai/wheels

build:
	cargo build

run: ## run databus-server
	@echo "Running databus-server..."
	@echo "If you want to install databus-wasm, please use 'make install' instead."
	@echo " "
	cd databus-server; \
	make run

test: ## cargo test with log
	cargo test -- --nocapture

test_cov: ## cargo test with log
	CARGO_INCREMENTAL=0 RUSTFLAGS='-Cinstrument-coverage' LLVM_PROFILE_FILE='./target/llvm/cargo-test-%p-%m.profraw' cargo test
	grcov . --binary-path ./target/debug/deps/ -s . -t html --branch --ignore-not-existing --ignore '../*' --ignore "/*" -o target/coverage/html


test-network: ## cargo test with log and network (http)
	cargo test -- --nocapture --cfg test_network

test-ut:
	yarn test:ut:cov
	make test-ut-python

test-ut-python:
	poetry run maturin dev -m databus-python/Cargo.toml
	python databus-python/test/basic.py

build-node-ci:
	docker compose -f docker-compose.yaml run --rm build make copy-node-artifacts

copy-node-artifacts:
	cp *.node index.js index.d.ts ../apitable/packages/databus

test-ut-ci:
	docker compose -f docker-compose.yaml run --rm unit-test yarn test:ut:cov

clean-coverage:
	if [ -d "coverage" ]; then \
		sudo chown -R $(shell id -u):$(shell id -g) coverage; \
	fi
	if [ -d "target" ]; then \
		sudo chown -R $(shell id -u):$(shell id -g) target; \
	fi
	rm -fr coverage || true
	rm -fr target || true



dockers:
	@echo "Building databus-server docker..."
	docker build -f Dockerfile.databus-server .


buildpush: ## build and push docker to ghcr.io/vikadata/vika/databus-server
	@[ -z "$$CR_PAT" ] && read -p "Enter the CR_PAT value: " CR_PAT ;\
	[ -z "$$REGISTRY_SERVER" ] && read -p "Enter the REGISTRY_SERVER value: " REGISTRY_SERVER ;\
	eval "$$(curl -fsSL https://vikadata.github.io/semver_ci.sh)";\
	export DOCKERFILE=Dockerfile.databus-server;\
	build_docker databus-server

