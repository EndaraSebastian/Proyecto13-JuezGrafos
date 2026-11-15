package ec.edu.epn.eda2.juez.generador;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

//Comprueba las referencias mediante algoritmos independientes.
public final class PruebaReferenciasRendimiento {

    private static final Path ENTRADAS =
            Path.of("casos", "rendimiento");

    private static final Path REFERENCIAS =
            Path.of("casos", "referencias-rendimiento");

    private static final List<String> CASOS = List.of(
            "14_rendimiento_100_disperso",
            "15_rendimiento_100_denso",
            "16_rendimiento_500_disperso",
            "17_rendimiento_500_denso",
            "18_rendimiento_1000_disperso"
    );

    //Representa una conexion local para las pruebas.
    private record Conexion(int destino, int peso) {
    }

    //Representa el grafo leido desde un archivo TXT.
    private record Grafo(
            int vertices,
            List<List<Conexion>> adyacencia) {
    }

    //Evita crear instancias de esta clase.
    private PruebaReferenciasRendimiento() {
    }

    //Ejecuta las cinco verificaciones independientes.
    public static void main(String[] args) {

        int correctas = 0;
        int fallidas = 0;

        System.out.println("PRUEBAS INDEPENDIENTES DE REFERENCIAS");
        System.out.println("--------------------------------------");

        for (String nombre : CASOS) {

            try {
                verificarCaso(nombre);
                correctas++;

            } catch (Exception | AssertionError e) {

                fallidas++;

                System.out.println(
                        "[ERROR] " + nombre + ": " + e.getMessage()
                );
            }
        }

        System.out.println("--------------------------------------");
        System.out.println("Correctas: " + correctas);
        System.out.println("Fallidas: " + fallidas);

        if (fallidas > 0) {
            System.exit(1);
        }
    }

    //Compara los resultados calculados con los archivos.
    private static void verificarCaso(String nombre)
            throws IOException {

        Grafo grafo = leerGrafo(
                ENTRADAS.resolve(nombre + ".txt")
        );

        Map<String, String> referencia = leerReferencia(
                REFERENCIAS.resolve(nombre + ".txt")
        );

        comprobar(
                nombre.equals(referencia.get("CASO")),
                "Nombre de referencia incorrecto."
        );

        comprobar(
                "REFERENCIA_V1".equals(referencia.get("FORMATO")),
                "Version de referencia incorrecta."
        );

        comprobar(
                "0".equals(referencia.get("INICIO")),
                "El origen BFS debe ser cero."
        );

        //Calcula el arbol minimo usando Prim.
        long pesoPrim = calcularPrim(grafo);

        long pesoReferencia = Long.parseLong(
                referencia.get("MST_PESO")
        );

        comprobar(
                pesoPrim == pesoReferencia,
                "Prim y Kruskal tienen pesos distintos."
        );

        comprobar(
                "1".equals(referencia.get("MST_COMPONENTES")),
                "El grafo debe tener una componente."
        );

        //Calcula nuevamente BFS desde el vertice cero.
        int[] distancias = new int[grafo.vertices()];
        Arrays.fill(distancias, -1);

        List<Integer> ordenBfs =
                calcularBfs(grafo, distancias);

        //Compara las distancias obtenidas.
        List<Integer> distanciasEsperadas =
                leerListaNumerica(
                        referencia.get("BFS_DISTANCIAS")
                );

        comprobar(
                distanciasEsperadas.size() == grafo.vertices(),
                "Cantidad de distancias incorrecta."
        );

        for (int i = 0; i < grafo.vertices(); i++) {

            comprobar(
                    distancias[i] == distanciasEsperadas.get(i),
                    "Distancia BFS incorrecta en vertice " + i
            );
        }

        //Compara el orden BFS con vecinos en orden ascendente.
        List<Integer> ordenEsperado =
                leerListaNumerica(
                        referencia.get("BFS_ORDEN")
                );

        comprobar(
                ordenBfs.equals(ordenEsperado),
                "Orden BFS diferente."
        );

        System.out.println(
                "[OK] " + nombre
                + " | Prim=" + pesoPrim
                + " | BFS=" + ordenBfs.size() + " vertices"
        );
    }

