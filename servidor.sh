#!/usr/bin/env sh
# Sobe o servidor. Uso: ./servidor.sh [IP_DESTA_MAQUINA]
cd "$(dirname "$0")"
JAVA="java"
[ -n "$JAVA_HOME" ] && JAVA="$JAVA_HOME/bin/java"
exec "$JAVA" -cp bin servidor.ServidorMain "$@"
