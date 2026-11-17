#!/usr/bin/env bash
# Compila todo src, ejecuta las 9 clases de prueba y juzga la submission de ejemplo.
set -e
cd "$(dirname "$0")/.."
rm -rf out && mkdir out
javac -encoding UTF-8 -d out $(find src -name '*.java')
for c in core.PruebaParser core.PruebaMedidor core.PruebaEjecutorJuez core.PruebaValidador \
         generador.PruebaGenerador generador.PruebaReferencias generador.PruebaRendimiento \
         generador.PruebaReferenciasRendimiento generador.PruebaCasosNegativos; do
  echo "== $c"; java -cp out ec.edu.epn.eda2.juez.$c | tail -2
done
echo "== Juzgando SolucionModelo"
java -cp out ec.edu.epn.eda2.juez.Main juzgar ec.edu.epn.eda2.juez.submission.SolucionModelo --casos todos | tail -2
