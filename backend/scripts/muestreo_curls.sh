#!/usr/bin/env bash
# =============================================================================
# muestreo_curls.sh — Tráfico variado contra el backend para poblar Grafana
#
# Cubre los 12 user stories de la consigna, genera errores deliberados para
# distribución de status codes, y una sección de carga para histogramas.
#
# Variables de entorno configurables:
#   BASE             URL base del backend  (default: staging en Render)
#   LOAD_ITERATIONS  Repeticiones de la sección de carga (default: 6)
#   SEED_FIG_A/B/C   IDs de figuritas de fallback si /figuritas no responde
#   ADMIN_USER       Usuario admin existente (opcional, para US12 real)
#   ADMIN_PASS       Contraseña del admin   (opcional, para US12 real)
# =============================================================================
set -euo pipefail

BASE="${BASE:-https://tacs-backend-staging.onrender.com}"
LOAD_ITERATIONS="${LOAD_ITERATIONS:-6}"
SEED_FIG_A="${SEED_FIG_A:-ARG-1}"
SEED_FIG_B="${SEED_FIG_B:-ARG-2}"
SEED_FIG_C="${SEED_FIG_C:-ARG-3}"
ADMIN_USER="${ADMIN_USER:-}"
ADMIN_PASS="${ADMIN_PASS:-}"

TS=$(date +%s)
USER_A="curltest_${TS}_a"
USER_B="curltest_${TS}_b"
PASS='Test1234!'
WEAK_PASS='abc'

TMPDIR=$(mktemp -d)
JAR_A="$TMPDIR/cookies_a.txt"
JAR_B="$TMPDIR/cookies_b.txt"
JAR_ADMIN="$TMPDIR/cookies_admin.txt"
trap 'rm -rf "$TMPDIR"' EXIT

if ! command -v node >/dev/null 2>&1; then
  echo "ERROR: este script necesita node para parsear JSON."
  exit 1
fi

# =============================================================================
# HELPERS
# =============================================================================

# Ejecuta una request y loguea [status] METHOD path - descripcion.
# Muestra body en 4xx y 5xx para facilitar debugging.
req() {
  local method="$1"; shift
  local path="$1";   shift
  local desc="$1";   shift
  local code
  code=$(curl -s -o "$TMPDIR/last_body.json" -w "%{http_code}" \
    -X "$method" "$BASE$path" "$@")
  echo "[$code] $method $path — $desc"
  if [[ "$code" == 4* || "$code" == 5* || "$code" == "000" ]]; then
    printf "        body: "
    tr -d '\n' < "$TMPDIR/last_body.json" | head -c 300
    printf "\n"
  fi
}

# Extrae un valor de last_body.json por path de puntos (ej: "contenido.0.id").
json_path() {
  node - "$TMPDIR/last_body.json" "$1" <<'JS'
const fs = require("fs");
let value;
try { value = JSON.parse(fs.readFileSync(process.argv[2], "utf8")); }
catch { process.exit(0); }
for (const part of process.argv[3].split(".")) {
  if (Array.isArray(value) && /^\d+$/.test(part)) {
    const idx = Number(part);
    if (idx >= value.length) process.exit(0);
    value = value[idx];
  } else if (value && typeof value === "object") {
    value = value[part];
  } else { process.exit(0); }
}
if (value !== undefined && value !== null) process.stdout.write(String(value));
JS
}

# Extrae IDs de figuritas desde /figuritas (soporta array plano o {contenido:[]}).
json_figurita_ids() {
  node - "$TMPDIR/last_body.json" <<'JS'
const fs = require("fs");
let data;
try { data = JSON.parse(fs.readFileSync(process.argv[2], "utf8")); }
catch { process.exit(0); }
const items = Array.isArray(data) ? data : (data?.contenido ?? []);
for (const item of items) {
  if (!item || typeof item !== "object") continue;
  const fig = item.figurita ?? item;
  const id = fig.id ?? fig.figurita_id ?? fig.figuritaId;
  if (id) console.log(id);
}
JS
}

