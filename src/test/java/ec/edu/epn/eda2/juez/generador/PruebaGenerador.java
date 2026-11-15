package ec.edu.epn.eda2.juez.generador;

import ec.edu.epn.eda2.juez.core.modelo.Arista;
import ec.edu.epn.eda2.juez.core.modelo.CasoGrafo;
import ec.edu.epn.eda2.juez.core.modelo.TipoGrafo;
import ec.edu.epn.eda2.juez.generador.servicio.BateriaCasos;
import ec.edu.epn.eda2.juez.generador.servicio.GeneradorAleatorio;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

//Ejecuta pruebas automaticas sobre los grafos generados.
public final class PruebaGenerador {

    private static int correctas = 0;
    private static int fallidas = 0;

    private static final List<CasoGrafo> CASOS =
            BateriaCasos.crearBateria();

    //Impide crear instancias de la clase de pruebas.
    private PruebaGenerador() {
    }

    //Ejecuta todas las pruebas del generador.
    public static void main(String[] args) {

        System.out.println("PRUEBAS DEL GENERADOR DE GRAFOS");
        System.out.println("--------------------------------");

        //Comprueba la cantidad total de casos.
        probar("Cantidad de casos", () -> {
            comprobar(CASOS.size() == 13, "Se esperaban 13 casos.");
        });

        //Comprueba el grafo conexo.
        probar("01 - Grafo conexo", () -> {
            CasoGrafo g = obtener("01_conexo");
            comprobar(esConexo(g), "El grafo debe ser conexo.");
            comprobar(g.aristas().size() == 7, "Cantidad incorrecta.");
        });

        //Comprueba el grafo desconectado.
        probar("02 - Grafo desconectado", () -> {
            comprobar(!esConexo(obtener("02_desconexo")),
                    "El grafo debe ser desconectado.");
        });

        //Comprueba que el DAG no tenga ciclos.
        probar("03 - DAG", () -> {
            comprobar(esDAG(obtener("03_dag")),
                    "El grafo no debe contener ciclos.");
        });

        //Comprueba la existencia de un ciclo dirigido.
        probar("04 - Ciclo dirigido", () -> {
            comprobar(!esDAG(obtener("04_ciclo_dirigido")),
                    "Se esperaba un ciclo dirigido.");
        });

        //Comprueba que existan varios pesos minimos iguales.
        probar("05 - MST con empates", () -> {
            CasoGrafo g = obtener("05_mst_empates");

            long iguales = g.aristas().stream()
                    .filter(a -> a.peso() == 1)
                    .count();

            comprobar(iguales == 4,
                    "Se esperaban cuatro aristas de peso 1.");
        });

        //Comprueba un grafo con vertices aislados.
        probar("06 - Vertices aislados", () -> {
            CasoGrafo g = obtener("06_aislados");
            comprobar(g.vertices() == 5 && g.aristas().isEmpty(),
                    "El grafo debe tener cinco vertices sin aristas.");
        });

        //Comprueba el caso limite de un vertice.
        probar("07 - Un vertice", () -> {
            CasoGrafo g = obtener("07_un_vertice");
            comprobar(g.vertices() == 1 && g.aristas().isEmpty(),
                    "El grafo debe tener un solo vertice.");
        });

        //Comprueba un ciclo en un grafo no dirigido.
        probar("08 - Ciclo no dirigido", () -> {
            CasoGrafo g = obtener("08_ciclo_no_dirigido");

            comprobar(esConexo(g) &&
                    g.aristas().size() >= g.vertices(),
                    "Se esperaba un grafo conexo con ciclo.");
        });

        //Comprueba un DAG desconectado.
        probar("09 - DAG desconectado", () -> {
            CasoGrafo g = obtener("09_dag_desconexo");

            comprobar(esDAG(g) && !esConexo(g),
                    "Debe ser un DAG desconectado.");
        });

        //Comprueba que el grafo disperso sea conexo.
        probar("10 - Grafo disperso", () -> {
            CasoGrafo g = obtener("10_disperso");

            comprobar(esConexo(g) &&
                    g.aristas().size() == g.vertices() - 1,
                    "El grafo disperso no cumple las condiciones.");
        });

        //Comprueba la densidad y conectividad.
        probar("11 - Grafo denso", () -> {
            CasoGrafo g = obtener("11_denso");

            comprobar(esConexo(g) && g.aristas().size() == 53,
                    "El grafo denso no cumple las condiciones.");
        });

        //Comprueba la cantidad de aristas del caso intermedio.
        probar("12 - Grafo aleatorio medio", () -> {
            CasoGrafo g = obtener("12_aleatorio_medio");

            comprobar(esConexo(g) && g.aristas().size() == 22,
                    "El grafo intermedio es incorrecto.");
        });

        //Comprueba que el grafo aleatorio contenga ciclos.
        probar("13 - Grafo aleatorio con ciclos", () -> {
            CasoGrafo g = obtener("13_ciclo_aleatorio");

            comprobar(esConexo(g) &&
                    g.aristas().size() > g.vertices() - 1,
                    "Se esperaba un grafo conexo con ciclos.");
        });

        //Comprueba que una semilla produzca los mismos resultados.
        probar("Generacion reproducible", () -> {

            GeneradorAleatorio a = new GeneradorAleatorio(12345L);
            GeneradorAleatorio b = new GeneradorAleatorio(12345L);

            CasoGrafo primero = a.generarDenso("prueba", 12, 20);
            CasoGrafo segundo = b.generarDenso("prueba", 12, 20);

            comprobar(primero.aristas().equals(segundo.aristas()),
                    "La semilla no genero los mismos resultados.");
        });

        //Verifica que los archivos TXT coincidan con los grafos.
        probar("Archivos TXT de entrada", () -> {

            for (CasoGrafo g : CASOS) {
                try {
                    Path archivo = Path.of(
                            "casos", "entrada", g.nombre() + ".txt"
                    );

                    comprobar(Files.isRegularFile(archivo),
                            "Archivo inexistente: " + archivo);

                    List<String> lineas = Files.readAllLines(
                            archivo, StandardCharsets.UTF_8
                    );

                    String encabezado = g.tipo().codigo() + " "
                            + g.vertices() + " " + g.aristas().size();

                    comprobar(!lineas.isEmpty() &&
                            lineas.get(0).equals(encabezado),
                            "Encabezado incorrecto: " + archivo);

                    comprobar(lineas.size() == g.aristas().size() + 1,
                            "Numero de lineas incorrecto: " + archivo);

                    //Compara cada arista escrita con el grafo original.
                    for (int i = 0; i < g.aristas().size(); i++) {
                        Arista a = g.aristas().get(i);

                        String esperada = a.origen() + " "
                                + a.destino() + " " + a.peso();

                        comprobar(lineas.get(i + 1).equals(esperada),
                                "Arista incorrecta: " + archivo);
                    }

                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        });

        //Comprueba que no se acepten aristas duplicadas.
        probar("Rechazo de aristas duplicadas", () -> {

            boolean rechazado = false;

            try {
                new CasoGrafo(
                    "invalido",
                    TipoGrafo.NO_DIRIGIDO,
                    3,
                    List.of(
                        new Arista(0, 1, 2),
                        new Arista(1, 0, 4)
                    )
                );
            } catch (IllegalArgumentException e) {
                rechazado = true;
            }

            comprobar(rechazado,
                    "El modelo acepto aristas duplicadas.");
        });

        //Muestra el resumen de las pruebas.
        System.out.println("--------------------------------");
        System.out.println("Correctas: " + correctas);
        System.out.println("Fallidas: " + fallidas);

        if (fallidas > 0) {
            System.exit(1);
        }
    }

    //Ejecuta una prueba y registra su resultado.
    private static void probar(String nombre, Runnable prueba) {

        try {
            prueba.run();
            correctas++;
            System.out.println("[OK] " + nombre);

        } catch (RuntimeException | AssertionError e) {
            fallidas++;
            System.out.println("[ERROR] " + nombre);
            System.out.println("  " + e.getMessage());
        }
    }

    //Comprueba una condicion y genera un error cuando falla.
    private static void comprobar(boolean condicion, String mensaje) {

        if (!condicion) {
            throw new AssertionError(mensaje);
        }
    }

    //Busca un grafo por su nombre.
    private static CasoGrafo obtener(String nombre) {

        return CASOS.stream()
                .filter(g -> g.nombre().equals(nombre))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Caso inexistente: " + nombre)
                );
    }

    //Comprueba conectividad ignorando la direccion de las aristas.
    private static boolean esConexo(CasoGrafo grafo) {

        int n = grafo.vertices();
        List<List<Integer>> vecinos = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            vecinos.add(new ArrayList<>());
        }

        //Construye conexiones en ambos sentidos.
        for (Arista a : grafo.aristas()) {
            vecinos.get(a.origen()).add(a.destino());
            vecinos.get(a.destino()).add(a.origen());
        }

        boolean[] visitado = new boolean[n];
        ArrayDeque<Integer> cola = new ArrayDeque<>();

        cola.add(0);
        visitado[0] = true;
        int total = 0;

        //Recorre los vertices alcanzables desde el vertice cero.
        while (!cola.isEmpty()) {
            int actual = cola.remove();
            total++;

            for (int siguiente : vecinos.get(actual)) {
                if (!visitado[siguiente]) {
                    visitado[siguiente] = true;
                    cola.add(siguiente);
                }
            }
        }

        return total == n;
    }

