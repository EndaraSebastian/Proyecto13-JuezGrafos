package ec.edu.epn.eda2.juez.core;

import ec.edu.epn.eda2.juez.core.ejecucion.CasoPrueba;
import ec.edu.epn.eda2.juez.core.ejecucion.CatalogoCasos;
import ec.edu.epn.eda2.juez.core.modelo.Arista;
import ec.edu.epn.eda2.juez.core.modelo.CasoGrafo;
import ec.edu.epn.eda2.juez.core.modelo.GrafoCargado;
import ec.edu.epn.eda2.juez.core.modelo.TipoGrafo;
import ec.edu.epn.eda2.juez.core.modelo.Vecino;
import ec.edu.epn.eda2.juez.core.parser.ErrorFormatoException;
import ec.edu.epn.eda2.juez.core.parser.LectorGrafoTxt;
import ec.edu.epn.eda2.juez.core.parser.LectorReferenciaTxt;
import ec.edu.epn.eda2.juez.core.parser.ReferenciaCaso;
import ec.edu.epn.eda2.juez.core.parser.ReferenciaCaso.EstadoTopologico;
import ec.edu.epn.eda2.juez.generador.io.EscritorGrafoTxt;
import ec.edu.epn.eda2.juez.generador.servicio.BateriaCasos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ec.edu.epn.eda2.juez.core.MiniPrueba.carpetaTemporal;
import static ec.edu.epn.eda2.juez.core.MiniPrueba.exigir;
import static ec.edu.epn.eda2.juez.core.MiniPrueba.probar;

//Pruebas del parser de grafos (GRAFO_V1), de la carga en memoria
//y del lector de referencias (REFERENCIA_V1).
//Ejecutar desde la raiz del proyecto (usa la carpeta casos/).
public final class PruebaParser {

    //Referencia valida minima; las pruebas la modifican.
    private static final List<String> BASE = List.of(
        "FORMATO=REFERENCIA_V1",
        "CASO=demo",
        "INICIO=0",
        "BFS_ORDEN=0,1",
        "BFS_DISTANCIAS=0,1",
        "DFS_ORDEN=0,1",
        "TOPO_ESTADO=NO_APLICA",
        "TOPO_ORDEN=NO_APLICA",
        "MST_PESO=4",
        "MST_COMPONENTES=1"
    );

    //Impide crear instancias de esta clase de prueba.
    private PruebaParser() {
    }

    public static void main(String[] args) throws Exception {

        System.out.println("PRUEBAS DEL PARSER");
        System.out.println("--------------------------------");

        pruebasDeCasosReales();
        pruebasDeMemoria();
        pruebasDeErroresDeGrafo();
        pruebasDeDisco();
        pruebasDeReferencia();

        MiniPrueba.terminar();
    }