# Busca en last_body.json (GET /subastas) la subasta activa de una figurita dada.
json_find_active_subasta_id() {
  local figurita_id="$1"
  node - "$TMPDIR/last_body.json" "$figurita_id" <<'JS'
const fs = require("fs");
let data;
try { data = JSON.parse(fs.readFileSync(process.argv[2], "utf8")); }
catch { process.exit(0); }
const figuritaId = process.argv[3];
for (const item of data?.contenido ?? []) {
  const fig = item.figurita_subastada ?? item.figuritaSubastada ?? item.figurita ?? {};
  if (fig.id === figuritaId || fig.figurita_id === figuritaId) {
    process.stdout.write(String(item.id ?? ""));
    break;
  }
}
JS
}

# Extrae el primer ID de sugerencia disponible de last_body.json.
json_first_sugerencia_id() {
  node - "$TMPDIR/last_body.json" <<'JS'
const fs = require("fs");
let data;
try { data = JSON.parse(fs.readFileSync(process.argv[2], "utf8")); }
catch { process.exit(0); }
const items = data?.contenido ?? [];
if (items.length > 0) process.stdout.write(String(items[0].id ?? ""));
JS
}

banner() {
  echo ""
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
  echo "  $1"
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
}

# =============================================================================
# INICIO
# =============================================================================
echo ""
echo "================================================================="
echo "  muestreo_curls.sh"
echo "  BASE:  $BASE"
echo "  Users: $USER_A / $USER_B"
echo "  Carga: $LOAD_ITERATIONS iteraciones"
echo "================================================================="

# =============================================================================
# §1  PING
# =============================================================================
banner "§1  PING"
req GET /ping "healthcheck → 200"

# =============================================================================
# §2  REGISTRO DE USUARIOS
# =============================================================================
banner "§2  REGISTRO DE USUARIOS"
req POST /usuarios "registrar usuario A → 204" \
  -H "Content-Type: application/json" \
  -d "{\"nombre\":\"$USER_A\",\"contrasenia\":\"$PASS\",\"rol\":\"USUARIO\"}"

req POST /usuarios "registrar usuario B → 204" \
  -H "Content-Type: application/json" \
  -d "{\"nombre\":\"$USER_B\",\"contrasenia\":\"$PASS\",\"rol\":\"USUARIO\"}"

# Errores deliberados
req POST /usuarios "usuario duplicado → 400" \
  -H "Content-Type: application/json" \
  -d "{\"nombre\":\"$USER_A\",\"contrasenia\":\"$PASS\",\"rol\":\"USUARIO\"}"

req POST /usuarios "password débil → 400" \
  -H "Content-Type: application/json" \
  -d "{\"nombre\":\"${USER_A}_weak\",\"contrasenia\":\"$WEAK_PASS\",\"rol\":\"USUARIO\"}"

req POST /usuarios "body vacío → 400" \
  -H "Content-Type: application/json" \
  -d "{}"

# =============================================================================
# §3  VERIFICACIÓN DE NOMBRE (HEAD)
# =============================================================================
banner "§3  VERIFICACIÓN DE NOMBRE"
req HEAD "/usuarios/$USER_A"             "nombre existente → 200"
req HEAD "/usuarios/no_existe_${TS}_zzz" "nombre inexistente → 404"

# =============================================================================
# §4  LOGIN
# =============================================================================
banner "§4  LOGIN"
req POST /login "login A correcto → 204" \
  -H "Content-Type: application/json" \
  -c "$JAR_A" \
  -d "{\"nombre\":\"$USER_A\",\"contrasenia\":\"$PASS\"}"

req POST /login "login B correcto → 204" \
  -H "Content-Type: application/json" \
  -c "$JAR_B" \
  -d "{\"nombre\":\"$USER_B\",\"contrasenia\":\"$PASS\"}"

# Errores deliberados
req POST /login "password incorrecta → 400" \
  -H "Content-Type: application/json" \
  -d "{\"nombre\":\"$USER_A\",\"contrasenia\":\"WrongPass1!\"}"

req POST /login "usuario inexistente → 404" \
  -H "Content-Type: application/json" \
  -d "{\"nombre\":\"no_existe_${TS}_zzz\",\"contrasenia\":\"$PASS\"}"

# =============================================================================
# §5  SESIÓN
# =============================================================================
banner "§5  SESIÓN (/yo)"
req GET /yo "yo con cookie A → 200" -b "$JAR_A"
PERFIL_A=$(json_path "perfil_id")

req GET /yo "yo con cookie B → 200" -b "$JAR_B"
PERFIL_B=$(json_path "perfil_id")

req GET /yo "yo sin cookie → 400"

