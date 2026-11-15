# Proyecto 13 - Contrato de formatos

## 1. Objetivo

Establecer un formato comun para los archivos de grafos,
resultados de referencia y validacion de algoritmos.

## 2. Formato de entrada: GRAFO_V1

Cada archivo TXT contiene:

TIPO V E
ORIGEN DESTINO PESO

TIPO:
- D: grafo dirigido.
- U: grafo no dirigido.

V: numero de vertices.
E: numero de aristas.

Los vertices se numeran desde 0 hasta V-1.
Los pesos son enteros positivos.
No se admiten bucles propios ni aristas duplicadas.
En grafos no dirigidos se escribe cada arista una sola vez.
El archivo utiliza UTF-8.

Ejemplo:

U 3 2
0 1 4
1 2 7

## 3. Formato de referencia: REFERENCIA_V1

FORMATO=REFERENCIA_V1
CASO=nombre_del_caso
INICIO=0
BFS_ORDEN=0,1,2
BFS_DISTANCIAS=0,1,2
DFS_ORDEN=0,1,2
TOPO_ESTADO=NO_APLICA
MST_PESO=11
MST_COMPONENTES=1

Cada caso tiene un archivo de entrada y otro de referencia.
Ambos archivos tienen el mismo nombre.

## 4. Reglas de evaluacion

### DFS

Recorrido desde INICIO.
Se visitan vecinos en orden ascendente.
Se utiliza profundidad primero de manera recursiva.
Solo se incluyen vertices alcanzables.

### BFS

Recorrido desde INICIO.
Se visitan vecinos en orden ascendente.
Se utiliza una cola FIFO.
Distancia -1 indica vertice inalcanzable.

### Orden topologico

Aplica solamente a grafos dirigidos.
TOPO_ESTADO puede ser:
- ACICLICO
- CICLO
- NO_APLICA

Se acepta cualquier orden topologico valido.
En presencia de ciclos debe reportarse su existencia.

### MST

Aplica solamente a grafos no dirigidos.
En grafos desconectados se valida un bosque minimo.
El resultado debe ser aciclico y cubrir cada componente.
Debe utilizar aristas existentes en el grafo.
Su peso total debe coincidir con el minimo de referencia.
Se aceptan diferentes soluciones optimas.

Para algoritmos no aplicables se utiliza NO_APLICA.

## 5. Reproducibilidad

Los grafos aleatorios utilizan semillas fijas.
Las pruebas deben producir los mismos grafos al repetirse.

## 6. Responsabilidades

Core: carga, ejecucion y medicion.
Generador: creacion de grafos de prueba.
Referencia: calculo y almacenamiento de respuestas.
Validador: comprobacion de soluciones.
Submission: implementacion de ejemplo.
Documentacion: arquitectura, formatos y manual.

## 7. Version

Contrato inicial: 1.0