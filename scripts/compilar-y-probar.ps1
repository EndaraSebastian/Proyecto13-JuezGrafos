# Compila todo src, ejecuta las 9 clases de prueba y juzga la submission de ejemplo.
$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "..")
if (Test-Path out) { Remove-Item out -Recurse -Force }
New-Item -ItemType Directory -Force -Path out | Out-Null
$archivos = @(Get-ChildItem "src" -Recurse -Filter "*.java" | Select-Object -ExpandProperty FullName)
javac -encoding UTF-8 -d out $archivos
if ($LASTEXITCODE -ne 0) { exit 1 }
$pruebas = "core.PruebaParser","core.PruebaMedidor","core.PruebaEjecutorJuez","core.PruebaValidador",
  "generador.PruebaGenerador","generador.PruebaReferencias","generador.PruebaRendimiento",
  "generador.PruebaReferenciasRendimiento","generador.PruebaCasosNegativos"
foreach ($c in $pruebas) {
  Write-Host "== $c"
  java -cp out "ec.edu.epn.eda2.juez.$c" | Select-Object -Last 2
  if ($LASTEXITCODE -ne 0) { exit 1 }
}
Write-Host "== Juzgando SolucionModelo"
java -cp out ec.edu.epn.eda2.juez.Main juzgar ec.edu.epn.eda2.juez.submission.SolucionModelo --casos todos | Select-Object -Last 2
