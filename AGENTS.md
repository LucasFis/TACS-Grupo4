# Repository Guidelines

## Project Structure & Module Organization

This repository contains a full-stack sticker trading application. The Java backend lives in `backend/` and follows a Spring Boot layout: production code is under `backend/src/main/java/app`, tests under `backend/src/test/java/app`, and runtime settings under `backend/src/main/resources`. The React frontend lives in `frontend/`, with application code in `frontend/src`, static files in `frontend/public`, and reusable UI pieces grouped under `frontend/src/components`. Project docs and API examples are in root Markdown files and `postman_collection.json`.

## Build, Test, and Development Commands

Use the root `Makefile` for common workflows:

- `make dev`: starts the development stack with Docker Compose Watch.
- `make dev-back` / `make dev-front`: rebuilds or restarts only one service.
- `make dev-down`: stops the development stack.
- `make test`: runs backend Maven tests inside a disposable container.
- `make build`: builds and starts the production Compose stack.
- `make logs`, `make logs-back`, `make logs-front`: follows service logs.

For direct module work, run `mvn test` or `mvn verify` from `backend/`; `verify` also runs SpotBugs and JaCoCo checks. From `frontend/`, use `npm run dev`, `npm run build`, `npm run lint`, and `npm run preview`.

## Coding Style & Naming Conventions

Backend code targets Java 17 and Spring Boot 3. Use 2-space Java indentation, 4-space continuation indentation, UTF-8, and Unix line endings. Follow the existing Spanish domain naming: controllers use `Controlador...`, services use `Servicio...`, and repositories use `Repositorio...`.

Frontend code uses React with JSX modules. Keep component files in kebab-case paths such as `selector-repetidas.jsx`; hooks use `use...` names. ESLint is configured in `frontend/eslint.config.js`; address errors and avoid new `console` or unused-variable warnings.

## Testing Guidelines

Backend tests use JUnit 5, Mockito, Spring Boot Test, and embedded MongoDB. Place tests beside their layer under `backend/src/test/java/app`, ending class names with `Test`. JaCoCo enforces 80% package-level line coverage for non-excluded packages during `mvn verify`. Add or update tests for controller, service, repository, and domain behavior changes.

## Commit & Pull Request Guidelines

Recent history mixes short Spanish summaries with merge commits. Prefer concise, imperative commit messages that name the changed behavior, for example `agrega validacion de ofertas duplicadas`. PRs should describe the change, link the related issue, list validation commands run, and include screenshots or recordings for UI changes.

## Security & Configuration Tips

Copy `.env.example` to `.env` for local development. Do not commit secrets, production MongoDB URIs, JWT keys, or third-party API tokens. Keep CORS and service URLs configurable through environment variables.
