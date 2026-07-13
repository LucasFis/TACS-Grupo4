#!/bin/sh

RESULTS_DIR="/loadtest/results"
mkdir -p "$RESULTS_DIR"

run_k6() {
  script="$1"
  # Derivar nombre legible: /loadtest/scripts/get/subastas.js -> get-subastas
  name=$(echo "$script" | sed 's|.*/scripts/||; s|\.js$||; s|/|-|g')
  json="$RESULTS_DIR/${name}.json"
  echo "==============================="
  echo "Corriendo: $script"
  echo "Exportando a: $json"
  echo "==============================="
  k6 run --summary-export="$json" "$script"
}

if [ -z "$1" ]; then
  for script in $(find /loadtest/scripts -name '*.js' -not -path '*/helpers/*' | sort)
  do
    run_k6 "$script"
  done
elif [ -d "/loadtest/scripts/$1" ]; then
  for script in $(find "/loadtest/scripts/$1" -name '*.js' -not -path '*/helpers/*' | sort)
  do
    run_k6 "$script"
  done
else
  SCRIPT="/loadtest/scripts/$1"
  if [ ! -f "$SCRIPT" ]; then
    echo "Error: no se encontro '$SCRIPT'"
    exit 1
  fi
  run_k6 "$SCRIPT"
fi

echo ""
echo "============================================"
echo "Resultados exportados en: $RESULTS_DIR"
echo "============================================"
ls -la "$RESULTS_DIR"
