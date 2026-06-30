.PHONY: up build down logs logs-back logs-front dev dev-back dev-front dev-down test loadtest loadtest-script

# --- Prod ---

up:
	docker compose up --build -d

build:
	docker compose up --build -d

down:
	docker compose down

# --- Dev ---

dev:
	docker compose -f docker-compose.dev.yml up --build --watch

dev-back:
	docker compose -f docker-compose.dev.yml up --build backend

dev-front:
	docker compose -f docker-compose.dev.yml up --build --watch frontend

dev-down:
	docker compose -f docker-compose.dev.yml down

# -- Ambos

logs:
	docker compose logs -f

logs-back:
	docker compose logs -f backend

logs-front:
	docker compose logs -f frontend

# --- Tests ---

test:
	docker compose -f docker-compose.dev.yml run --rm --build backend mvn test

loadtest:
	docker compose -f docker-compose.test.yml run --rm --build loadtest

loadtest-script:
	docker compose -f docker-compose.test.yml run --rm --build loadtest $(SCRIPT)