$projectRoot = Split-Path $PSScriptRoot -Parent
foreach ($name in @('MSAccount_HE190091','MSBrand_HE190091','MSWatch_HE190091','APIGateway_HE190091','frontend')) {
    $pidFile = "$projectRoot/logs/$name.local.pid"
    if (!(Test-Path -LiteralPath $pidFile)) { continue }
    $serviceProcessId = [int](Get-Content -LiteralPath $pidFile)
    $process = Get-CimInstance Win32_Process -Filter "ProcessId = $serviceProcessId"
    $expected = if ($name -eq 'frontend') { "$projectRoot/frontend/node_modules/vite/bin/vite.js" } else { "$projectRoot/$name/target/$name-1.0.0.jar" }
    if ($process -and $process.CommandLine.Replace('\','/').Contains($expected.Replace('\','/'))) {
        Stop-Process -Id $serviceProcessId
        Write-Output "Stopped $name"
    }
    Remove-Item -LiteralPath $pidFile
}