echo "  PERFIL_A: ${PERFIL_A:-<no detectado>}"
echo "  PERFIL_B: ${PERFIL_B:-<no detectado>}"

# =============================================================================
# §6  US3 — BUSCAR FIGURITAS
# =============================================================================
banner "§6  US3 — BUSCAR FIGURITAS"

# Catálogo base
req GET "/figuritas?pagina=0&tamanioPagina=20" "figuritas base (todas) → 200"
mapfile -t FIG_IDS < <(json_figurita_ids)
FIG_A="${FIG_IDS[0]:-$SEED_FIG_A}"
FIG_B="${FIG_IDS[1]:-$SEED_FIG_B}"
FIG_C="${FIG_IDS[2]:-$SEED_FIG_C}"
echo "  FIG_A: $FIG_A  |  FIG_B: $FIG_B  |  FIG_C: $FIG_C"

# Filtros individuales
req GET "/figuritas?seleccion=Argentina&pagina=0&tamanioPagina=10" "filtro por seleccion → 200"
req GET "/figuritas?numero=10&pagina=0&tamanioPagina=10"           "filtro por numero → 200"
req GET "/figuritas?jugador=Messi&pagina=0&tamanioPagina=10"       "filtro por jugador → 200"

# Intercambiables (1-based pagination)
req GET "/figuritas/intercambiables?pagina=1&tamanioPagina=12"                    "intercambiables sin filtros → 200"
req GET "/figuritas/intercambiables?tipo=INTERCAMBIO&pagina=1&tamanioPagina=12"   "intercambiables tipo INTERCAMBIO → 200"
req GET "/figuritas/intercambiables?tipo=SUBASTA&pagina=1&tamanioPagina=12"       "intercambiables tipo SUBASTA → 200"
req GET "/figuritas/intercambiables?q=Messi&pagina=1&tamanioPagina=12"            "intercambiables búsqueda libre → 200"

# Errores deliberados
req GET "/figuritas?numero=abc"              "número inválido → 400"
req GET "/figuritas/intercambiables?tipo=NO_EXISTE" "tipo inválido → 400"

# =============================================================================
# §7  US2 — FALTANTES
# =============================================================================
banner "§7  US2 — FALTANTES"
req POST /colecciones/faltantes "A agrega FIG_A como faltante → 201" -b "$JAR_A" \
  -H "Content-Type: application/json" \
  -d "{\"fig_id\":\"$FIG_A\"}"

req POST /colecciones/faltantes "B agrega FIG_B como faltante → 201" -b "$JAR_B" \
  -H "Content-Type: application/json" \
  -d "{\"fig_id\":\"$FIG_B\"}"

req POST /colecciones/faltantes "B agrega FIG_C como faltante → 201" -b "$JAR_B" \
  -H "Content-Type: application/json" \
  -d "{\"fig_id\":\"$FIG_C\"}"

req GET "/colecciones/faltantes?pagina=1&limite=10" "listar faltantes A → 200" -b "$JAR_A"
req GET "/colecciones/faltantes?pagina=1&limite=10" "listar faltantes B → 200" -b "$JAR_B"

# Errores deliberados
req POST /colecciones/faltantes "body vacío → 400" -b "$JAR_A" \
  -H "Content-Type: application/json" \
  -d "{}"

req POST /colecciones/faltantes "sin autenticación → 400" \
  -H "Content-Type: application/json" \
  -d "{\"fig_id\":\"$FIG_A\"}"

# =============================================================================
# §8  US1 — REPETIDAS
# =============================================================================
banner "§8  US1 — REPETIDAS"

# A publica FIG_B para intercambio directo (B la tiene como faltante → dispara notificación)
req POST /colecciones/repetidas "A agrega FIG_B (INTERCAMBIO) → 201" -b "$JAR_A" \
  -H "Content-Type: application/json" \
  -d "{\"fig_id\":\"$FIG_B\",\"cantidad_existente\":5,\"modos_intercambio\":[\"INTERCAMBIO\"]}"

# A publica FIG_C para subasta
req POST /colecciones/repetidas "A agrega FIG_C (SUBASTA) → 201" -b "$JAR_A" \
  -H "Content-Type: application/json" \
  -d "{\"fig_id\":\"$FIG_C\",\"cantidad_existente\":3,\"modos_intercambio\":[\"SUBASTA\"]}"

