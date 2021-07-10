.DEFAULT_GOAL := help
SHELL := /bin/bash

.PHONY: help
help:
	@cat $(MAKEFILE_LIST) | grep -E '^[a-zA-Z_-]+:.*?## .*$$' | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

.PHONY: bootstrap
bootstrap: ## Install build dependencies
	mvn --batch-mode clean initialize

.PHONY: build
build: bootstrap ## Build project (dummy task for CI)

.PHONY: test
test: ## Run tests
	mvn --batch-mode clean test

.PHONY: integration-test
integration-test: ## Run integration tests
	mvn --batch-mode clean integration-test

.PHONY: bootstrap-with-docker
bootstrap-with-docker: ## Prepare the Docker builder image
	docker build -t notifications-java-client .
	./scripts/run_with_docker.sh make bootstrap

.PHONY: test-with-docker
test-with-docker: ## Run tests inside a Docker container
	./scripts/run_with_docker.sh make test

.PHONY: integration-test-with-docker
integration-test-with-docker: ## Run integration tests inside a Docker container
	./scripts/run_with_docker.sh make integration-test

clean:
	rm -rf .m2

.PHONY: get-client-version
get-client-version: ## Retrieve client version number from source code (https://stackoverflow.com/a/26514030)
	@mvn help:evaluate -Dexpression=project.version -q -DforceStdout
