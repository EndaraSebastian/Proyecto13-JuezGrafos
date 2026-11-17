# Validador y submission de ejemplo

## 1. Validador real: `ValidadorGrafos`

Es el validador por defecto del juez (`JuezCli.VALIDADOR_POR_DEFECTO`).
Implementa `ValidadorResultado`, nunca lanza excepciones y devuelve un
`Veredicto` con el motivo exacto cuando rechaza una respuesta.

| Algoritmo | Tipo de comprobacion | Criterios |
|---|---|---|
| DFS | Exacta (orden determinista: vecinos ascendentes) | Sin vertices fuera de rango ni repetidos; mismo orden y longitud que la referencia (solo alcanzables) |
| BFS | Exacta | Igual que DFS para el orden, y `distancias` de longitud V igual a la referencia (`-1` = inalcanzable) |
| Topologico | Por propiedades | Si la referencia dice `CICLO`, la solucion debe reportar ciclo. Si es `ACICLICO`, no debe reportar ciclo, debe devolver una permutacion de los V vertices y respetar cada arista `u->v` (u antes que v). Se acepta **cualquier** orden valido |
| MST | Por propiedades | Cada arista existe en el grafo con su peso real (cualquier sentido), sin ciclos ni repetidas, quedan exactamente `MST_COMPONENTES` componentes (V-C aristas) y el peso total es `MST_PESO`. Se aceptan todos los MST optimos distintos |

Para usar otro validador: `--validador <Clase>`. El provisional
`ValidadorPendiente` (no valida, reporta `SIN_VALIDAR`) sigue disponible.

## 2. Submission de ejemplo: `SolucionModelo`

`ec.edu.epn.eda2.juez.submission.SolucionModelo` es una submission correcta
de referencia para los estudiantes:

- DFS iterativo con pila explicita (mismo orden que el recursivo, sin riesgo
  de desbordar la pila).
- BFS con cola FIFO y arreglo de distancias.
- Orden topologico por el algoritmo de Kahn; `conCiclo()` si no se procesan
  todos los vertices.
- MST con Kruskal y Union-Find; devuelve un bosque minimo si el grafo es
  desconexo.

Resultado verificado: 18 casos (13 funcionales + 5 de rendimiento),
54 comprobaciones `CORRECTO`, 18 `NO_APLICA`, codigo de salida 0.

```powershell
java -cp out ec.edu.epn.eda2.juez.Main juzgar ec.edu.epn.eda2.juez.submission.SolucionModelo --casos todos --csv resultados.csv
```

## 3. Como escribir una submission

1. Crear una clase publica con constructor publico sin argumentos que
   implemente `SolucionEstudiante` (ver [core-juez.md](core-juez.md)).
2. No guardar estado entre llamadas (el juez repite cada metodo para medir).
3. Vecinos en orden ascendente: `grafo.vecinos(v)` ya los entrega asi.
4. Compilar `src` junto con la clase y ejecutar `juzgar <ClaseCompleta>`.

## 4. Pruebas del validador

`src/test/java/ec/edu/epn/eda2/juez/core/PruebaValidador.java` (29
comprobaciones): la submission modelo es correcta en los 18 casos; cinco
soluciones deliberadamente erroneas (DFS invertido, BFS sin distancias,
topologico invertido, ciclo no detectado, MST de peso maximo) son marcadas
`INCORRECTO`; y respuestas absurdas (fuera de rango, repetidas, aristas
inexistentes, pesos alterados, ciclos, longitudes incorrectas) son rechazadas
con su motivo.

```powershell
java -cp out ec.edu.epn.eda2.juez.core.PruebaValidador
```
