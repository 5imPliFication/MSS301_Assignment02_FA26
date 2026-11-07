param([string]$Server)
$ErrorActionPreference = 'Stop'
& "$PSScriptRoot/load-env.ps1"
if (!$Server) {
    $dbHost = if ($env:DB_HOST) { $env:DB_HOST } else { 'localhost' }
    $dbPort = if ($env:DB_PORT) { $env:DB_PORT } else { '1433' }
    $Server = "$dbHost,$dbPort"
}
$settings = [System.Data.SqlClient.SqlConnectionStringBuilder]::new()
$settings.DataSource = $Server
$settings.InitialCatalog = 'master'
$settings.UserID = if ($env:DB_USERNAME) { $env:DB_USERNAME } else { 'sa' }
$settings.Password = if ($env:DB_PASSWORD) { $env:DB_PASSWORD } else { 'sa' }
$settings.Encrypt = if ($env:DB_ENCRYPT) { [bool]::Parse($env:DB_ENCRYPT) } else { $true }
$settings.TrustServerCertificate = if ($env:DB_TRUST_SERVER_CERTIFICATE) { [bool]::Parse($env:DB_TRUST_SERVER_CERTIFICATE) } else { $true }
$settings.ConnectTimeout = 5
$connection = [System.Data.SqlClient.SqlConnection]::new($settings.ConnectionString)
try {
    $connection.Open()
    $command = $connection.CreateCommand()
    $command.CommandText = "SELECT CAST(SERVERPROPERTY('ProductMajorVersion') AS int)"
    $major = [int]$command.ExecuteScalar()
    if ($major -ne 15) { throw "Expected SQL Server 2019 (major 15), found major $major" }
    $command.CommandText = "SELECT @@VERSION"
    Write-Output $command.ExecuteScalar()
    foreach ($name in @(
        $(if ($env:DB_ACCOUNT_NAME) { $env:DB_ACCOUNT_NAME } else { 'DBAccount' }),
        $(if ($env:DB_BRAND_NAME) { $env:DB_BRAND_NAME } else { 'DBBrand' }),
        $(if ($env:DB_WATCH_NAME) { $env:DB_WATCH_NAME } else { 'DBWatch' })
    )) {
        if ($name -notmatch '^[A-Za-z_][A-Za-z0-9_]{0,127}$') { throw 'Database names must be simple SQL identifiers.' }
        $command.CommandText = "IF DB_ID('$name') IS NULL CREATE DATABASE [$name]"
        [void]$command.ExecuteNonQuery()
    }
    Write-Output 'Verified SQL Server 2019, configured SQL credentials, and all three assignment databases.'
} finally { $connection.Dispose() }
