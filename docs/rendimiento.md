# Proyecto 13 - Bateria de rendimiento

## 1. Objetivo

Preparar grafos de diferentes tamanos y densidades
para evaluar el tiempo y el consumo de memoria
de las implementaciones estudiantiles.

Los casos se generan de forma reproducible
y se almacenan independientemente de la
bateria funcional.

## 2. Casos implementados

| Caso | Vertices | Aristas | Densidad |
|---|---:|---:|---:|
| 14_disperso | 100 | 150 | 3,03 % |
| 15_denso | 100 | 3960 | 80 % |
| 16_disperso | 500 | 750 | 0,60 % |
| 17_denso | 500 | 99800 | 80 % |
| 18_disperso | 1000 | 1500 | 0,30 % |

Los nombres de los archivos incluyen el prefijo
"rendimiento" y la cantidad de vertices.

Todos los grafos son:
- No dirigidos.
- Ponderados con pesos entre 1 y 20.
- Conexos.
- Sin bucles propios ni aristas duplicadas.

## 3. Densidad

Para grafos simples no dirigidos:

Densidad = 2E / (V * (V - 1))

V representa la cantidad de vertices.
E representa la cantidad de aristas.

Los grafos dispersos tienen pocas conexiones
en comparacion con el maximo posible.

Los grafos densos utilizan aproximadamente
el 80 % de las conexiones posibles.

## 4. Reproducibilidad

Las semillas utilizadas son:

- Caso 14: 20261314
- Caso 15: 20261315
- Caso 16: 20261316
- Caso 17: 20261317
- Caso 18: 20261318

La misma semilla y parametros permiten
reproducir los grafos generados.

## 5. Generacion

Compilar las clases principales y ejecutar:

```powershell
java -cp out ec.edu.epn.eda2.juez.Main rendimiento
```

Los archivos se guardan en:

casos/rendimiento/

## 6. Verificacion

Ejecutar:

```powershell
java -cp out ec.edu.epn.eda2.juez.generador.PruebaRendimiento
```

Resultado obtenido:

Correctas: 7
Fallidas: 0

Las pruebas verifican el formato,
las dimensiones, la conectividad,
los pesos y la ausencia de duplicados.

Tambien se comprueba la reproducibilidad
de una configuracion de generacion.

## 7. Experimentos pendientes

Cuando este integrado el juez se mediran:

- Tiempo de ejecucion en nanosegundos.
- Memoria asignada por algoritmo (bytes asignados por la solucion).
- Resultados de correccion.
- Diferencias entre grafos densos y dispersos.
- Comportamiento ante distintos tamanos.

Se recomienda ejecutar varias repeticiones,
utilizar calentamiento de la JVM y registrar
los resultados de manera consistente.

No deben confundirse los tiempos de generacion
de archivos con los tiempos de los algoritmos.

## 8. Limitaciones

La bateria de rendimiento actual contiene
solamente grafos no dirigidos.

Se recomienda incorporar posteriormente
DAGs grandes y grafos dirigidos con ciclos
para evaluar el ordenamiento topologico.

Las mediciones y reportes de rendimiento
todavia no estan implementados.