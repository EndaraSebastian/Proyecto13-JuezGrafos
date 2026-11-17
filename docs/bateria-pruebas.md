# Proyecto 13 - Bateria de pruebas de grafos

## 1. Objetivo

Documentar los grafos utilizados para evaluar
implementaciones de DFS, BFS, orden topologico y MST.

La bateria incluye casos manuales y aleatorios
para comprobar correccion, casos limite y
diferentes configuraciones de grafos.

## 2. Organizacion

Los archivos se almacenan en dos carpetas:

- casos/entrada/: grafos de prueba.
- casos/referencia/: resultados esperados.

Cada grafo tiene un archivo de referencia
con el mismo nombre.

Ejemplo:

- entrada/01_conexo.txt
- referencia/01_conexo.txt

## 3. Resumen de los 13 casos

| Caso | Tipo | V | E | Objetivo |
|---|---|---:|---:|---|
| 01_conexo | No dirigido | 5 | 7 | Recorridos y MST |
| 02_desconexo | No dirigido | 6 | 3 | Componentes separados |
| 03_dag | Dirigido | 6 | 6 | Orden topologico |
| 04_ciclo_dirigido | Dirigido | 5 | 5 | Deteccion de ciclos |
| 05_mst_empates | No dirigido | 4 | 6 | Multiples MST validos |
| 06_aislados | No dirigido | 5 | 0 | Vertices aislados |
| 07_un_vertice | No dirigido | 1 | 0 | Caso limite |
| 08_ciclo_no_dirigido | No dirigido | 5 | 5 | Ciclos y MST |
| 09_dag_desconexo | Dirigido | 6 | 3 | DAG desconectado |
| 10_disperso | No dirigido | 12 | 11 | Pocas aristas |
| 11_denso | No dirigido | 12 | 53 | Muchas aristas |
| 12_aleatorio_medio | No dirigido | 12 | 22 | Grafo intermedio |
| 13_ciclo_aleatorio | No dirigido | 12 | 30 | Ciclos aleatorios |

V representa vertices y E representa aristas.

## 4. Descripcion de los casos manuales

### Caso 01 - Grafo conexo

Contiene cinco vertices y siete aristas.
Todos los vertices son alcanzables desde 0.

Permite comprobar:
- Recorrido DFS.
- Recorrido BFS.
- Distancias BFS.
- Peso minimo MST.

Peso minimo esperado: 10.
Componentes esperadas: 1.

### Caso 02 - Grafo desconectado

Contiene seis vertices y tres aristas.

Tiene tres componentes:
- Vertices 0, 1 y 2.
- Vertices 3 y 4.
- Vertice aislado 5.

Permite comprobar vertices inalcanzables
y bosques de expansion minima.

Peso minimo esperado: 10.
Componentes esperadas: 3.

### Caso 03 - DAG

Grafo dirigido sin ciclos.

Permite comprobar que el orden topologico
respete la direccion de todas las aristas.

Estado esperado: ACICLICO.

Los vertices 1 y 3 no son alcanzables
desde el vertice inicial 0.

### Caso 04 - Ciclo dirigido

Contiene el ciclo:

0 -> 1 -> 2 -> 0

Permite comprobar la deteccion de ciclos.

Estado esperado: CICLO.

No existe un orden topologico valido.

### Caso 05 - MST con empates

Contiene varias aristas de peso 1.

Existen varios arboles de expansion
minima con el mismo peso total.

Peso minimo esperado: 3.

El validador debe aceptar diferentes
arboles correctos.

### Caso 06 - Vertices aislados

Contiene cinco vertices sin aristas.

Permite comprobar:
- Recorrido desde un vertice aislado.
- Distancias inalcanzables.
- Bosque de expansion minima vacio.

Peso MST esperado: 0.
Componentes esperadas: 5.

### Caso 07 - Un vertice

Contiene un unico vertice y ninguna arista.

Permite comprobar el caso limite minimo
admitido por nuestro modelo.

