#!/usr/bin/env bash
set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"
cd "$DIR"
if [ -f .env ]; then
    set -a
    source .env
    set +a
fi
if [ -n "${JAVA_HOME:-}" ] && [ -d "$JAVA_HOME/bin" ]; then
    export PATH="$JAVA_HOME/bin:$PATH"
fi

echo "=== Starting MS SQL Server container ==="
docker compose up -d mssql

docker compose run --rm mssql-init

mkdir -p logs

echo "=== Starting MSAccount_HE190091 on port 8081 ==="
nohup java -jar MSAccount_HE190091/target/MSAccount_HE190091-1.0.0.jar > logs/msaccount.log 2>&1 &
echo $! > logs/msaccount.pid

echo "=== Starting MSBrand_HE190091 on port 8082 ==="
nohup java -jar MSBrand_HE190091/target/MSBrand_HE190091-1.0.0.jar > logs/msbrand.log 2>&1 &
echo $! > logs/msbrand.pid

echo "=== Starting MSWatch_HE190091 on port 8083 ==="
nohup java -jar MSWatch_HE190091/target/MSWatch_HE190091-1.0.0.jar > logs/mswatch.log 2>&1 &
echo $! > logs/mswatch.pid

echo "=== Starting APIGateway_HE190091 on port 8080 ==="
nohup java -jar APIGateway_HE190091/target/APIGateway_HE190091-1.0.0.jar > logs/gateway.log 2>&1 &
echo $! > logs/gateway.pid

echo "Waiting for all microservices to start..."
sleep 10
echo "System ready!"
