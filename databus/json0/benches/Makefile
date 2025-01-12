

bench:
	@echo '!! Benchmarking JavaScript json0...'
	@python3 -m timeit -v -s 'import subprocess' 'subprocess.call(["node", "test_json0.js"])' 
	@echo 'Benchmarking Rust json0...'
	@python3 -m timeit -v -s 'import subprocess' 'subprocess.call(["./target/debug/benches"])' 




build:
	cargo build

install:
	npm install
	cargo build
