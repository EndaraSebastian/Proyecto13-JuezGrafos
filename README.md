# Proyecto 13 - Juez de Grafos

## Escuela Politecnica Nacional
**Asignatura:** Estructuras de Datos y Algoritmos II
**Grupo:** 13
**Lenguaje:** Java 17 o superior

## 1. Descripcion

Framework desarrollado en Java para evaluar
implementaciones estudiantiles de algoritmos de grafos.

El proyecto busca:

1. Cargar grafos desde archivos TXT.
2. Ejecutar DFS, BFS, orden topologico y MST.
3. Verificar la correccion de los resultados.
4. Medir tiempos de ejecucion y memoria.
5. Generar reportes de evaluacion.

Actualmente esta implementado el modulo de
generacion de grafos y respuestas de referencia.

## 2. Algoritmos evaluados

- DFS: busqueda en profundidad.
- BFS: busqueda en anchura.
- Ordenamiento topologico de grafos dirigidos.
- MST: arbol o bosque de expansion minima.

## 3. Arquitectura

El sistema utiliza una arquitectura modular
orientada a objetos.

Componentes:

- core: modelo compartido, parser y ejecucion.
- generador: creacion de grafos.
- referencia: calculo de respuestas esperadas.
- validador: comprobacion de soluciones.
- submission: implementaciones de estudiantes.
- docs: documentacion del sistema.

Los componentes pendientes se incorporaran
durante la integracion del framework.

## 4. Estructura del repositorio

```text
Proyecto13-JuezGrafos/
|-- src/
|   |-- main/java/ec/edu/epn/eda2/juez/
|   |   |-- Main.java
|   |   |-- core/
|   |   |-- generador/
|   |   |-- validador/
|   |   `-- submission/
|   `-- test/java/
|-- casos/
|   |-- entrada/
|   `-- referencia/
|-- docs/
|-- .gitignore
`-- README.md
```

## 5. Requisitos

- JDK 17 o superior.
- Git.
- Terminal compatible con Java.
- Visual Studio Code (recomendado).

El modulo actual no requiere dependencias externas.

## 6. Compilacion

Desde PowerShell, en la raiz del proyecto:

```powershell
New-Item -ItemType Directory -Force -Path out | Out-Null

$archivos = @(Get-ChildItem "src" -Recurse -Filter "*.java" |
    Where-Object { $_.Length -gt 0 } |
    Select-Object -ExpandProperty FullName)

javac -encoding UTF-8 -d out $archivos
```

## 7. Generar grafos

```powershell
java -cp out ec.edu.epn.eda2.juez.Main generar
```

Se crean 13 grafos en:

`casos/entrada/`

## 8. Generar referencias

```powershell
java -cp out ec.edu.epn.eda2.juez.Main referencias
```

Las respuestas se guardan en:

`casos/referencia/`

Para regenerar las referencias:

```powershell
java -cp out ec.edu.epn.eda2.juez.Main referencias --sobrescribir
```

## 9. Pruebas automatizadas

Pruebas del generador:

```powershell
java -cp out ec.edu.epn.eda2.juez.generador.PruebaGenerador
```

Pruebas independientes de referencias:

```powershell
java -cp out ec.edu.epn.eda2.juez.generador.PruebaReferencias
```

Resultados obtenidos:

| Prueba | Correctas | Fallidas |
|---|---:|---:|
| Generador | 17 | 0 |
| Referencias | 14 | 0 |
| Total | 31 | 0 |

Estos resultados corresponden al modulo desarrollado.
Todavia no son pruebas del juez completo.

## 10. Bateria de pruebas

Se incluyen 13 grafos:

- Conexos y desconectados.
- Dirigidos y no dirigidos.
- DAGs y grafos con ciclos.
- Densos y dispersos.
- Casos con vertices aislados.
- Casos con multiples MST validos.
- Grafos aleatorios reproducibles.

## 11. Formatos

### Entrada

```text
U 3 2
0 1 4
1 2 7
```

La primera linea indica tipo, vertices y aristas.
Las siguientes indican origen, destino y peso.

### Referencia

```text
FORMATO=REFERENCIA_V1
CASO=01_conexo
INICIO=0
BFS_ORDEN=0,1,2,3,4
BFS_DISTANCIAS=0,1,1,2,2
DFS_ORDEN=0,1,2,3,4
TOPO_ESTADO=NO_APLICA
TOPO_ORDEN=NO_APLICA
MST_PESO=10
MST_COMPONENTES=1
```

