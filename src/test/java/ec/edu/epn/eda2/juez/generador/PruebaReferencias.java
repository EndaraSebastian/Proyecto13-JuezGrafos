package ec.edu.epn.eda2.juez.generador;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Stream;

//Comprueba las referencias sin utilizar CalculadorReferencias.
public final class PruebaReferencias {

    private static final Path ENTRADA =
            Path.of("casos", "entrada");

    private static final Path REFERENCIA =
            Path.of("casos", "referencia");

    private static int correctas;
    private static int fallidas;

    //Impide crear instancias de la clase de pruebas.
    private PruebaReferencias() {
    }

    //Ejecuta las comprobaciones sobre todos los archivos.
    public static void main(String[] args) throws IOException {

        List<Path> entradas = listarTxt(ENTRADA);
        List<Path> referencias = listarTxt(REFERENCIA);

        System.out.println("PRUEBAS INDEPENDIENTES DE REFERENCIAS");
        System.out.println("------------------------------------");

        //Comprueba que existan las trece parejas de archivos.
        probar("Cantidad y nombres de archivos", () -> {

            comprobar(entradas.size() == 13,
                    "Se esperaban 13 entradas.");

            comprobar(referencias.size() == 13,
                    "Se esperaban 13 referencias.");

            Set<String> nombres = new HashSet<>();

            for (Path p : entradas) {
                nombres.add(p.getFileName().toString());
            }

            for (Path p : referencias) {
                comprobar(nombres.remove(p.getFileName().toString()),
                        "Referencia sin entrada: " + p.getFileName());
            }

            comprobar(nombres.isEmpty(), "Faltan referencias.");
        });

        //Comprueba individualmente cada grafo.
        for (Path archivo : entradas) {

            String nombre = archivo.getFileName().toString();

            probar(nombre, () -> comprobarCaso(
                    archivo, REFERENCIA.resolve(nombre)
            ));
        }

        //Presenta el resumen de las pruebas.
        System.out.println("------------------------------------");
        System.out.println("Correctas: " + correctas);
        System.out.println("Fallidas: " + fallidas);

        if (fallidas > 0) {
            System.exit(1);
        }
    }

    //Lee el grafo y verifica sus respuestas.
    private static void comprobarCaso(
            Path entrada, Path referencia) throws IOException {

        Grafo grafo = leerGrafo(entrada);
        Map<String, String> esperado = leerReferencia(referencia);

        String nombre = entrada.getFileName().toString()
                .replaceFirst("\\.txt$", "");

        comprobar("REFERENCIA_V1".equals(esperado.get("FORMATO")),
                "Version de referencia incorrecta.");

        comprobar(nombre.equals(esperado.get("CASO")),
                "El nombre de la referencia no coincide.");

        int inicio = Integer.parseInt(valor(esperado, "INICIO"));

        comprobar(inicio >= 0 && inicio < grafo.vertices(),
                "Vertice inicial fuera de rango.");

        //Construye una representacion independiente del grafo.
        List<List<Integer>> vecinos = adyacencia(grafo);

        //Ejecuta las cuatro comprobaciones.
        comprobarBfs(vecinos, inicio, esperado);
        comprobarDfs(vecinos, inicio, esperado);
        comprobarTopologico(grafo, vecinos, esperado);
        comprobarMst(grafo, esperado);
    }