    //Los archivos generados por el Estudiante 3 deben leerse bien.
    private static void pruebasDeCasosReales() throws IOException {

        List<CasoPrueba> funcionales = CatalogoCasos.descubrir(
            CatalogoCasos.ENTRADA_FUNCIONAL,
            CatalogoCasos.REFERENCIA_FUNCIONAL
        );

        List<CasoPrueba> rendimiento = CatalogoCasos.descubrir(
            CatalogoCasos.ENTRADA_RENDIMIENTO,
            CatalogoCasos.REFERENCIA_RENDIMIENTO
        );

        probar("Lee los 13 casos funcionales", () -> {

            exigir(funcionales.size() == 13,
                "se esperaban 13 casos y hay " + funcionales.size());

            for (CasoPrueba caso : funcionales) {

                GrafoCargado grafo = LectorGrafoTxt.leer(caso.entrada());

                exigir(grafo.nombre().equals(caso.nombre()),
                    "nombre incorrecto en " + caso.nombre());
            }
        });

        probar("Lee los 5 casos de rendimiento", () -> {

            exigir(rendimiento.size() == 5,
                "se esperaban 5 casos y hay " + rendimiento.size());

            for (CasoPrueba caso : rendimiento) {
                LectorGrafoTxt.leer(caso.entrada());
            }
        });

        probar("Caso 01_conexo: tipo, vertices y aristas", () -> {

            GrafoCargado g = LectorGrafoTxt.leer(
                CatalogoCasos.ENTRADA_FUNCIONAL.resolve("01_conexo.txt")
            );

            exigir(g.tipo() == TipoGrafo.NO_DIRIGIDO, "tipo");
            exigir(g.numVertices() == 5, "vertices");
            exigir(g.numAristas() == 7, "aristas");
        });

        probar("Caso 04_ciclo_dirigido es dirigido (V=5, E=5)", () -> {

            GrafoCargado g = LectorGrafoTxt.leer(
                CatalogoCasos.ENTRADA_FUNCIONAL
                    .resolve("04_ciclo_dirigido.txt")
            );

            exigir(g.esDirigido(), "debia ser dirigido");
            exigir(g.numVertices() == 5 && g.numAristas() == 5,
                "V=" + g.numVertices() + " E=" + g.numAristas());
        });

        probar("Caso 17: 500 vertices y 99800 aristas", () -> {

            GrafoCargado g = LectorGrafoTxt.leer(
                CatalogoCasos.ENTRADA_RENDIMIENTO
                    .resolve("17_rendimiento_500_denso.txt")
            );

            exigir(g.numVertices() == 500, "vertices");
            exigir(g.numAristas() == 99800, "aristas");
        });

        probar("Las 18 referencias son coherentes con su grafo", () -> {

            List<CasoPrueba> todos = new ArrayList<>(funcionales);
            todos.addAll(rendimiento);

            exigir(todos.size() == 18, "se esperaban 18 casos");

            for (CasoPrueba caso : todos) {

                GrafoCargado grafo = LectorGrafoTxt.leer(caso.entrada());
                ReferenciaCaso ref =
                        LectorReferenciaTxt.leer(caso.referencia());

                ref.verificarCoherencia(grafo);
            }
        });
    }

    //Estructura en memoria: listas de adyacencia.
    private static void pruebasDeMemoria() {

        probar("Adyacencia no dirigida: simetrica y ordenada", () -> {

            GrafoCargado g = LectorGrafoTxt.leer(
                CatalogoCasos.ENTRADA_FUNCIONAL.resolve("01_conexo.txt")
            );

            int total = 0;

            for (int v = 0; v < g.numVertices(); v++) {

                List<Vecino> vecinos = g.vecinos(v);
                total += vecinos.size();

                for (int i = 1; i < vecinos.size(); i++) {
                    exigir(vecinos.get(i - 1).destino()
                            < vecinos.get(i).destino(),
                        "vecinos de " + v + " sin ordenar");
                }
            }

            exigir(total == 2 * g.numAristas(),
                "suma de grados " + total);

            for (Arista a : g.aristas()) {
                exigir(g.vecinos(a.origen())
                        .contains(new Vecino(a.destino(), a.peso())),
                    "falta ida " + a);
                exigir(g.vecinos(a.destino())
                        .contains(new Vecino(a.origen(), a.peso())),
                    "falta vuelta " + a);
            }
        });

        probar("Adyacencia dirigida: solo sucesores", () -> {

            GrafoCargado g = LectorGrafoTxt.leer(
                CatalogoCasos.ENTRADA_FUNCIONAL
                    .resolve("04_ciclo_dirigido.txt")
            );

            //El archivo tiene la arista 0 -> 1 (peso 1) y no 1 -> 0.
            exigir(g.vecinos(0).contains(new Vecino(1, 1)),
                "falta 0 -> 1");
            exigir(g.vecinos(1).stream().noneMatch(v -> v.destino() == 0),
                "no debe existir 1 -> 0");

            int total = 0;

            for (int v = 0; v < g.numVertices(); v++) {
                total += g.vecinos(v).size();
            }

            exigir(total == g.numAristas(), "suma de grados " + total);
        });

        probar("Las listas de adyacencia son inmutables", () -> {

            GrafoCargado g = LectorGrafoTxt.leer(
                CatalogoCasos.ENTRADA_FUNCIONAL.resolve("01_conexo.txt")
            );

            try {
                g.vecinos(0).add(new Vecino(4, 9));
            } catch (UnsupportedOperationException e) {
                return;
            }

            throw new AssertionError("se pudo modificar la lista");
        });
    }

