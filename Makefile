.PHONY: up build down logs logs-back logs-front dev dev-back dev-front dev-down test

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
	docker compose -f docker-compose.dev.yml run --rm backend mvn test

    #Todos los scripts
	docker compose -f docker-compose.test.yml run --rm loadtest

    #Un solo script
    docker compose -f docker-compose.test.yml run --rm loadtest sugerencias.js