# B publica FIG_A para intercambio y subasta (A la tiene como faltante → dispara notificación)
req POST /colecciones/repetidas "B agrega FIG_A (INTERCAMBIO+SUBASTA) → 201" -b "$JAR_B" \
  -H "Content-Type: application/json" \
  -d "{\"fig_id\":\"$FIG_A\",\"cantidad_existente\":6,\"modos_intercambio\":[\"INTERCAMBIO\",\"SUBASTA\"]}"

# Listar repetidas
req GET "/colecciones/repetidas?pagina=1&limite=10" "listar repetidas A → 200" -b "$JAR_A"
req GET "/colecciones/repetidas?pagina=1&limite=10" "listar repetidas B → 200" -b "$JAR_B"
req GET "/colecciones/repetidas?metodoIntercambio=INTERCAMBIO&pagina=1&limite=10" "repetidas A filtro INTERCAMBIO → 200" -b "$JAR_A"

# Editar repetida (PATCH)
req PATCH "/colecciones/repetidas/$FIG_B" "A edita FIG_B: agrega modo SUBASTA → 204" -b "$JAR_A" \
  -H "Content-Type: application/json" \
  -d "{\"cantidad_nueva\":4,\"metodos\":[\"INTERCAMBIO\",\"SUBASTA\"]}"

# Errores deliberados
req POST /colecciones/repetidas "body vacío → 400" -b "$JAR_A" \
  -H "Content-Type: application/json" \
  -d "{}"

# =============================================================================
# §9  US8 (parcial) — PERFIL Y CONTADORES
# =============================================================================
banner "§9  US8 — PERFIL Y CONTADORES"
req GET /perfil           "perfil A → 200"     -b "$JAR_A"
req GET /perfil/contadores "contadores A → 200" -b "$JAR_A"

req PUT /perfil "editar perfil A → 200" -b "$JAR_A" \
  -H "Content-Type: application/json" \
  -d "{\"nombre\":\"Usuario Curl A\",\"nombre_usuario\":\"$USER_A\",\"medios_de_contacto\":[]}"

# Errores deliberados
req PUT /usuarios/contrasenia "cambio pass con clave incorrecta → 400" -b "$JAR_A" \
  -H "Content-Type: application/json" \
  -d "{\"contrasenia_actual\":\"WrongPass1!\",\"contrasenia_nueva\":\"$PASS\"}"

# =============================================================================
# §10  US5 + US9 — PROPUESTAS (ciclo completo)
# =============================================================================
banner "§10  US5 + US9 — PROPUESTAS"

PROP_ACEPTAR=""
PROP_RECHAZAR=""
PROP_CANCELAR=""

if [ -n "${PERFIL_B:-}" ]; then
  # Propuesta 1: A → B, para aceptar (A quiere FIG_A que tiene B, ofrece FIG_B)
  req POST /propuestas "A propone intercambio a B (para aceptar) → 201" -b "$JAR_A" \
    -H "Content-Type: application/json" \
    -d "{\"destinatario_id\":\"$PERFIL_B\",\"figurita_buscada_id\":\"$FIG_A\",\"figuritas_ofrecidas_ids\":[\"$FIG_B\"]}"
  PROP_ACEPTAR=$(json_path "id")

  # Propuesta 2: A → B, para rechazar
  req POST /propuestas "A propone intercambio a B (para rechazar) → 201" -b "$JAR_A" \
    -H "Content-Type: application/json" \
    -d "{\"destinatario_id\":\"$PERFIL_B\",\"figurita_buscada_id\":\"$FIG_A\",\"figuritas_ofrecidas_ids\":[\"$FIG_B\"]}"
  PROP_RECHAZAR=$(json_path "id")

  # Propuesta 3: A → B, para cancelar
  req POST /propuestas "A propone intercambio a B (para cancelar) → 201" -b "$JAR_A" \
    -H "Content-Type: application/json" \
    -d "{\"destinatario_id\":\"$PERFIL_B\",\"figurita_buscada_id\":\"$FIG_A\",\"figuritas_ofrecidas_ids\":[\"$FIG_B\"]}"
  PROP_CANCELAR=$(json_path "id")

  echo "  PROP_ACEPTAR: ${PROP_ACEPTAR:-<no detectada>}"
  echo "  PROP_RECHAZAR: ${PROP_RECHAZAR:-<no detectada>}"
  echo "  PROP_CANCELAR: ${PROP_CANCELAR:-<no detectada>}"

  # Acciones
  [ -n "$PROP_ACEPTAR"  ] && req PATCH "/propuestas/$PROP_ACEPTAR/aceptar"   "B acepta propuesta → 204"   -b "$JAR_B"
  [ -n "$PROP_RECHAZAR" ] && req PATCH "/propuestas/$PROP_RECHAZAR/rechazar"  "B rechaza propuesta → 204"  -b "$JAR_B"
  [ -n "$PROP_CANCELAR" ] && req PATCH "/propuestas/$PROP_CANCELAR/cancelar"  "A cancela propuesta → 204"  -b "$JAR_A"

  # Consultas
  req GET "/propuestas?tipo=ENVIADAS&pagina=1&limite=10"                      "propuestas enviadas A → 200"        -b "$JAR_A"
  req GET "/propuestas?tipo=RECIBIDAS&pagina=1&limite=10"                     "propuestas recibidas B → 200"       -b "$JAR_B"
  req GET "/propuestas?tipo=ENVIADAS&estado=ACEPTADO&pagina=1&limite=10"      "propuestas enviadas aceptadas → 200" -b "$JAR_A"
  [ -n "$PROP_ACEPTAR" ] && req GET "/propuestas/$PROP_ACEPTAR"              "detalle propuesta aceptada → 200"   -b "$JAR_A"

  # Errores deliberados
  req POST /propuestas "destinatario inexistente → 404" -b "$JAR_A" \
    -H "Content-Type: application/json" \
    -d "{\"destinatario_id\":\"000000000000000000000000\",\"figurita_buscada_id\":\"$FIG_A\",\"figuritas_ofrecidas_ids\":[\"$FIG_B\"]}"
