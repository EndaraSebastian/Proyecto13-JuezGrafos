# Proyecto 13 - Manual de uso

## 1. Introduccion

El Proyecto 13 consiste en desarrollar un framework
en Java para evaluar algoritmos de grafos.

El sistema tiene como objetivo cargar grafos,
ejecutar algoritmos de estudiantes, verificar
sus resultados y medir su rendimiento.

Los algoritmos evaluados son:

- DFS: recorrido en profundidad.
- BFS: recorrido en anchura.
- Ordenamiento topologico.
- MST: arbol de expansion minima.

Actualmente se encuentra implementado el modulo
de generacion de grafos y referencias.

## 2. Requisitos

Para utilizar el modulo actual se necesita:

- Java JDK 17 o superior.
- Visual Studio Code o un editor compatible.
- Terminal PowerShell en Windows.
- Git para el control de versiones.

No se necesitan librerias externas para
compilar el modulo implementado.

## 3. Abrir el proyecto

Abrir la carpeta Proyecto13-JuezGrafos
desde Visual Studio Code.

En la terminal, comprobar la ubicacion:

```powershell
Get-Location
```

La terminal debe encontrarse dentro
de la carpeta principal del repositorio.

## 4. Compilar el proyecto

Crear la carpeta para los archivos compilados:

```powershell
New-Item -ItemType Directory -Force -Path out | Out-Null
```

Obtener los archivos Java principales:

```powershell
$archivos = @(Get-ChildItem "src\main\java" -Recurse -Filter "*.java" |
    Where-Object { $_.Length -gt 0 } |
    Select-Object -ExpandProperty FullName)
```

Compilar:

```powershell
javac -encoding UTF-8 -d out $archivos
```

Si la terminal no muestra errores, la compilacion
se completo correctamente.

## 5. Generar grafos

Ejecutar el siguiente comando:

```powershell
java -cp out ec.edu.epn.eda2.juez.Main generar
```

El programa crea 13 archivos TXT en:

casos/entrada/

La bateria incluye grafos:

- Conexos.
- Desconectados.
- Dirigidos aciclicos (DAG).
- Dirigidos con ciclos.
- Con pesos iguales.
- Con vertices aislados.
- Con un solo vertice.
- Densos y dispersos.
- Aleatorios reproducibles.

Advertencia: el comando generar reemplaza
los TXT de entrada que tengan el mismo nombre.

## 6. Generar respuestas de referencia

Ejecutar:

```powershell
java -cp out ec.edu.epn.eda2.juez.Main referencias
```

Se generan hasta 13 archivos en:

casos/referencia/

Cada referencia contiene resultados de DFS,
BFS, orden topologico y MST, segun corresponda.

Los archivos existentes se conservan
cuando se utiliza el comando normal.

Para regenerar las referencias existentes:

```powershell
java -cp out ec.edu.epn.eda2.juez.Main referencias --sobrescribir
```

Se recomienda regenerar las referencias
si se modificaron los grafos de entrada.

Importante: el calculo de referencias parte
de la bateria definida en Java, no de una
lectura independiente de los TXT de entrada.

## 7. Formato de los archivos de entrada

El formato utilizado es:

TIPO V E
ORIGEN DESTINO PESO

Ejemplo:

```text
U 3 2
0 1 4
1 2 7
```

Significado:

- U: grafo no dirigido.
- D: grafo dirigido.
- V: cantidad de vertices.
- E: cantidad de aristas.

Los vertices empiezan en 0.
Los pesos son enteros positivos.
No se admiten aristas duplicadas ni bucles propios.

## 8. Formato de las referencias

Ejemplo:

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

BFS_ORDEN: orden del recorrido en anchura.
BFS_DISTANCIAS: distancias en numero de aristas.
DFS_ORDEN: orden del recorrido en profundidad.
TOPO_ESTADO: ACICLICO, CICLO o NO_APLICA.
TOPO_ORDEN: orden topologico de referencia.
MST_PESO: peso minimo total del bosque.
MST_COMPONENTES: cantidad de componentes.

NO_APLICA indica que el algoritmo no
corresponde al tipo de grafo.

## 9. Compilar las pruebas

Compilar las clases principales y las pruebas:

```powershell
$archivos = @(Get-ChildItem "src" -Recurse -Filter "*.java" |
    Where-Object { $_.Length -gt 0 } |
    Select-Object -ExpandProperty FullName)

javac -encoding UTF-8 -d out $archivos
```

## 10. Ejecutar pruebas del generador

Ejecutar:

```powershell
java -cp out ec.edu.epn.eda2.juez.generador.PruebaGenerador
```

Resultado verificado:

Correctas: 17
Fallidas: 0

Estas pruebas comprueban propiedades,
generacion reproducible y archivos de entrada.

## 11. Ejecutar pruebas de referencias

Ejecutar:

```powershell
java -cp out ec.edu.epn.eda2.juez.generador.PruebaReferencias
```

Resultado verificado:

Correctas: 14
Fallidas: 0

Estas pruebas revisan los archivos TXT
y contrastan los resultados de referencia.

Para MST se utiliza Prim como comprobacion
independiente del calculo con Kruskal.

## 12. Mostrar la ayuda

Ejecutar:

