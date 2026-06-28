#!/bin/sh
if [ -z "$1" ]; then
  for script in /loadtest/scripts/*.js
  do
    case "$script" in
      */helpers/*) continue ;;
    esac
    echo "==============================="
    echo "Corriendo: $script"
    echo "==============================="
    k6 run "$script"
  done
else
  k6 run "/loadtest/scripts/$1"
fi