else
  echo "  (omitido: no se pudo obtener PERFIL_B)"
fi

req GET "/propuestas/000000000000000000000000" "propuesta inexistente → 404" -b "$JAR_A"

# =============================================================================
# §11  US10 — CALIFICACIONES POST-INTERCAMBIO
# =============================================================================
banner "§11  US10 — CALIFICACIONES (post-intercambio directo)"

if [ -n "$PROP_ACEPTAR" ] && [ -n "${PERFIL_A:-}" ] && [ -n "${PERFIL_B:-}" ]; then
  req POST /perfil/calificaciones "A califica a B tras intercambio → 200" -b "$JAR_A" \
    -H "Content-Type: application/json" \
    -d "{\"valor\":5,\"destinatario_id\":\"$PERFIL_B\",\"descripcion\":\"Excelente intercambio\",\"transaction_id\":\"$PROP_ACEPTAR\",\"tipo_transaccion\":\"INTERCAMBIO\"}"

  req POST /perfil/calificaciones "B califica a A tras intercambio → 200" -b "$JAR_B" \
    -H "Content-Type: application/json" \
    -d "{\"valor\":4,\"destinatario_id\":\"$PERFIL_A\",\"descripcion\":\"Todo bien\",\"transaction_id\":\"$PROP_ACEPTAR\",\"tipo_transaccion\":\"INTERCAMBIO\"}"

  req GET "/perfil/calificaciones?pagina=0&limite=10" "calificaciones recibidas A → 200" -b "$JAR_A"
  req GET "/perfil/calificaciones?pagina=0&limite=10" "calificaciones recibidas B → 200" -b "$JAR_B"

  # Errores deliberados
  req POST /perfil/calificaciones "valor fuera de rango → 400" -b "$JAR_A" \
    -H "Content-Type: application/json" \
    -d "{\"valor\":10,\"destinatario_id\":\"$PERFIL_B\",\"descripcion\":\"x\",\"transaction_id\":\"$PROP_ACEPTAR\",\"tipo_transaccion\":\"INTERCAMBIO\"}"

  req POST /perfil/calificaciones "sin transaction_id → 400" -b "$JAR_A" \
    -H "Content-Type: application/json" \
    -d "{\"valor\":3,\"destinatario_id\":\"$PERFIL_B\",\"descripcion\":\"x\",\"tipo_transaccion\":\"INTERCAMBIO\"}"
else
  echo "  (omitido: no se completó el intercambio directo)"
fi

# =============================================================================
# §12  US6 + US7 — SUBASTAS (ciclo completo)
# =============================================================================
banner "§12  US6 + US7 — SUBASTAS"

SUBASTA_ID=""
OFERTA_ID=""

