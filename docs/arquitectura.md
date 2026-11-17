# Proyecto 13 - Arquitectura del Juez de Grafos

## 1. Objetivo

Desarrollar un framework en Java para cargar grafos,
ejecutar algoritmos entregados por estudiantes,
verificar sus resultados y medir su rendimiento.

El sistema evalua DFS, BFS, orden topologico y MST.

## 2. Arquitectura

Se utiliza una arquitectura modular orientada a objetos.

Cada componente tiene una responsabilidad definida
para facilitar el mantenimiento, las pruebas y la integracion.

## 3. Componentes

### Core

Contiene el modelo compartido del sistema.

Clases existentes:
- Arista: representa una conexion ponderada.
- TipoGrafo: identifica grafos dirigidos y no dirigidos.
- CasoGrafo: almacena vertices, aristas y metadatos.

Integrado por el Estudiante 1 (detalle en core-juez.md):
- GrafoCargado y Vecino: grafo en memoria con adyacencia.
- LectorGrafoTxt y LectorReferenciaTxt: parser de TXT.
- SolucionEstudiante: contrato de las soluciones.
- MedidorEjecucion y EjecutorJuez: ejecucion y medicion.
- ReporteJuez y JuezCli: reporte y comando juzgar.

### Generador

Construye los casos de prueba.

Clases:
- GeneradorAleatorio
- CasosManuales
- BateriaCasos
- EscritorGrafoTxt

### Referencias

Calcula y almacena las respuestas esperadas.

Clases:
- CalculadorReferencias
- EscritorReferenciaTxt

Utiliza DFS, BFS, Kahn y Kruskal para obtener
los resultados de referencia.

### Validador

Implementado en `ValidadorGrafos`.

Comprueba los resultados de los algoritmos,
aceptando todas las soluciones alternativas validas
(ver validador.md).

### Submission

Implementada en `SolucionModelo`.

Ejemplo correcto de implementacion estudiantil
de DFS, BFS, orden topologico y MST.

### Reporte y medicion

Implementados en el core (Estudiante 1).

Registran el resultado de correccion y el tiempo de
ejecucion de cada algoritmo, junto con la memoria asignada
(bytes asignados por el hilo de la solucion).

## 4. Diagrama de arquitectura

```mermaid
flowchart TD
    A["Main"] --> B["BateriaCasos"]
    B --> C["CasosManuales"]
    B --> D["GeneradorAleatorio"]
    B --> E["EscritorGrafoTxt"]
    B --> F["CalculadorReferencias"]
    F --> G["EscritorReferenciaTxt"]

    E --> H[("TXT de entrada")]
    G --> I[("TXT de referencia")]

    H --> J["Parser"]
    J --> K["Ejecutor"]
    L["Submission"] --> K

    K --> M["Validador"]
    I --> M

    K --> N["Medicion"]
    M --> O["Reporte"]
    N --> O
```

El modelo compartido del core es utilizado
por los componentes implementados en Java.

Todos los componentes estan integrados.

## 5. Formatos

### Entrada

Primera linea:
TIPO V E

Lineas siguientes:
ORIGEN DESTINO PESO

D: dirigido.
U: no dirigido.

### Referencia

Se utiliza el formato REFERENCIA_V1,
documentado en contrato-formatos.md.

Incluye recorridos, distancias, estado topologico,
peso minimo y numero de componentes.

## 6. Pruebas

PruebaGenerador:
17 comprobaciones superadas.

PruebaReferencias:
14 comprobaciones superadas.

Las pruebas comprueban la generacion,
el formato y las propiedades de los grafos.

Las referencias se contrastan mediante
implementaciones independientes.

## 7. Integracion

El estudiante 1 integro el parser, el ejecutor,
la medicion y el reporte (ver core-juez.md).

El estudiante 2 integro el validador real
y la submission de ejemplo (ver validador.md).

El estudiante 3 proporciona los casos,
las referencias y la documentacion.

## 8. Estado actual

Implementado:
- Modelo de grafos.
- Generacion manual y aleatoria.
- Escritura de entradas TXT.
- Calculo de referencias.
- Escritura de referencias.
- Pruebas del generador y referencias.

Implementado por el Estudiante 1:
- Parser de entrada y de referencias.
- Interfaz de submission.
- Ejecucion del codigo estudiantil.
- Medicion de tiempo.
- Reporte integrado.

Implementado por el Estudiante 2:
- Validador real (`ValidadorGrafos`).
- Submission de ejemplo (`SolucionModelo`).
- Pruebas del validador (`PruebaValidador`).

## Ampliacion de arquitectura: rendimiento y pruebas

### 1. Objetivo de la ampliacion

El modulo generador incorpora cinco grafos grandes
para preparar futuras pruebas de rendimiento.

Se conserva la separacion entre casos funcionales
y casos de rendimiento.

Ambas baterias reutilizan el modelo compartido
de grafos y los algoritmos de referencia.

### 2. Diagrama de componentes

