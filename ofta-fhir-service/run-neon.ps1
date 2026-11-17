# Levanta el microservicio conectado a Neon.
# La cadena se lee de .env.local (IGNORADO por Git); nunca se versiona.
$ruta = Join-Path $PSScriptRoot '.env.local'
if (Test-Path $ruta) {
    foreach ($linea in Get-Content $ruta) {
        if ($linea -match '^([A-Z_]+)=(.*)$') {
            [Environment]::SetEnvironmentVariable($Matches[1], $Matches[2], 'Process')
        }
    }
}
Set-Location $PSScriptRoot
& 'C:/Users/Martin/Tools/apache-maven-3.9.9/bin/mvn' spring-boot:run
