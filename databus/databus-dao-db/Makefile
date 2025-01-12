


entities: ## for temporary generate entities, and refactor into src/
	@read -p "Enter the password of integration mysql: " var; \
	RUST_BACKTRACE=1 sea-orm-cli generate entity \
    -u mysql://vika_reader:$$var@localhost:33306/vikadata \
    -o tests/entities \
		-t vika_unit