```mermaid
flowchart TD
    MAIN["Main.java"]

    BF["BateriaCasos"]
    BR["BateriaRendimiento"]

    GA["GeneradorAleatorio"]
    CG["CasoGrafo y Arista"]

    EG["EscritorGrafoTxt"]
    CR["CalculadorReferencias"]
    ER["EscritorReferenciaTxt"]
    GR["GeneradorReferenciasRendimiento"]

    EF["13 TXT funcionales"]
    RF["13 referencias funcionales"]
    EP["5 TXT de rendimiento"]
    RP["5 referencias de rendimiento"]

    MAIN --> BF
    MAIN --> BR
    MAIN --> GR

    BF --> GA
    BR --> GA
    GA --> CG

    BF --> EG
    BR --> EG
    EG --> EF
    EG --> EP

    BF --> CR
    CR --> ER
    ER --> RF

    GR --> BR
    GR --> CR
    GR --> ER
    GR --> RP
```

El diagrama representa las responsabilidades
principales y el flujo general de datos.

### 3. Clases del modulo de rendimiento

| Clase | Responsabilidad |
|---|---|
| BateriaRendimiento | Define cinco casos grandes reproducibles |
| GeneradorAleatorio | Construye grafos ponderados |
| GeneradorReferenciasRendimiento | Coordina las referencias grandes |
| CalculadorReferencias | Calcula DFS, BFS, topologico y MST segun el tipo de grafo |
| EscritorGrafoTxt | Guarda grafos de entrada |
| EscritorReferenciaTxt | Guarda los resultados esperados |

### 4. Arquitectura de pruebas

```mermaid
flowchart TD
    PG["PruebaGenerador"]
    PR["PruebaReferencias"]
    PE["PruebaRendimiento"]
    PRE["PruebaReferenciasRendimiento"]
    PN["PruebaCasosNegativos"]

    MODELO["Modelo de grafos"]
    FUNC["13 grafos y referencias funcionales"]
    GRANDES["5 grafos y referencias grandes"]

    PG --> MODELO
    PG --> FUNC

    PR --> FUNC

    PE --> GRANDES

    PRE --> GRANDES

    PN --> MODELO
```

Las cinco clases de prueba se encuentran
en src/test/java.

### 5. Resultados de verificacion

| Prueba | Correctas | Fallidas |
|---|---:|---:|
| Generador | 17 | 0 |
| Referencias funcionales | 14 | 0 |
| Grafos de rendimiento | 7 | 0 |
| Referencias de rendimiento | 5 | 0 |
| Casos negativos | 12 | 0 |
| Total | 55 | 0 |

Para los grafos grandes, los pesos MST
se contrastaron mediante Prim y Kruskal.

Las distancias y los ordenes BFS tambien
se compararon con una implementacion independiente.

### 6. Integracion

Esta arquitectura corresponde al modulo
de generacion, referencias y pruebas.

El proyecto completo requiere integrar
los componentes de los otros integrantes:

- Lectura y procesamiento de TXT.
- Ejecucion de soluciones estudiantiles.
- Validacion de los resultados.
- Medicion de tiempo y memoria.
- Reportes finales del juez.

La integracion debera respetar el modelo
compartido y el contrato de formatos.

## Ampliacion de arquitectura: core del juez

Este diagrama reemplaza al de la seccion 4 ahora que el
parser, el ejecutor, la medicion y el reporte existen.
El validador y la submission de ejemplo tambien estan
integrados (Estudiante 2).

```mermaid
flowchart TD
    MAIN["Main juzgar"] --> CLI["JuezCli"]
    CLI --> CAT["CatalogoCasos"]
    CLI --> EJ["EjecutorJuez"]
    CAT --> EJ

    TXT[("TXT de entrada")] --> LG["LectorGrafoTxt"]
    REF[("TXT de referencia")] --> LR["LectorReferenciaTxt"]
    EJ --> LG
    EJ --> LR
    LG --> GC["GrafoCargado"]
    LR --> RC["ReferenciaCaso"]

    GC --> MED["MedidorEjecucion"]
    EJ --> MED
    MED --> SOL["SolucionEstudiante"]
    SOL --> RES["Resultados"]

    RES --> VAL["ValidadorResultado<br/>ValidadorGrafos"]
    GC --> VAL
    RC --> VAL

    VAL --> REP["ReporteJuez"]
    MED --> REP
    REP --> OUT[("Consola y CSV")]
```

Contratos entre componentes:

| Contrato | Define | Implementa |
|---|---|---|
| `SolucionEstudiante` | Los 4 metodos de cada solucion | Estudiantes evaluados; ejemplo: `SolucionModelo` |
| `ValidadorResultado` | Como se decide si una respuesta es correcta | `ValidadorGrafos` |
| `GRAFO_V1` y `REFERENCIA_V1` | Formato de los TXT | Generador (Estudiante 3) y parser (Estudiante 1) |

El punto de conexion del validador es la constante
`VALIDADOR_POR_DEFECTO` de `JuezCli` o la opcion
`--validador`. Ver guia-estudiante2.md.
