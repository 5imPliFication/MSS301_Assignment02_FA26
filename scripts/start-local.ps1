param([ValidateSet('sqlserver','h2')][string]$Profile = 'sqlserver', [switch]$NoFrontend)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
& "$PSScriptRoot/load-env.ps1"
if ($Profile -eq 'sqlserver') { & "$PSScriptRoot/verify-sql.ps1" }
$java = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin/java.exe' } else { (Get-Command java -ErrorAction Stop).Source }
$services = @('MSAccount_HE190091','MSBrand_HE190091','MSWatch_HE190091','APIGateway_HE190091')
foreach ($service in $services) {
    if (!(Test-Path "$projectRoot/$service/target/$service-1.0.0.jar")) { throw 'Build first with mvn verify.' }
}
$ports = @(
    $(if ($env:GATEWAY_PORT) { [int]$env:GATEWAY_PORT } else { 8080 }),
    $(if ($env:ACCOUNT_PORT) { [int]$env:ACCOUNT_PORT } else { 8081 }),
    $(if ($env:BRAND_PORT) { [int]$env:BRAND_PORT } else { 8082 }),
    $(if ($env:WATCH_PORT) { [int]$env:WATCH_PORT } else { 8083 })
)
$frontendPort = if ($env:FRONTEND_PORT) { [int]$env:FRONTEND_PORT } else { 5173 }
if (!$NoFrontend) {
    $ports += $frontendPort
    $node = (Get-Command node -ErrorAction Stop).Source
    if (!(Test-Path "$projectRoot/frontend/node_modules/vite/bin/vite.js")) { throw 'Run npm ci inside frontend first.' }
}
foreach ($port in $ports) {
    if (Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue) { throw "Port $port is already occupied. Stop the existing application first." }
}
New-Item -ItemType Directory -Force "$projectRoot/logs" | Out-Null
foreach ($service in $services) {
    $jar = "$projectRoot/$service/target/$service-1.0.0.jar"
    $profileArgument = if ($Profile -eq 'h2' -and $service -ne 'APIGateway_HE190091') { '--spring.profiles.active=h2' } else { '--spring.profiles.active=default' }
    $process = Start-Process -FilePath $java -ArgumentList @('-Xmx256m','-jar',('"' + $jar + '"'),$profileArgument) -WorkingDirectory $projectRoot -WindowStyle Hidden -PassThru -RedirectStandardOutput "$projectRoot/logs/$service.out.log" -RedirectStandardError "$projectRoot/logs/$service.err.log"
    $process.Id | Set-Content "$projectRoot/logs/$service.local.pid"
}
if (!$NoFrontend) {
    $vite = '"' + "$projectRoot/frontend/node_modules/vite/bin/vite.js" + '"'
    $process = Start-Process -FilePath $node -ArgumentList @($vite,'--host','127.0.0.1','--strictPort') -WorkingDirectory "$projectRoot/frontend" -WindowStyle Hidden -PassThru -RedirectStandardOutput "$projectRoot/logs/frontend.out.log" -RedirectStandardError "$projectRoot/logs/frontend.err.log"
    $process.Id | Set-Content "$projectRoot/logs/frontend.local.pid"
}
Write-Output "Services are starting. Logs: logs/. Frontend: http://127.0.0.1:$frontendPort. Gateway: http://localhost:$($ports[0])."
Write-Output 'Stop only these processes with scripts/stop-local.ps1.'