    //Lee y comprueba el formato del archivo de entrada.
    private static Grafo leerGrafo(Path archivo) throws IOException {

        List<String> lineas = Files.readAllLines(
                archivo, StandardCharsets.UTF_8
        );

        comprobar(!lineas.isEmpty(), "Archivo de entrada vacio.");

        String[] encabezado = lineas.get(0).trim().split("\\s+");

        comprobar(encabezado.length == 3, "Encabezado invalido.");

        boolean dirigido = encabezado[0].equals("D");

        comprobar(dirigido || encabezado[0].equals("U"),
                "Tipo de grafo desconocido.");

        int vertices = Integer.parseInt(encabezado[1]);
        int cantidad = Integer.parseInt(encabezado[2]);

        comprobar(vertices > 0 && cantidad >= 0,
                "Dimensiones invalidas.");

        comprobar(lineas.size() == cantidad + 1,
                "El numero de aristas no coincide.");

        List<Arista> aristas = new ArrayList<>();
        Set<String> claves = new HashSet<>();

        //Lee las conexiones y verifica sus extremos y pesos.
        for (int i = 1; i < lineas.size(); i++) {

            String[] partes = lineas.get(i).trim().split("\\s+");

            comprobar(partes.length == 3,
                    "Arista invalida en linea " + (i + 1));

            int origen = Integer.parseInt(partes[0]);
            int destino = Integer.parseInt(partes[1]);
            int peso = Integer.parseInt(partes[2]);

            comprobar(
                    origen >= 0 && destino >= 0
                    && origen < vertices && destino < vertices
                    && origen != destino && peso > 0,
                    "Arista fuera de contrato."
            );

            //Detecta conexiones duplicadas.
            int a = dirigido ? origen : Math.min(origen, destino);
            int b = dirigido ? destino : Math.max(origen, destino);

            comprobar(claves.add(a + ":" + b),
                    "Aristas duplicadas.");

            aristas.add(new Arista(origen, destino, peso));
        }

        return new Grafo(vertices, dirigido, List.copyOf(aristas));
    }

    //Lee las respuestas almacenadas en formato CLAVE=VALOR.
    private static Map<String, String> leerReferencia(Path archivo)
            throws IOException {

        Map<String, String> datos = new HashMap<>();

        for (String linea : Files.readAllLines(
                archivo, StandardCharsets.UTF_8)) {

            String[] partes = linea.split("=", 2);

            comprobar(partes.length == 2 && !partes[0].isBlank(),
                    "Linea de referencia invalida.");

            comprobar(datos.putIfAbsent(partes[0], partes[1]) == null,
                    "Clave duplicada: " + partes[0]);
        }

        return datos;
    }

    //Construye listas de adyacencia con vecinos ordenados.
    private static List<List<Integer>> adyacencia(Grafo grafo) {

        List<List<Integer>> vecinos = new ArrayList<>();

        for (int i = 0; i < grafo.vertices(); i++) {
            vecinos.add(new ArrayList<>());
        }

        for (Arista arista : grafo.aristas()) {

            vecinos.get(arista.origen()).add(arista.destino());

            if (!grafo.dirigido()) {
                vecinos.get(arista.destino()).add(arista.origen());
            }
        }

        vecinos.forEach(
                lista -> lista.sort(Comparator.naturalOrder())
        );

        return vecinos;
    }

    //Comprueba el recorrido BFS y las distancias.
    private static void comprobarBfs(
            List<List<Integer>> vecinos,
            int inicio,
            Map<String, String> referencia) {

        int[] distancia = new int[vecinos.size()];
        Arrays.fill(distancia, -1);

        Queue<Integer> cola = new ArrayDeque<>();
        List<Integer> orden = new ArrayList<>();

        cola.add(inicio);
        distancia[inicio] = 0;

        //Recorre los vertices utilizando una cola FIFO.
        while (!cola.isEmpty()) {

            int actual = cola.remove();
            orden.add(actual);

            for (int siguiente : vecinos.get(actual)) {

                if (distancia[siguiente] == -1) {

                    distancia[siguiente] = distancia[actual] + 1;
                    cola.add(siguiente);
                }
            }
        }

        comprobar(
                unir(orden).equals(valor(referencia, "BFS_ORDEN")),
                "Orden BFS incorrecto."
        );

        String distancias = Arrays.toString(distancia)
                .replace("[", "")
                .replace("]", "")
                .replace(" ", "");

        comprobar(
                distancias.equals(valor(referencia, "BFS_DISTANCIAS")),
                "Distancias BFS incorrectas."
        );
    }

