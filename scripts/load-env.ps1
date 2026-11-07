param([string]$Path = (Join-Path (Split-Path $PSScriptRoot -Parent) '.env'))
# Parse data, never execute .env contents. Explicit process variables take priority.
if (!(Test-Path -LiteralPath $Path)) { return }
foreach ($line in Get-Content -LiteralPath $Path -Encoding UTF8) {
    if ($line -match '^\s*(#|$)') { continue }
    if ($line -notmatch '^\s*([A-Za-z_][A-Za-z0-9_]*)=(.*)$') {
        throw 'Invalid .env entry. Use KEY=value, one entry per line.'
    }
    $key = $Matches[1]
    $value = $Matches[2].Trim()
    if ($null -eq [Environment]::GetEnvironmentVariable($key, 'Process')) {
        [Environment]::SetEnvironmentVariable($key, $value, 'Process')
    }
}
