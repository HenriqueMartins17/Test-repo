export RUSTFLAGS=-Aunused_variables -Aunused_imports -Adead_code -Aunused_must_use

install:
	cargo build

build:
	cargo build

dev: ## run development env with hot reload
	cargo watch -x run

run:
	cargo run

swagger: ## genereate databus-server python client
	openapi-generator generate -i http://localhost:8625/databus/api-docs/openapi.json -g python -c .swagger-codegen.config.json -o .venv/databus_client
	rsync -av .venv/databus_client/databus_client/ ./databus_client
	@echo 'databus client generated into `/databus_client`, feel free to import databus_client'



