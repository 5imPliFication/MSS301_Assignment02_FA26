#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"
cd "$DIR"

echo "Stopping all microservices..."
for pidfile in logs/*.pid; do
    if [ -f "$pidfile" ]; then
        PID=$(cat "$pidfile")
        if kill -0 "$PID" 2>/dev/null; then
            echo "Killing process $PID ($pidfile)..."
            kill "$PID" || true
        fi
        rm -f "$pidfile"
    fi
done

# Fallback kill by port
fuser -k 8080/tcp 2>/dev/null || true
fuser -k 8081/tcp 2>/dev/null || true
fuser -k 8082/tcp 2>/dev/null || true
fuser -k 8083/tcp 2>/dev/null || true

echo "All services stopped."