## 12. Documentacion

- [Contrato de formatos](docs/contrato-formatos.md)
- [Arquitectura del sistema](docs/arquitectura.md)
- [Manual de uso](docs/manual-uso.md)
- [Bateria de pruebas](docs/bateria-pruebas.md)

## 13. Distribucion del trabajo

### Estudiante 1 - Core y medicion

Responsable de:
- Parser de entrada.
- Contrato de submission.
- Ejecucion de implementaciones.
- Medicion de tiempo y memoria.
- Reporte del juez.

### Estudiante 2 - Validacion y submission

Responsable de:
- Validacion de DFS y BFS.
- Validacion del orden topologico.
- Validacion de MST.
- Implementacion estudiantil de ejemplo.

### Estudiante 3 - Grafos y documentacion

Responsable de:
- Generacion manual y aleatoria.
- Bateria de pruebas.
- Referencias de resultados.
- Pruebas del generador.
- Documentacion y arquitectura.

## 14. Estado del proyecto

Implementado:
- Modelo compartido de grafos.
- Generacion de 13 casos.
- Generacion de 13 referencias.
- Pruebas automatizadas.
- Documentacion inicial.

Pendiente:
- Parser y ejecucion de submissions.
- Validadores de algoritmos.
- Medicion de tiempo y memoria.
- Reporte integrado.
- Pruebas de integracion del framework.

## 15. Observaciones

El proyecto se desarrolla de forma colaborativa
mediante Git y GitHub.

Las referencias admiten soluciones alternativas
cuando el algoritmo lo permite.

Se utiliza una semilla fija para reproducir
los grafos aleatorios.

## Batería de rendimiento y verificación ampliada

El módulo de generación incluye 18 grafos y 18 referencias.

Se utilizan dos conjuntos de casos:

- 13 casos funcionales para verificar algoritmos.
- 5 casos grandes para futuros experimentos de rendimiento.

### Casos de rendimiento

| Caso | Vértices | Aristas | Peso MST |
|---|---:|---:|---:|
| 14 - Disperso | 100 | 150 | 857 |
| 15 - Denso | 100 | 3960 | 100 |
| 16 - Disperso | 500 | 750 | 3919 |
| 17 - Denso | 500 | 99800 | 499 |
| 18 - Disperso | 1000 | 1500 | 7875 |

Los cinco grafos son no dirigidos, ponderados, conexos
y reproducibles mediante semillas fijas.

Los pesos MST fueron contrastados utilizando Prim
y Kruskal. Los resultados BFS también se verificaron
con una implementación independiente.

### Carpetas

- `casos/entrada/`: 13 grafos funcionales.
- `casos/referencia/`: 13 referencias funcionales.
- `casos/rendimiento/`: 5 grafos grandes.
- `casos/referencias-rendimiento/`: 5 referencias grandes.

### Comandos principales

Generar grafos funcionales:

```powershell
java -cp out ec.edu.epn.eda2.juez.Main generar
```

Generar referencias funcionales:

```powershell
java -cp out ec.edu.epn.eda2.juez.Main referencias
```

Generar grafos de rendimiento:

```powershell
java -cp out ec.edu.epn.eda2.juez.Main rendimiento
```

Generar referencias de rendimiento:

```powershell
java -cp out ec.edu.epn.eda2.juez.Main referencias-rendimiento
```

Por defecto, las referencias existentes se conservan.

### Pruebas automatizadas

Las clases de prueba se encuentran en
`src/test/java/ec/edu/epn/eda2/juez/generador/`.

| Clase | Comprobaciones |
|---|---:|
| `PruebaGenerador` | 17 |
| `PruebaReferencias` | 14 |
| `PruebaRendimiento` | 7 |
| `PruebaReferenciasRendimiento` | 5 |
| `PruebaCasosNegativos` | 12 |
| **Total** | **55** |

Resultado de la ejecución conjunta:

- Clases de prueba superadas: 5 de 5.
- Comprobaciones correctas: 55.
- Comprobaciones fallidas: 0.

### Limitaciones actuales

Los cinco grafos grandes son no dirigidos.
Todavía no se incluyen DAGs grandes ni grafos
dirigidos grandes con ciclos.

El ejecutor, el parser, el validador general y las
mediciones de tiempo y memoria se integrarán
con los módulos de los otros integrantes.

Estos resultados verifican la generación y las
referencias; no representan un benchmark final.

Para más detalles, consultar
[Documentación de rendimiento](docs/rendimiento.md).