# Listar subastas activas
req GET "/subastas?estado=ACTIVA&pagina=1&limite=10"    "subastas activas (global) → 200" -b "$JAR_A"
req GET "/subastas/000000000000000000000000"             "subasta inexistente → 404"

# Crear subasta (A subasta FIG_C, desea FIG_A)
req POST /subastas "A crea subasta de FIG_C → 200" -b "$JAR_A" \
  -H "Content-Type: application/json" \
  -d "{\"figurita_id\":\"$FIG_C\",\"duracion_en_horas\":24,\"figuritas_deseadas_ids\":[\"$FIG_A\"],\"calificacion_minima\":0}"

# Recuperar subasta recién creada
req GET "/subastas?autorId=${PERFIL_A:-}&estado=ACTIVA&pagina=1&limite=20" "subastas activas de A → 200" -b "$JAR_A"
SUBASTA_ID=$(json_find_active_subasta_id "$FIG_C")
echo "  SUBASTA_ID: ${SUBASTA_ID:-<no detectada>}"

if [ -n "$SUBASTA_ID" ]; then
  # Detalle de la subasta
  req GET "/subastas/$SUBASTA_ID" "detalle subasta sin ofertas → 200"

  # B oferta en la subasta (B tiene FIG_A que A desea)
  req POST "/subastas/$SUBASTA_ID/ofertas" "B oferta FIG_A en subasta → 200" -b "$JAR_B" \
    -H "Content-Type: application/json" \
    -d "{\"figuritas_ofrecidas_id\":[\"$FIG_A\"]}"

  # Recuperar id de la oferta
  req GET "/subastas/$SUBASTA_ID" "detalle subasta con oferta → 200"
  OFERTA_ID=$(json_path "ofertas.0.id")
  echo "  OFERTA_ID: ${OFERTA_ID:-<no detectada>}"

  if [ -n "$OFERTA_ID" ]; then
    # B edita su oferta
    req PATCH "/subastas/$SUBASTA_ID/ofertas/$OFERTA_ID" "B edita su oferta → 200" -b "$JAR_B" \
      -H "Content-Type: application/json" \
      -d "{\"figuritas_ofrecidas_id\":[\"$FIG_A\"]}"

    # A selecciona la oferta ganadora
    req PATCH "/subastas/$SUBASTA_ID/ofertas/$OFERTA_ID/seleccionar" "A selecciona oferta ganadora → 200" -b "$JAR_A"

    # A cierra la subasta (ejecuta el intercambio)
    req PATCH "/subastas/$SUBASTA_ID/cerrar" "A cierra la subasta → 200" -b "$JAR_A"
  fi

  # Subastas finalizadas y participadas
  req GET "/subastas?autorId=${PERFIL_A:-}&estado=FINALIZADA&pagina=1&limite=10" "subastas finalizadas de A → 200" -b "$JAR_A"
  req GET "/subastas?participanteId=${PERFIL_B:-}&pagina=1&limite=10"             "subastas participadas por B → 200" -b "$JAR_B"
fi

# Ciclo secundario: subasta que termina cancelada
req POST /subastas "A crea subasta para cancelar → 200" -b "$JAR_A" \
  -H "Content-Type: application/json" \
  -d "{\"figurita_id\":\"$FIG_C\",\"duracion_en_horas\":1,\"figuritas_deseadas_ids\":[\"$FIG_B\"],\"calificacion_minima\":0}"
req GET "/subastas?autorId=${PERFIL_A:-}&estado=ACTIVA&pagina=1&limite=20" "buscar subasta a cancelar → 200" -b "$JAR_A"
SUBASTA_CANCELAR=$(json_find_active_subasta_id "$FIG_C")
if [ -n "${SUBASTA_CANCELAR:-}" ]; then
  # B oferta para luego cancelar la oferta
  req POST "/subastas/$SUBASTA_CANCELAR/ofertas" "B oferta para luego cancelar → 200" -b "$JAR_B" \
    -H "Content-Type: application/json" \
    -d "{\"figuritas_ofrecidas_id\":[\"$FIG_A\"]}"
  req GET "/subastas/$SUBASTA_CANCELAR" "detalle subasta antes de cancelar oferta → 200"
  OFERTA_CANCELAR=$(json_path "ofertas.0.id")
  [ -n "${OFERTA_CANCELAR:-}" ] && \
    req PATCH "/subastas/$SUBASTA_CANCELAR/ofertas/$OFERTA_CANCELAR/cancelar" "B cancela su oferta → 200" -b "$JAR_B"

  req PATCH "/subastas/$SUBASTA_CANCELAR/cancelar" "A cancela la subasta → 200" -b "$JAR_A"
