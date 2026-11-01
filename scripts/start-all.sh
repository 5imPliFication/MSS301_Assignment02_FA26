#!/usr/bin/env bash
set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"
cd "$DIR"

echo "=== Starting MS SQL Server container ==="
docker compose up -d mssql

echo "Waiting for SQL Server to be healthy..."
until docker exec mss301-mssql-server /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P 'YourStrong@Password123' -C -Q 'SELECT 1' > /dev/null 2>&1; do
    sleep 1
done

echo "Ensuring DBAccount, DBBrand, DBWatch exist..."
docker exec mss301-mssql-server /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P 'YourStrong@Password123' -C -i /docker-entrypoint-initdb.d/init-databases.sql

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
