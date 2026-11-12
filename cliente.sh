#!/usr/bin/env sh
cd "$(dirname "$0")"
JAVA="java"
[ -n "$JAVA_HOME" ] && JAVA="$JAVA_HOME/bin/java"
exec "$JAVA" -cp bin cliente.ClienteMain "$@"