fi

# Errores deliberados
req POST /subastas "figuritasDeseadasIds vacío → 400" -b "$JAR_A" \
  -H "Content-Type: application/json" \
  -d "{\"figurita_id\":\"$FIG_C\",\"duracion_en_horas\":24,\"figuritas_deseadas_ids\":[],\"calificacion_minima\":0}"

req POST /subastas "figurita inexistente → 404" -b "$JAR_A" \
  -H "Content-Type: application/json" \
  -d "{\"figurita_id\":\"000000000000000000000000\",\"duracion_en_horas\":24,\"figuritas_deseadas_ids\":[\"$FIG_A\"],\"calificacion_minima\":0}"

# =============================================================================
# §13  US10 — CALIFICACIONES POST-SUBASTA
# =============================================================================
banner "§13  US10 — CALIFICACIONES (post-subasta)"

if [ -n "${SUBASTA_ID:-}" ] && [ -n "${PERFIL_A:-}" ] && [ -n "${PERFIL_B:-}" ]; then
  req POST /perfil/calificaciones "A califica a B tras subasta → 200" -b "$JAR_A" \
    -H "Content-Type: application/json" \
    -d "{\"valor\":5,\"destinatario_id\":\"$PERFIL_B\",\"descripcion\":\"Gran ofertante\",\"transaction_id\":\"$SUBASTA_ID\",\"tipo_transaccion\":\"SUBASTA\"}"

  req POST /perfil/calificaciones "B califica a A tras subasta → 200" -b "$JAR_B" \
    -H "Content-Type: application/json" \
    -d "{\"valor\":5,\"destinatario_id\":\"$PERFIL_A\",\"descripcion\":\"Subasta justa\",\"transaction_id\":\"$SUBASTA_ID\",\"tipo_transaccion\":\"SUBASTA\"}"
else
  echo "  (omitido: no se completó la subasta)"
fi

# =============================================================================
# §14  US4 — SUGERENCIAS
# =============================================================================
banner "§14  US4 — SUGERENCIAS"
req GET "/sugerencias?pagina=1&limite=10" "sugerencias A → 200" -b "$JAR_A"
SUGERENCIA_ID=$(json_first_sugerencia_id)
echo "  SUGERENCIA_ID: ${SUGERENCIA_ID:-<ninguna todavía, se generan a las 3am>}"

[ -n "${SUGERENCIA_ID:-}" ] && \
  req PATCH "/sugerencias/$SUGERENCIA_ID/favorito" "A marca sugerencia como favorita → 204" -b "$JAR_A"

req GET "/sugerencias?pagina=2&limite=5" "sugerencias A página 2 → 200" -b "$JAR_A"

# =============================================================================
# §15  US11 — NOTIFICACIONES
# =============================================================================
banner "§15  US11 — NOTIFICACIONES"
req GET  /perfil/notificaciones          "notificaciones A → 200"        -b "$JAR_A"
req GET  /perfil/notificaciones          "notificaciones B → 200"        -b "$JAR_B"
req PATCH /perfil/notificaciones/leidas  "A marca todas como leídas → 204" -b "$JAR_A"
req GET  /perfil/notificaciones          "notificaciones A (post-leidas) → 200" -b "$JAR_A"

# =============================================================================
# §16  US12 — ESTADÍSTICAS ADMIN
# =============================================================================
banner "§16  US12 — ESTADÍSTICAS ADMIN"

if [ -n "$ADMIN_USER" ] && [ -n "$ADMIN_PASS" ]; then
  req POST /login "login admin → 204" \
    -H "Content-Type: application/json" \
    -c "$JAR_ADMIN" \
    -d "{\"nombre\":\"$ADMIN_USER\",\"contrasenia\":\"$ADMIN_PASS\"}"

  TODAY=$(date +%Y-%m-%d)
  WEEK_AGO=$(date -d "7 days ago" +%Y-%m-%d 2>/dev/null || date -v-7d +%Y-%m-%d)

  req GET "/administrador/estadisticas?desde=$WEEK_AGO&hasta=$TODAY" \
    "estadísticas admin última semana → 200" -b "$JAR_ADMIN"

  req GET "/administrador/estadisticas?desde=2026-01-01&hasta=$TODAY" \
    "estadísticas admin desde el inicio → 200" -b "$JAR_ADMIN"

  req GET "/administrador/estadisticas?desde=2026-06-01&hasta=2026-05-01" \
    "estadísticas rango inválido (desde > hasta) → 400" -b "$JAR_ADMIN"

  req DELETE /sesion "logout admin → 204" -b "$JAR_ADMIN"
