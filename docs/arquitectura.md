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

Pendiente de integrar:
- Parser de archivos TXT.
- Contrato de soluciones de estudiantes.
- Ejecutor y medicion de rendimiento.

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

Pendiente de implementar.

Comprobara los resultados de los algoritmos,
considerando soluciones alternativas validas.

### Submission

Pendiente de implementar.

Contendra un ejemplo de implementacion estudiantil
de DFS, BFS, orden topologico y MST.

### Reporte y medicion

Pendientes de implementar.

Registraran resultados de correccion, tiempos
de ejecucion y uso estimado de memoria.

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

    H --> J["Parser - pendiente"]
    J --> K["Ejecutor - pendiente"]
    L["Submission - pendiente"] --> K

    K --> M["Validador - pendiente"]
    I --> M

    K --> N["Medicion - pendiente"]
    M --> O["Reporte - pendiente"]
    N --> O
```

El modelo compartido del core es utilizado
por los componentes implementados en Java.

Los componentes identificados como pendientes
se integraran posteriormente.

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

El estudiante 1 integrara el parser,
el ejecutor y la medicion.

El estudiante 2 integrara el validador
y la submission de ejemplo.

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

Pendiente:
- Parser de entrada.
- Interfaz de submission.
- Ejecucion del codigo estudiantil.
- Validacion de soluciones.
- Medicion de rendimiento.
- Reporte integrado.