Peso MST esperado: 0.
Componentes esperadas: 1.

### Caso 08 - Ciclo no dirigido

Contiene cinco vertices y cinco aristas.

Permite comprobar:
- Recorridos sobre grafos con ciclos.
- Evitar visitas repetidas.
- Seleccion de aristas para MST.

Peso minimo esperado: 11.

### Caso 09 - DAG desconectado

Contiene seis vertices y tres aristas.

Permite comprobar el orden topologico
cuando existen componentes desconectados.

Estado esperado: ACICLICO.

El orden topologico debe incluir
todos los vertices.

## 5. Casos aleatorios

Los grafos aleatorios se generan con
semillas fijas para garantizar reproducibilidad.

| Caso | Semilla | Vertices | Aristas |
|---|---:|---:|---:|
| 10_disperso | 20261301 | 12 | 11 |
| 11_denso | 20261302 | 12 | 53 |
| 12_aleatorio_medio | 20261303 | 12 | 22 |
| 13_ciclo_aleatorio | 20261304 | 12 | 30 |

### Caso 10 - Grafo disperso

Contiene 12 vertices y 11 aristas.

Es conexo y tiene estructura de arbol.
No contiene ciclos.

Peso MST esperado: 114.

### Caso 11 - Grafo denso

Contiene 12 vertices y 53 aristas.

Tiene aproximadamente el 80% de las
aristas posibles de un grafo simple
no dirigido de 12 vertices.

Peso MST esperado: 34.

### Caso 12 - Grafo aleatorio medio

Contiene 12 vertices y 22 aristas.

Permite comprobar los algoritmos en una
configuracion intermedia de conexiones.

Peso MST esperado: 68.

### Caso 13 - Grafo aleatorio con ciclos

Contiene 12 vertices y 30 aristas.

Es conexo y necesariamente contiene ciclos.

Peso MST esperado: 62.

## 6. Criterios de validacion

### DFS

Se utiliza el vertice inicial 0.
Los vecinos se visitan en orden ascendente.

Se comprueba el orden de descubrimiento
segun el contrato del proyecto.

### BFS

Se utiliza el vertice inicial 0.

Se comprueban:
- Orden del recorrido.
- Distancias minimas en numero de aristas.
- Vertices inalcanzables.

Los vertices inalcanzables tienen distancia -1.

### Orden topologico

Solo se aplica a grafos dirigidos.

En DAGs se comprueba que cada vertice
aparezca antes que sus sucesores.

En grafos con ciclos se debe reportar CICLO.

Pueden existir varios ordenes validos.

### MST

Solo se aplica a grafos no dirigidos.

Se comprueba:
- Utilizacion de aristas existentes.
- Ausencia de ciclos.
- Cobertura de cada componente.
- Cantidad correcta de aristas.
- Peso total minimo.

En grafos desconectados se utiliza
un bosque de expansion minima.

Para C componentes, el bosque
debe contener V-C aristas.

## 7. Pruebas automatizadas

### PruebaGenerador

Verifica la construccion de grafos,
propiedades, archivos y reproducibilidad.

Resultado obtenido:
- Correctas: 17.
- Fallidas: 0.

### PruebaReferencias

Lee los archivos TXT y comprueba
los resultados almacenados.

Utiliza Prim para contrastar los pesos
calculados originalmente con Kruskal.

Resultado obtenido:
- Correctas: 14.
- Fallidas: 0.

Total de comprobaciones superadas: 31.

## 8. Limitaciones y ampliaciones

La bateria actual utiliza grafos pequenos,
principalmente de hasta 12 vertices.

Es adecuada para las pruebas funcionales
iniciales del framework.

Para evaluar rendimiento se recomienda
agregar posteriormente grafos mas grandes
y con diferentes densidades.

El generador aleatorio actual produce
grafos no dirigidos ponderados.

Los DAGs y los grafos dirigidos con ciclos
de esta primera bateria se definen manualmente.