    //Comprueba DFS mediante una pila en lugar de recursividad.
    private static void comprobarDfs(
            List<List<Integer>> vecinos,
            int inicio,
            Map<String, String> referencia) {

        boolean[] visto = new boolean[vecinos.size()];

        ArrayDeque<Integer> pila = new ArrayDeque<>();
        List<Integer> orden = new ArrayList<>();

        pila.push(inicio);

        //Explora primero los vertices mas profundos.
        while (!pila.isEmpty()) {

            int actual = pila.pop();

            if (visto[actual]) {
                continue;
            }

            visto[actual] = true;
            orden.add(actual);

            List<Integer> lista = vecinos.get(actual);

            //Apila en orden inverso para visitar el menor primero.
            for (int i = lista.size() - 1; i >= 0; i--) {

                if (!visto[lista.get(i)]) {
                    pila.push(lista.get(i));
                }
            }
        }

        comprobar(
                unir(orden).equals(valor(referencia, "DFS_ORDEN")),
                "Orden DFS incorrecto."
        );
    }

    //Valida ciclos y restricciones del orden topologico.
    private static void comprobarTopologico(
            Grafo grafo,
            List<List<Integer>> vecinos,
            Map<String, String> referencia) {

        String estado = valor(referencia, "TOPO_ESTADO");
        String orden = valor(referencia, "TOPO_ORDEN");

        //El orden topologico no aplica a grafos no dirigidos.
        if (!grafo.dirigido()) {

            comprobar(
                    estado.equals("NO_APLICA")
                    && orden.equals("NO_APLICA"),
                    "Topologico no aplica a grafos no dirigidos."
            );

            return;
        }

        //Detecta ciclos utilizando tres estados de visita.
        byte[] color = new byte[grafo.vertices()];
        boolean ciclo = false;

        for (int i = 0; i < color.length; i++) {

            if (color[i] == 0 && hayCiclo(i, vecinos, color)) {
                ciclo = true;
                break;
            }
        }

        if (ciclo) {

            comprobar(
                    estado.equals("CICLO")
                    && orden.equals("NO_APLICA"),
                    "No se reporto el ciclo dirigido."
            );

            return;
        }

        comprobar(estado.equals("ACICLICO"), "Se esperaba un DAG.");

        String[] partes = orden.split(",", -1);

        comprobar(partes.length == grafo.vertices(),
                "El orden topologico esta incompleto.");

        int[] posicion = new int[grafo.vertices()];
        Arrays.fill(posicion, -1);

        //Registra la posicion de cada vertice.
        for (int i = 0; i < partes.length; i++) {

            int vertice = Integer.parseInt(partes[i]);

            comprobar(
                    vertice >= 0 && vertice < posicion.length
                    && posicion[vertice] == -1,
                    "Vertice topologico repetido o invalido."
            );

            posicion[vertice] = i;
        }

        //Cada arista debe respetar el orden topologico.
        for (Arista arista : grafo.aristas()) {

            comprobar(
                    posicion[arista.origen()] < posicion[arista.destino()],
                    "Orden topologico viola una arista."
            );
        }
    }

    //Detecta ciclos dirigidos mediante DFS.
    private static boolean hayCiclo(
            int actual,
            List<List<Integer>> vecinos,
            byte[] color) {

        //Estado gris: vertice en exploracion.
        color[actual] = 1;

        for (int siguiente : vecinos.get(actual)) {

            if (color[siguiente] == 1) {
                return true;
            }

            if (color[siguiente] == 0
                    && hayCiclo(siguiente, vecinos, color)) {
                return true;
            }
        }

        //Estado negro: vertice completamente explorado.
        color[actual] = 2;
        return false;
    }