    //Archivos invalidos: debe fallar con el numero de linea correcto.
    private static void pruebasDeErroresDeGrafo() {

        rechazar("Archivo vacio", 0);
        rechazar("Solo lineas en blanco", 0, "", "   ");
        rechazar("TIPO invalido", 1, "X 3 0");
        rechazar("TIPO en minuscula", 1, "u 3 0");
        rechazar("V igual a cero", 1, "U 0 0");
        rechazar("E negativo", 1, "U 3 -1");
        rechazar("Encabezado con 2 campos", 1, "U 3");
        rechazar("V no entero", 1, "U tres 0");
        rechazar("Faltan aristas", 0, "U 3 2", "0 1 4");
        rechazar("Sobran lineas", 3, "U 3 1", "0 1 4", "1 2 5");
        rechazar("Arista con 2 campos", 2, "U 3 1", "0 1");
        rechazar("Arista con 4 campos", 2, "U 3 1", "0 1 4 9");
        rechazar("Vertice fuera de rango", 2, "U 3 1", "0 3 4");
        rechazar("Vertice negativo", 2, "U 3 1", "-1 2 4");
        rechazar("Bucle propio", 2, "U 3 1", "1 1 4");
        rechazar("Peso cero", 2, "U 3 1", "0 1 0");
        rechazar("Peso negativo", 2, "U 3 1", "0 1 -3");
        rechazar("Peso decimal", 2, "U 3 1", "0 1 1.5");
        rechazar("Peso no numerico", 2, "U 3 1", "0 1 abc");
        rechazar("Peso fuera del rango de int", 2,
            "U 3 1", "0 1 99999999999");
        rechazar("Arista duplicada no dirigida", 3,
            "U 3 2", "0 1 2", "1 0 5");
        rechazar("Arista duplicada dirigida", 3,
            "D 3 2", "0 1 2", "0 1 7");

        probar("Aristas dirigidas opuestas son validas", () -> {

            CasoGrafo c = LectorGrafoTxt.parsear("demo", "demo.txt",
                List.of("D 2 2", "0 1 2", "1 0 4"));

            exigir(c.aristas().size() == 2, "debian ser 2 aristas");
        });

        probar("Acepta lineas en blanco y espacios extra", () -> {

            CasoGrafo c = LectorGrafoTxt.parsear("demo", "demo.txt",
                List.of("", "  U   3   2  ", "", "0 1 4", "   ",
                        "1\t2\t7", ""));

            exigir(c.vertices() == 3 && c.aristas().size() == 2,
                "V=" + c.vertices() + " E=" + c.aristas().size());
        });

        probar("Acepta BOM UTF-8 al inicio", () -> {

            CasoGrafo c = LectorGrafoTxt.parsear("demo", "demo.txt",
                List.of("﻿U 2 1", "0 1 3"));

            exigir(c.vertices() == 2, "vertices");
        });

        probar("Acepta un grafo sin aristas", () -> {

            CasoGrafo c = LectorGrafoTxt.parsear("demo", "demo.txt",
                List.of("U 1 0"));

            exigir(c.vertices() == 1 && c.aristas().isEmpty(), "grafo");
        });

        probar("Rechaza un nombre de caso invalido", () -> {

            try {
                LectorGrafoTxt.parsear("mal nombre", "x.txt",
                    List.of("U 1 0"));
            } catch (ErrorFormatoException e) {
                return;
            }

            throw new AssertionError("se acepto el nombre");
        });
    }

