#!/bin/sh
if [ -z "$1" ]; then
  for script in $(find /loadtest/scripts -name '*.js' -not -path '*/helpers/*' | sort)
  do
    echo "==============================="
    echo "Corriendo: $script"
    echo "==============================="
    k6 run "$script"
  done
elif [ -d "/loadtest/scripts/$1" ]; then
  for script in $(find "/loadtest/scripts/$1" -name '*.js' -not -path '*/helpers/*' | sort)
  do
    echo "==============================="
    echo "Corriendo: $script"
    echo "==============================="
    k6 run "$script"
  done
else
  SCRIPT="/loadtest/scripts/$1"
  if [ ! -f "$SCRIPT" ]; then
    echo "Error: no se encontro '$SCRIPT'"
    exit 1
  fi
  echo "==============================="
  echo "Corriendo: $SCRIPT"
  echo "==============================="
  k6 run "$SCRIPT"
fi
