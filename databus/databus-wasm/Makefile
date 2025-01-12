

setup:
	# install wasm-pack
	npm install -g wasm-pack
	cargo install cargo-generate
	rustup target add wasm32-wasi
	

test:
	wasm-pack test --node
	wasm-pack test --chrome
	wasm-pack test --safari
	wasm-pack test --firefox


build-wasi:
	cargo build --target wasm32-wasi
	
build:
	wasm-pack build --target bundler --out-dir pkg/bundler
	wasm-pack build --target nodejs --out-dir pkg/nodejs
	wasm-pack build --target web --out-dir pkg/web

build_web:
	wasm-pack build --target web --out-dir pkg/web

install: build # deploy & embed wasm to 
	rm -rf pkg/web/.gitignore
	rm -rf pkg/nodejs/.gitignore
	rm -rf pkg/bundler/.gitignore
	rsync -avz --exclude 'package.json' pkg/web/ ../../apitable/packages/databus-wasm-web
	rsync -avz --exclude 'package.json' pkg/bundler/  ../../apitable/packages/databus-wasm-nodejs/esm
	rsync -avz --exclude 'package.json' pkg/nodejs/  ../../apitable/packages/databus-wasm-nodejs/cjs