else
  echo "  (ADMIN_USER/ADMIN_PASS no definidos — se omite estadísticas con permisos reales)"
fi

# Usuario normal intentando endpoints de admin (genera 401/403 en Grafana)
req GET "/administrador/estadisticas?desde=2026-01-01&hasta=2026-12-31" \
  "estadísticas con usuario normal → 401/403" -b "$JAR_A"

req POST /administradores "alta admin con usuario normal → 401/403" -b "$JAR_A" \
  -H "Content-Type: application/json" \
  -d "{\"nombre\":\"${USER_A}_admin\",\"contrasenia\":\"$PASS\"}"

# =============================================================================
# §17  ERRORES ADICIONALES (distribución de status codes para Grafana)
# =============================================================================
banner "§17  ERRORES ADICIONALES"

# 401 — sin cookie en endpoints que la requieren
req GET  /perfil                         "perfil sin cookie → 400/401"
req GET  /colecciones/faltantes          "faltantes sin cookie → 400/401"
req GET  /propuestas                     "propuestas sin cookie → 400/401"
req GET  /subastas                       "subastas sin cookie → 400/401"
req POST /perfil/calificaciones          "calificación sin cookie → 400/401" \
  -H "Content-Type: application/json" \
  -d "{\"valor\":5,\"destinatario_id\":\"x\",\"transaction_id\":\"x\",\"tipo_transaccion\":\"INTERCAMBIO\"}"

# 404 — recursos inexistentes
req GET  "/propuestas/000000000000000000000000"  "propuesta id 0 → 404" -b "$JAR_A"
req GET  "/subastas/000000000000000000000000"    "subasta id 0 → 404"

# 400 — bodies malformados
req POST /colecciones/repetidas "repetida sin modos_intercambio → 400" -b "$JAR_A" \
  -H "Content-Type: application/json" \
  -d "{\"fig_id\":\"$FIG_A\",\"cantidad_existente\":1,\"modos_intercambio\":[]}"

req POST /login "login sin body → 400" \
  -H "Content-Type: application/json" \
  -d "{}"

# =============================================================================
# §18  CARGA (histogramas de latencia y distribución de endpoints)
# =============================================================================
banner "§18  CARGA ($LOAD_ITERATIONS iteraciones)"
for i in $(seq 1 "$LOAD_ITERATIONS"); do
  req GET "/figuritas/intercambiables?pagina=1&tamanioPagina=12"                   "carga intercambiables [$i]"       -b "$JAR_A"
  req GET "/figuritas/intercambiables?tipo=INTERCAMBIO&pagina=1&tamanioPagina=12"  "carga intercambiables INTER [$i]" -b "$JAR_A"
  req GET "/colecciones/faltantes?pagina=1&limite=10"                              "carga faltantes [$i]"             -b "$JAR_A"
  req GET "/colecciones/repetidas?pagina=1&limite=10"                              "carga repetidas [$i]"             -b "$JAR_A"
  req GET "/propuestas?tipo=ENVIADAS&pagina=1&limite=10"                           "carga propuestas [$i]"            -b "$JAR_A"
  req GET "/subastas?estado=ACTIVA&pagina=1&limite=10"                             "carga subastas activas [$i]"      -b "$JAR_A"
  req GET "/sugerencias?pagina=1&limite=10"                                        "carga sugerencias [$i]"           -b "$JAR_A"
  req GET "/perfil/notificaciones"                                                  "carga notificaciones [$i]"        -b "$JAR_A"
  req GET "/perfil/contadores"                                                      "carga contadores [$i]"            -b "$JAR_A"
done

# =============================================================================
# §19  LOGOUT
# =============================================================================
banner "§19  LOGOUT"
req DELETE /sesion "logout A → 204" -b "$JAR_A"
req DELETE /sesion "logout B → 204" -b "$JAR_B"

echo ""
echo "================================================================="
echo "  Muestreo completo."
echo "================================================================="