    //Lee un grafo no dirigido desde el TXT.
    private static Grafo leerGrafo(Path archivo)
            throws IOException {

        try (BufferedReader lector = Files.newBufferedReader(
                archivo, StandardCharsets.UTF_8)) {

            String encabezado = lector.readLine();

            comprobar(
                    encabezado != null,
                    "El archivo del grafo esta vacio."
            );

            String[] datos = encabezado.trim().split("\\s+");

            comprobar(
                    datos.length == 3 && "U".equals(datos[0]),
                    "Encabezado de grafo incorrecto."
            );

            int vertices = Integer.parseInt(datos[1]);
            int aristas = Integer.parseInt(datos[2]);

            List<List<Conexion>> adyacencia =
                    new ArrayList<>();

            for (int i = 0; i < vertices; i++) {
                adyacencia.add(new ArrayList<>());
            }

            //Carga las conexiones en ambos sentidos.
            for (int i = 0; i < aristas; i++) {

                String linea = lector.readLine();

                comprobar(
                        linea != null,
                        "Faltan aristas en el archivo."
                );

                String[] partes = linea.trim().split("\\s+");

                comprobar(
                        partes.length == 3,
                        "Formato de arista incorrecto."
                );

                int origen = Integer.parseInt(partes[0]);
                int destino = Integer.parseInt(partes[1]);
                int peso = Integer.parseInt(partes[2]);

                comprobar(
                        origen >= 0 && origen < vertices
                        && destino >= 0 && destino < vertices,
                        "Vertice fuera de rango."
                );

                comprobar(
                        origen != destino && peso > 0,
                        "Arista o peso incorrecto."
                );

                adyacencia.get(origen).add(
                        new Conexion(destino, peso)
                );

                adyacencia.get(destino).add(
                        new Conexion(origen, peso)
                );
            }

            comprobar(
                    lector.readLine() == null,
                    "Existen lineas adicionales."
            );

            //Ordena vecinos para reproducir el criterio BFS.
            for (List<Conexion> vecinos : adyacencia) {

                vecinos.sort(
                        Comparator.comparingInt(Conexion::destino)
                );
            }

            return new Grafo(vertices, adyacencia);
        }
    }

    //Carga las respuestas del archivo de referencia.
    private static Map<String, String> leerReferencia(Path archivo)
            throws IOException {

        Map<String, String> campos = new HashMap<>();

        try (BufferedReader lector = Files.newBufferedReader(
                archivo, StandardCharsets.UTF_8)) {

            String linea;

            while ((linea = lector.readLine()) != null) {

                int posicion = linea.indexOf('=');

                comprobar(
                        posicion > 0,
                        "Campo de referencia incorrecto."
                );

                String clave =
                        linea.substring(0, posicion).trim();

                String valor =
                        linea.substring(posicion + 1).trim();

                comprobar(
                        !campos.containsKey(clave),
                        "Clave duplicada: " + clave
                );

                campos.put(clave, valor);
            }
        }

        return campos;
    }

    //Calcula el MST mediante el algoritmo de Prim.
    private static long calcularPrim(Grafo grafo) {

        boolean[] visitados =
                new boolean[grafo.vertices()];

        PriorityQueue<Conexion> cola =
                new PriorityQueue<>(
                        Comparator.comparingInt(Conexion::peso)
                );

        //Inicia en el vertice cero.
        cola.add(new Conexion(0, 0));

        long pesoTotal = 0;
        int cantidadVisitados = 0;

        while (!cola.isEmpty()) {

            Conexion actual = cola.poll();

            if (visitados[actual.destino()]) {
                continue;
            }

            visitados[actual.destino()] = true;
            cantidadVisitados++;

            pesoTotal += actual.peso();

            //Agrega las conexiones hacia vertices pendientes.
            for (Conexion vecino :
                    grafo.adyacencia().get(actual.destino())) {

                if (!visitados[vecino.destino()]) {
                    cola.add(vecino);
                }
            }
        }

        comprobar(
                cantidadVisitados == grafo.vertices(),
                "El grafo no es conexo."
        );

        return pesoTotal;
    }

    //Calcula BFS sin utilizar la clase de referencias.
    private static List<Integer> calcularBfs(
            Grafo grafo, int[] distancias) {

        ArrayDeque<Integer> cola = new ArrayDeque<>();
        List<Integer> orden = new ArrayList<>();

        //El recorrido comienza desde el vertice cero.
        distancias[0] = 0;
        cola.add(0);

        while (!cola.isEmpty()) {

            int actual = cola.remove();
            orden.add(actual);

            for (Conexion vecino :
                    grafo.adyacencia().get(actual)) {

                int destino = vecino.destino();

                if (distancias[destino] == -1) {

                    distancias[destino] =
                            distancias[actual] + 1;

                    cola.add(destino);
                }
            }
        }

        return orden;
    }

    //Convierte una secuencia de numeros a una lista.
    private static List<Integer> leerListaNumerica(
            String contenido) {

        comprobar(
                contenido != null,
                "Falta una lista numerica."
        );

        String limpio = contenido.trim();

        //Acepta listas con o sin corchetes.
        if (limpio.startsWith("[") &&
                limpio.endsWith("]")) {

            limpio = limpio.substring(
                    1, limpio.length() - 1
            ).trim();
        }

        List<Integer> numeros = new ArrayList<>();

        if (limpio.isEmpty()) {
            return numeros;
        }

        for (String parte : limpio.split("[,\\s]+")) {
            numeros.add(Integer.parseInt(parte));
        }

        return numeros;
    }

    //Comprueba una condicion de la prueba.
    private static void comprobar(
            boolean condicion, String mensaje) {

        if (!condicion) {
            throw new AssertionError(mensaje);
        }
    }
}