    //Comprueba que un grafo dirigido sea aciclico.
    private static boolean esDAG(CasoGrafo grafo) {

        if (grafo.tipo() != TipoGrafo.DIRIGIDO) {
            return false;
        }

        int n = grafo.vertices();
        int[] gradoEntrada = new int[n];
        List<List<Integer>> vecinos = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            vecinos.add(new ArrayList<>());
        }

        //Calcula el grado de entrada de cada vertice.
        for (Arista a : grafo.aristas()) {
            vecinos.get(a.origen()).add(a.destino());
            gradoEntrada[a.destino()]++;
        }

        ArrayDeque<Integer> cola = new ArrayDeque<>();

        //Agrega los vertices que no tienen predecesores.
        for (int i = 0; i < n; i++) {
            if (gradoEntrada[i] == 0) {
                cola.add(i);
            }
        }

        int procesados = 0;

        //Elimina logicamente las aristas mediante los grados.
        while (!cola.isEmpty()) {
            int actual = cola.remove();
            procesados++;

            for (int siguiente : vecinos.get(actual)) {
                gradoEntrada[siguiente]--;

                if (gradoEntrada[siguiente] == 0) {
                    cola.add(siguiente);
                }
            }
        }

        //Si se procesaron todos los vertices, no existen ciclos.
        return procesados == n;
    }
}