#!/usr/bin/env bash
# wait-for-it.sh: espera a que un host:puerto esté disponible

host="$1"
shift
cmd="$@"

until nc -z "$host" 3306; do
  echo "⏳ Esperando a que MySQL esté disponible en $host:3306..."
  sleep 2
done

echo "✅ MySQL está disponible. Ejecutando la aplicación..."
exec $cmd