```powershell
java -cp out ec.edu.epn.eda2.juez.Main ayuda
```

El programa muestra los comandos disponibles.

## 13. Problemas frecuentes

### Java no reconocido

Comprobar la instalacion:

```powershell
java -version
javac -version
```

### Clase principal no encontrada

Comprobar que el proyecto fue compilado
y que se utiliza el nombre completo del paquete.

### Archivos TXT no encontrados

Ejecutar primero el comando generar.

### Una prueba de referencia falla

Revisar si los grafos de entrada coinciden
con las referencias almacenadas.

Si fueron modificados, regenerar ambos
con los comandos correspondientes.

### Errores de ubicacion

Ejecutar los comandos desde la carpeta
raiz del repositorio.

## 14. Limitaciones actuales

El modulo desarrollado permite generar
grafos, referencias y ejecutar pruebas.

Ya integrados en el core (Estudiante 1):

- Parser de grafos y de referencias.
- Contrato de submission de estudiantes.
- Ejecucion de algoritmos entregados.
- Medicion de tiempo.
- Reporte integrado de resultados.

Integrados por el Estudiante 2:

- Validador de soluciones (`ValidadorGrafos`).
- Submission de ejemplo (`SolucionModelo`).

La memoria se mide como bytes asignados por la solucion (ver core-juez.md).

## 15. Documentos relacionados

- contrato-formatos.md
- arquitectura.md

## 16. Conclusion

El modulo implementado permite construir
una bateria de grafos y generar sus respuestas
de referencia de manera reproducible.

Las pruebas automatizadas contribuyen
a comprobar el funcionamiento del modulo
antes de integrarlo con el juez completo.

## Ampliacion: grafos de rendimiento

### Objetivo

La bateria de rendimiento permite trabajar con
grafos de mayor tamano y diferentes densidades.

Se mantienen separados los 13 casos funcionales
y los 5 casos de rendimiento.

### Generar los grafos grandes

Ejecutar desde la raiz del repositorio:

```powershell
java -cp out ec.edu.epn.eda2.juez.Main rendimiento
```

Este comando genera cinco archivos TXT en:

`casos/rendimiento/`

### Generar las referencias

Ejecutar:

```powershell
java -cp out ec.edu.epn.eda2.juez.Main referencias-rendimiento
```

Las respuestas se almacenan en:

`casos/referencias-rendimiento/`

Para regenerar las referencias existentes:

```powershell
java -cp out ec.edu.epn.eda2.juez.Main referencias-rendimiento --sobrescribir
```

Esta opcion reemplaza los archivos de referencias
de rendimiento previamente generados.

### Compilar las clases Java

Desde PowerShell en la raiz del repositorio:

```powershell
$archivos = @(Get-ChildItem "src" -Recurse -Filter "*.java" |
    Select-Object -ExpandProperty FullName)

New-Item -ItemType Directory -Force -Path out | Out-Null

javac -encoding UTF-8 -d out $archivos
```

### Ejecutar las pruebas

Primero debe haberse compilado el proyecto.

Generador funcional:

```powershell
java -cp out ec.edu.epn.eda2.juez.generador.PruebaGenerador
```

Referencias funcionales:

```powershell
java -cp out ec.edu.epn.eda2.juez.generador.PruebaReferencias
```

Grafos de rendimiento:

```powershell
java -cp out ec.edu.epn.eda2.juez.generador.PruebaRendimiento
```

Referencias de rendimiento:

```powershell
java -cp out ec.edu.epn.eda2.juez.generador.PruebaReferenciasRendimiento
```

Casos negativos del modelo:

```powershell
java -cp out ec.edu.epn.eda2.juez.generador.PruebaCasosNegativos
```

### Resultados verificados

| Prueba | Correctas | Fallidas |
|---|---:|---:|
| Generador | 17 | 0 |
| Referencias funcionales | 14 | 0 |
| Grafos de rendimiento | 7 | 0 |
| Referencias de rendimiento | 5 | 0 |
| Casos negativos del modelo | 12 | 0 |
| Total | 55 | 0 |

Las pruebas se ejecutaron correctamente
en conjunto durante el desarrollo.

### Consideraciones

Los grafos grandes son actualmente
no dirigidos y ponderados.

La validacion de soluciones estudiantiles
y la medicion de tiempo y memoria
estan cubiertas por el juez (comando `juzgar`).

La documentacion detallada de los casos
se encuentra en `docs/rendimiento.md`.

## Juzgar una solucion de estudiante

Compilar todo `src` (seccion 9) y ejecutar desde la raiz:

```powershell
java -cp out ec.edu.epn.eda2.juez.Main juzgar <ClaseSolucion> --casos todos --csv resultados.csv
```

`<ClaseSolucion>` es el nombre completo (con paquete) de una
clase publica que implemente `SolucionEstudiante`.

Para cada caso y algoritmo, el reporte muestra el estado
(CORRECTO, INCORRECTO, SIN_VALIDAR, NO_APLICA o un error) y el
tiempo medido. Si algo falla, el programa termina con codigo 1.

Antes de juzgar deben existir los casos y las referencias
(comandos generar y referencias, ya incluidos en el repositorio).

Opciones, estados y formato del reporte: `docs/core-juez.md`.
Validador y submission de ejemplo: `docs/validador.md`.