    //Comprueba el peso del MST mediante Prim-Jarnik.
    private static void comprobarMst(
            Grafo grafo,
            Map<String, String> referencia) {

        String pesoEsperado = valor(referencia, "MST_PESO");

        String componentesEsperados =
                valor(referencia, "MST_COMPONENTES");

        if (grafo.dirigido()) {

            comprobar(
                    pesoEsperado.equals("NO_APLICA")
                    && componentesEsperados.equals("NO_APLICA"),
                    "MST no aplica a grafos dirigidos."
            );

            return;
        }

        List<List<Paso>> vecinos = new ArrayList<>();

        for (int i = 0; i < grafo.vertices(); i++) {
            vecinos.add(new ArrayList<>());
        }

        //Construye conexiones ponderadas en ambos sentidos.
        for (Arista arista : grafo.aristas()) {

            vecinos.get(arista.origen()).add(
                    new Paso(arista.destino(), arista.peso())
            );

            vecinos.get(arista.destino()).add(
                    new Paso(arista.origen(), arista.peso())
            );
        }

        boolean[] visto = new boolean[grafo.vertices()];

        //Prioriza las conexiones de menor peso.
        PriorityQueue<Paso> pendientes = new PriorityQueue<>(
                Comparator.comparingInt(Paso::peso)
        );

        long total = 0;
        int componentes = 0;

        //Calcula un bosque minimo cuando el grafo es desconectado.
        for (int inicio = 0; inicio < grafo.vertices(); inicio++) {

            if (visto[inicio]) {
                continue;
            }

            componentes++;
            visto[inicio] = true;

            pendientes.addAll(vecinos.get(inicio));

            while (!pendientes.isEmpty()) {

                Paso paso = pendientes.remove();

                if (visto[paso.destino()]) {
                    continue;
                }

                visto[paso.destino()] = true;
                total += paso.peso();

                pendientes.addAll(vecinos.get(paso.destino()));
            }
        }

        //Contrasta el resultado de Prim contra la referencia.
        comprobar(
                Long.toString(total).equals(pesoEsperado),
                "Peso MST incorrecto. Prim obtuvo " + total
        );

        comprobar(
                Integer.toString(componentes)
                        .equals(componentesEsperados),
                "Numero de componentes MST incorrecto."
        );
    }

    //Obtiene archivos TXT ordenados por nombre.
    private static List<Path> listarTxt(Path carpeta)
            throws IOException {

        try (Stream<Path> archivos = Files.list(carpeta)) {

            return archivos
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".txt"))
                    .sorted()
                    .toList();
        }
    }

    //Obtiene un valor obligatorio de la referencia.
    private static String valor(
            Map<String, String> datos, String clave) {

        String valor = datos.get(clave);

        comprobar(valor != null, "Falta la clave: " + clave);

        return valor;
    }

    //Convierte el recorrido a una cadena separada por comas.
    private static String unir(List<Integer> vertices) {

        return String.join(
                ",",
                vertices.stream().map(String::valueOf).toList()
        );
    }

    //Lanza un error si una condicion no se cumple.
    private static void comprobar(
            boolean condicion, String mensaje) {

        if (!condicion) {
            throw new AssertionError(mensaje);
        }
    }

    //Ejecuta una prueba y registra su resultado.
    private static void probar(String nombre, Prueba prueba) {

        try {

            prueba.ejecutar();
            correctas++;

            System.out.println("[OK] " + nombre);

        } catch (Exception | AssertionError e) {

            fallidas++;

            System.out.println(
                    "[ERROR] " + nombre + " - " + e.getMessage()
            );
        }
    }

    //Permite ejecutar pruebas que pueden producir errores de lectura.
    @FunctionalInterface
    private interface Prueba {
        void ejecutar() throws IOException;
    }

    //Representa una arista leida del archivo TXT.
    private record Arista(int origen, int destino, int peso) {
    }

    //Representa un grafo para las pruebas independientes.
    private record Grafo(
            int vertices, boolean dirigido, List<Arista> aristas) {
    }

    //Representa una conexion candidata para Prim.
    private record Paso(int destino, int peso) {
    }
}