## 9. Conclusion

La bateria contiene 13 grafos diferentes
y sus respectivas respuestas de referencia.

Los casos cubren las principales condiciones
solicitadas en el Proyecto 13.

Las comprobaciones automatizadas permiten
verificar propiedades y resultados antes
de integrar el framework completo.

## Ampliación: rendimiento y pruebas negativas

### 1. Casos de rendimiento

Se incorporaron cinco grafos adicionales para
preparar los experimentos de eficiencia algorítmica.

| Caso | Vértices | Aristas | Peso MST |
|---|---:|---:|---:|
| 14 - Disperso | 100 | 150 | 857 |
| 15 - Denso | 100 | 3960 | 100 |
| 16 - Disperso | 500 | 750 | 3919 |
| 17 - Denso | 500 | 99800 | 499 |
| 18 - Disperso | 1000 | 1500 | 7875 |

Todos los casos son grafos no dirigidos,
ponderados, conexos y reproducibles.

Los archivos se encuentran en:

- `casos/rendimiento/`
- `casos/referencias-rendimiento/`

### 2. Verificación de grafos grandes

Clase: `PruebaRendimiento.java`

Se comprueba:

- Existencia de los cinco archivos TXT.
- Cantidad de vértices y aristas.
- Formato del encabezado y de las conexiones.
- Vértices dentro del rango permitido.
- Pesos entre 1 y 20.
- Ausencia de bucles propios.
- Ausencia de aristas duplicadas.
- Conectividad mediante Union-Find.
- Reproducibilidad de una configuración.

Resultado: 7 pruebas correctas y 0 fallidas.

### 3. Verificación independiente de referencias

Clase: `PruebaReferenciasRendimiento.java`

Se utilizaron implementaciones independientes:

- Prim para contrastar los pesos MST de Kruskal.
- BFS para verificar distancias y órdenes de recorrido.

Se comprobaron los cinco casos grandes.

Resultado: 5 pruebas correctas y 0 fallidas.

### 4. Pruebas negativas del modelo

Clase: `PruebaCasosNegativos.java`

Casos inválidos rechazados:

1. Origen negativo.
2. Destino negativo.
3. Peso igual a cero.
4. Peso negativo.
5. Bucle propio.
6. Vértice fuera de rango.
7. Aristas duplicadas no dirigidas.
8. Aristas duplicadas dirigidas.
9. Grafo sin vértices.

Casos válidos aceptados:

10. Grafo desconectado.
11. Aristas dirigidas opuestas.
12. Grafo de un solo vértice.

Resultado: 12 pruebas correctas y 0 fallidas.

Estas comprobaciones corresponden al modelo
`Arista` y `CasoGrafo`. No sustituyen las
futuras pruebas del parser ni del validador.

### 5. Resumen de ejecución

| Clase de prueba | Correctas | Fallidas |
|---|---:|---:|
| PruebaGenerador | 17 | 0 |
| PruebaReferencias | 14 | 0 |
| PruebaRendimiento | 7 | 0 |
| PruebaReferenciasRendimiento | 5 | 0 |
| PruebaCasosNegativos | 12 | 0 |
| **Total** | **55** | **0** |

Las cinco clases se ejecutaron en conjunto
y todas finalizaron correctamente.

### 6. Pruebas de integración

Con los módulos integrados, estas comprobaciones están cubiertas por
`PruebaValidador`, `PruebaEjecutorJuez` y `PruebaParser`:

- Lectura y rechazo de TXT mal formados.
- Rechazo de soluciones incorrectas.
- Aceptación de MST alternativos válidos.
- Aceptación de órdenes topológicos válidos.
- Ejecución completa mediante el juez.
- Mediciones repetidas de tiempo y de memoria asignada.

Los grafos grandes actuales son no dirigidos.
Todavía no representan una batería completa
de rendimiento para ordenamiento topológico.

Los 55 resultados anteriores son pruebas
de corrección, no mediciones de benchmark.