    //Lectura desde disco y ida y vuelta con el escritor.
    private static void pruebasDeDisco() {

        probar("Ida y vuelta: escritor y lector dan el mismo grafo", () -> {

            Path carpeta = carpetaTemporal();

            for (CasoGrafo original : BateriaCasos.crearBateria()) {

                Path archivo = EscritorGrafoTxt.guardar(original, carpeta);
                CasoGrafo leido = LectorGrafoTxt.leerCaso(archivo);

                exigir(leido.nombre().equals(original.nombre()),
                    "nombre de " + original.nombre());
                exigir(leido.tipo() == original.tipo(),
                    "tipo de " + original.nombre());
                exigir(leido.vertices() == original.vertices(),
                    "vertices de " + original.nombre());
                exigir(leido.aristas().equals(original.aristas()),
                    "aristas de " + original.nombre());
            }
        });

        probar("El nombre del caso sale del nombre del archivo", () -> {

            Path archivo = carpetaTemporal().resolve("mi-caso_1.txt");
            Files.writeString(archivo, "U 1 0\n");

            exigir("mi-caso_1".equals(LectorGrafoTxt.leerCaso(archivo)
                    .nombre()),
                "nombre");
        });

        probar("Rechaza un archivo que no es UTF-8", () -> {

            Path archivo = carpetaTemporal().resolve("malo.txt");
            Files.write(archivo, new byte[] {'U', ' ', (byte) 0xFF});

            try {
                LectorGrafoTxt.leerCaso(archivo);
            } catch (ErrorFormatoException e) {
                return;
            }

            throw new AssertionError("se acepto un archivo no UTF-8");
        });

        probar("Archivo inexistente produce IOException", () -> {

            try {
                LectorGrafoTxt.leerCaso(
                    carpetaTemporal().resolve("no_existe.txt")
                );
            } catch (NoSuchFileException e) {
                return;
            }

            throw new AssertionError("no se informo el archivo faltante");
        });
    }

    //Lector de referencias REFERENCIA_V1.
    private static void pruebasDeReferencia() {

        probar("Referencia 01_conexo: valores esperados", () -> {

            ReferenciaCaso r = LectorReferenciaTxt.leer(
                CatalogoCasos.REFERENCIA_FUNCIONAL.resolve("01_conexo.txt")
            );

            exigir(r.caso().equals("01_conexo"), "caso");
            exigir(r.inicio() == 0, "inicio");
            exigir(Arrays.equals(r.bfsOrden(), new int[] {0, 1, 2, 3, 4}),
                "bfs orden");
            exigir(Arrays.equals(r.bfsDistancias(),
                    new int[] {0, 1, 1, 2, 2}),
                "bfs distancias");
            exigir(Arrays.equals(r.dfsOrden(), new int[] {0, 1, 2, 3, 4}),
                "dfs orden");
            exigir(r.topoEstado() == EstadoTopologico.NO_APLICA,
                "topo estado");
            exigir(r.topoOrden().length == 0, "topo orden");
            exigir(r.mstAplica() && r.mstPeso() == 10
                    && r.mstComponentes() == 1,
                "mst");
        });

        probar("Referencia 03_dag: aciclico, sin MST", () -> {

            ReferenciaCaso r = LectorReferenciaTxt.leer(
                CatalogoCasos.REFERENCIA_FUNCIONAL.resolve("03_dag.txt")
            );

            exigir(r.topoEstado() == EstadoTopologico.ACICLICO, "estado");
            exigir(r.topoOrden().length == 6, "orden de 6 vertices");
            exigir(!r.mstAplica(), "el MST no aplica");

            try {
                r.mstPeso();
            } catch (IllegalStateException e) {
                return;
            }

            throw new AssertionError("mstPeso debia fallar");
        });

        probar("Referencia 04_ciclo_dirigido: CICLO", () -> {

            ReferenciaCaso r = LectorReferenciaTxt.leer(
                CatalogoCasos.REFERENCIA_FUNCIONAL
                    .resolve("04_ciclo_dirigido.txt")
            );

            exigir(r.topoEstado() == EstadoTopologico.CICLO, "estado");
            exigir(r.topoOrden().length == 0, "sin orden");
        });

        probar("Referencia base valida se acepta", () -> {
            LectorReferenciaTxt.parsear("demo.txt", BASE);
        });

        probar("Referencia ignora claves desconocidas", () -> {
            LectorReferenciaTxt.parsear("demo.txt", con("EXTRA=1"));
        });

        probar("Referencia acepta que falte TOPO_ORDEN", () -> {
            LectorReferenciaTxt.parsear("demo.txt", sin("TOPO_ORDEN"));
        });

        rechazarReferencia("Falta FORMATO", sin("FORMATO"));
        rechazarReferencia("FORMATO distinto",
            reemplazar("FORMATO", "FORMATO=REFERENCIA_V9"));
        rechazarReferencia("Falta CASO", sin("CASO"));
        rechazarReferencia("Falta BFS_ORDEN", sin("BFS_ORDEN"));
        rechazarReferencia("Clave repetida", con("INICIO=1"));
        rechazarReferencia("Linea sin '='", con("basura"));
        rechazarReferencia("INICIO no entero",
            reemplazar("INICIO", "INICIO=a"));
        rechazarReferencia("INICIO negativo",
            reemplazar("INICIO", "INICIO=-1"));
        rechazarReferencia("Lista con valor no entero",
            reemplazar("BFS_ORDEN", "BFS_ORDEN=0,x"));
        rechazarReferencia("TOPO_ESTADO invalido",
            reemplazar("TOPO_ESTADO", "TOPO_ESTADO=TAL_VEZ"));
        rechazarReferencia("MST_PESO sin MST_COMPONENTES coherente",
            reemplazar("MST_COMPONENTES", "MST_COMPONENTES=NO_APLICA"));
        rechazarReferencia("MST_PESO negativo",
            reemplazar("MST_PESO", "MST_PESO=-2"));

        probar("CASO debe coincidir con el nombre del archivo", () -> {

            Path archivo = carpetaTemporal().resolve("otra.txt");
            Files.write(archivo, BASE);

            try {
                LectorReferenciaTxt.leer(archivo);
            } catch (ErrorFormatoException e) {
                return;
            }

            throw new AssertionError("se acepto un CASO distinto");
        });

        probar("Coherencia: referencia de otro grafo se detecta", () -> {

            GrafoCargado grafo = LectorGrafoTxt.leer(
                CatalogoCasos.ENTRADA_FUNCIONAL.resolve("02_desconexo.txt")
            );

            ReferenciaCaso otra = LectorReferenciaTxt.leer(
                CatalogoCasos.REFERENCIA_FUNCIONAL.resolve("01_conexo.txt")
            );

            try {
                otra.verificarCoherencia(grafo);
            } catch (IllegalArgumentException e) {
                return;
            }

            throw new AssertionError("no se detecto la incoherencia");
        });

        probar("Coherencia: distancias de largo incorrecto", () -> {

            GrafoCargado grafo = LectorGrafoTxt.leer(
                CatalogoCasos.ENTRADA_FUNCIONAL.resolve("01_conexo.txt")
            );

            ReferenciaCaso mala = new ReferenciaCaso(
                "01_conexo", 0,
                new int[] {0}, new int[] {0, 1}, new int[] {0},
                EstadoTopologico.NO_APLICA, new int[0], true, 10, 1
            );

            try {
                mala.verificarCoherencia(grafo);
            } catch (IllegalArgumentException e) {
                return;
            }

            throw new AssertionError("no se detecto la incoherencia");
        });
    }

