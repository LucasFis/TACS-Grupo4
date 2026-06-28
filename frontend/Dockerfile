# --- Etapa de dependencias ---
# Instalamos paquetes
# lo usamos como base en dev
FROM node:22-alpine AS deps
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci

# --- Etapa de desarrollo ---
# Lo usamos para tener hot reload
FROM deps AS dev
COPY . .

# --- Etapa de build ---
# Compila la app con Vite para producción
FROM dev AS builder

ARG VITE_BACKEND_URI
ENV VITE_BACKEND_URI=$VITE_BACKEND_URI

RUN npm run build

# --- Etapa de runtime ---
# nginx-unprivileged corre como non-root en el puerto 8080
FROM nginxinc/nginx-unprivileged:alpine AS production

COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 8080

CMD ["nginx", "-g", "daemon off;"]
