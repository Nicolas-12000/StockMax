<#
  run-with-env.ps1
  - Loads variables from .env into the current process environment
  - Runs the Spring Boot app using the Maven wrapper

  Usage (PowerShell):
    ./scripts/run-with-env.ps1

  This avoids adding a Java dependency to parse .env files; it only sets
  environment variables for the PowerShell session and then runs mvnw.
#>

$envFile = Join-Path $PSScriptRoot "..\.env"

if (-Not (Test-Path $envFile)) {
    Write-Host "No .env file found at $envFile. Create one from .env.example or set env vars manually." -ForegroundColor Yellow
} else {
    Write-Host "Loading environment variables from $envFile" -ForegroundColor Green
    Get-Content $envFile | ForEach-Object {
        $line = $_.Trim()
        if ($line -and -not $line.StartsWith('#')) {
            $pair = $line -split '=', 2
            if ($pair.Length -eq 2) {
                $name = $pair[0].Trim()
                $value = $pair[1].Trim()
                [System.Environment]::SetEnvironmentVariable($name, $value, 'Process')
                Write-Host "  Loaded $name" -ForegroundColor DarkGreen
            }
        }
    }
}

Write-Host "Starting Spring Boot (mvnw) with loaded environment..." -ForegroundColor Cyan
Push-Location (Join-Path $PSScriptRoot "..")
try {
    & .\mvnw.cmd spring-boot:run
} finally {
    Pop-Location
}
