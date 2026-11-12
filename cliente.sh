#!/usr/bin/env sh
# Abre um cliente. Uso: ./cliente.sh [IP_DO_SERVIDOR] [IP_DESTA_MAQUINA]
cd "$(dirname "$0")"
JAVA="java"
[ -n "$JAVA_HOME" ] && JAVA="$JAVA_HOME/bin/java"
exec "$JAVA" -cp bin cliente.ClienteMain "$@"