    //Un grafo invalido debe fallar con ErrorFormatoException y la
    //linea indicada (0 cuando el error no pertenece a una linea).
    private static void rechazar(
            String nombre, int lineaEsperada, String... lineas) {

        probar(nombre, () -> {

            try {
                LectorGrafoTxt.parsear(
                    "caso", "caso.txt", List.of(lineas)
                );
            } catch (ErrorFormatoException e) {

                exigir(e.linea() == lineaEsperada,
                    "linea esperada " + lineaEsperada
                    + " y fue " + e.linea() + " (" + e.getMessage() + ")");

                return;
            }

            throw new AssertionError("se acepto un archivo invalido");
        });
    }

    //Una referencia invalida debe fallar con ErrorFormatoException.
    private static void rechazarReferencia(
            String nombre, List<String> lineas) {

        probar(nombre, () -> {

            try {
                LectorReferenciaTxt.parsear("demo.txt", lineas);
            } catch (ErrorFormatoException e) {
                return;
            }

            throw new AssertionError("se acepto una referencia invalida");
        });
    }

    //BASE sin las lineas de una clave.
    private static List<String> sin(String clave) {

        List<String> lineas = new ArrayList<>();

        for (String linea : BASE) {
            if (!linea.startsWith(clave + "=")) {
                lineas.add(linea);
            }
        }

        return lineas;
    }

    //BASE mas una linea al final.
    private static List<String> con(String linea) {

        List<String> lineas = new ArrayList<>(BASE);
        lineas.add(linea);
        return lineas;
    }

    //BASE con una clave reemplazada por otra linea.
    private static List<String> reemplazar(
            String clave, String nuevaLinea) {

        List<String> lineas = sin(clave);
        lineas.add(nuevaLinea);
        return lineas;
    }
}
