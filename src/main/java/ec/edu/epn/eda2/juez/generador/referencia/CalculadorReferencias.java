package ec.edu.epn.eda2.juez.generador.referencia;

import ec.edu.epn.eda2.juez.core.modelo.Arista;
import ec.edu.epn.eda2.juez.core.modelo.CasoGrafo;
import ec.edu.epn.eda2.juez.core.modelo.TipoGrafo;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.stream.Collectors;

//Calcula los resultados de referencia de un grafo.
public final class CalculadorReferencias {

    //Impide instanciar esta clase utilitaria.
    private CalculadorReferencias() {
    }

    //Calcula las respuestas para los algoritmos evaluados.
    public static Map<String, String> calcular(CasoGrafo grafo) {

        Map<String, String> resultado = new LinkedHashMap<>();

        int inicio = 0;

        resultado.put("FORMATO", "REFERENCIA_V1");
        resultado.put("CASO", grafo.nombre());
        resultado.put("INICIO", String.valueOf(inicio));

        //Construye las listas de adyacencia.
        List<List<Integer>> vecinos = crearAdyacencia(grafo);

        //Calcula el recorrido BFS.
        calcularBFS(vecinos, inicio, resultado);

        //Calcula el recorrido DFS.
        calcularDFS(vecinos, inicio, resultado);

        //Calcula el resultado topologico cuando corresponde.
        if (grafo.tipo() == TipoGrafo.DIRIGIDO) {
            calcularTopologico(grafo, resultado);
        } else {
            resultado.put("TOPO_ESTADO", "NO_APLICA");
            resultado.put("TOPO_ORDEN", "NO_APLICA");
        }

        //Calcula el MST solamente en grafos no dirigidos.
        if (grafo.tipo() == TipoGrafo.NO_DIRIGIDO) {
            calcularMST(grafo, resultado);
        } else {
            resultado.put("MST_PESO", "NO_APLICA");
            resultado.put("MST_COMPONENTES", "NO_APLICA");
        }

        return resultado;
    }

    //Construye listas de adyacencia ordenadas ascendentemente.
    private static List<List<Integer>> crearAdyacencia(
            CasoGrafo grafo) {

        List<List<Integer>> vecinos = new ArrayList<>();

        for (int i = 0; i < grafo.vertices(); i++) {
            vecinos.add(new ArrayList<>());
        }

        for (Arista arista : grafo.aristas()) {

            vecinos.get(arista.origen()).add(arista.destino());

            //En grafos no dirigidos se recorre en ambos sentidos.
            if (grafo.tipo() == TipoGrafo.NO_DIRIGIDO) {
                vecinos.get(arista.destino()).add(arista.origen());
            }
        }

        //Garantiza recorridos reproducibles.
        for (List<Integer> lista : vecinos) {
            lista.sort(Comparator.naturalOrder());
        }

        return vecinos;
    }

    //Calcula BFS utilizando una cola FIFO.
    private static void calcularBFS(
            List<List<Integer>> vecinos,
            int inicio,
            Map<String, String> resultado) {

        int n = vecinos.size();

        int[] distancias = new int[n];
        Arrays.fill(distancias, -1);

        List<Integer> orden = new ArrayList<>();
        Queue<Integer> cola = new ArrayDeque<>();

        distancias[inicio] = 0;
        cola.add(inicio);

        //Visita primero los vertices mas cercanos.
        while (!cola.isEmpty()) {

            int actual = cola.remove();
            orden.add(actual);

            for (int siguiente : vecinos.get(actual)) {

                if (distancias[siguiente] == -1) {

                    distancias[siguiente] =
                            distancias[actual] + 1;

                    cola.add(siguiente);
                }
            }
        }

        resultado.put("BFS_ORDEN", unir(orden));

        resultado.put(
            "BFS_DISTANCIAS",
            Arrays.stream(distancias)
                  .mapToObj(String::valueOf)
                  .collect(Collectors.joining(","))
        );
    }

    //Calcula DFS mediante exploracion recursiva.
    private static void calcularDFS(
            List<List<Integer>> vecinos,
            int inicio,
            Map<String, String> resultado) {

        boolean[] visitados = new boolean[vecinos.size()];
        List<Integer> orden = new ArrayList<>();

        visitarDFS(inicio, vecinos, visitados, orden);

        resultado.put("DFS_ORDEN", unir(orden));
    }

    //Visita recursivamente los vecinos no explorados.
    private static void visitarDFS(
            int actual,
            List<List<Integer>> vecinos,
            boolean[] visitados,
            List<Integer> orden) {

        visitados[actual] = true;
        orden.add(actual);

        for (int siguiente : vecinos.get(actual)) {

            if (!visitados[siguiente]) {
                visitarDFS(siguiente, vecinos, visitados, orden);
            }
        }
    }

    //Calcula un orden topologico usando el algoritmo de Kahn.
    private static void calcularTopologico(
            CasoGrafo grafo,
            Map<String, String> resultado) {

        int n = grafo.vertices();

        int[] gradoEntrada = new int[n];
        List<List<Integer>> vecinos = crearAdyacencia(grafo);

        for (Arista arista : grafo.aristas()) {
            gradoEntrada[arista.destino()]++;
        }

        //Prioriza vertices menores para generar un orden estable.
        PriorityQueue<Integer> cola = new PriorityQueue<>();

        for (int i = 0; i < n; i++) {

            if (gradoEntrada[i] == 0) {
                cola.add(i);
            }
        }

        List<Integer> orden = new ArrayList<>();

        while (!cola.isEmpty()) {

            int actual = cola.remove();
            orden.add(actual);

            for (int siguiente : vecinos.get(actual)) {

                gradoEntrada[siguiente]--;

                if (gradoEntrada[siguiente] == 0) {
                    cola.add(siguiente);
                }
            }
        }

        //Un ciclo impide ordenar todos los vertices.
        if (orden.size() != n) {

            resultado.put("TOPO_ESTADO", "CICLO");
            resultado.put("TOPO_ORDEN", "NO_APLICA");

        } else {

            resultado.put("TOPO_ESTADO", "ACICLICO");
            resultado.put("TOPO_ORDEN", unir(orden));
        }
    }

    //Calcula el peso minimo mediante Kruskal.
    private static void calcularMST(
            CasoGrafo grafo,
            Map<String, String> resultado) {

        List<Arista> aristas =
                new ArrayList<>(grafo.aristas());

        //Ordena las aristas desde el menor peso.
        aristas.sort(
            Comparator.comparingInt(Arista::peso)
                      .thenComparingInt(Arista::origen)
                      .thenComparingInt(Arista::destino)
        );

        UnionFind conjuntos = new UnionFind(grafo.vertices());

        long pesoTotal = 0;

        //Selecciona aristas que no formen ciclos.
        for (Arista arista : aristas) {

            if (conjuntos.unir(
                    arista.origen(),
                    arista.destino())) {

                pesoTotal += arista.peso();
            }
        }

        resultado.put("MST_PESO", String.valueOf(pesoTotal));

        //En grafos desconectados representa un bosque minimo.
        resultado.put(
            "MST_COMPONENTES",
            String.valueOf(conjuntos.componentes())
        );
    }

    //Convierte una lista de vertices a texto separado por comas.
    private static String unir(List<Integer> vertices) {

        return vertices.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    //Estructura Union-Find para el algoritmo de Kruskal.
    private static final class UnionFind {

        private final int[] padre;
        private final int[] rango;
        private int componentes;

        //Inicializa cada vertice como un conjunto independiente.
        private UnionFind(int vertices) {

            padre = new int[vertices];
            rango = new int[vertices];
            componentes = vertices;

            for (int i = 0; i < vertices; i++) {
                padre[i] = i;
            }
        }

        //Busca el representante con compresion de caminos.
        private int buscar(int vertice) {

            if (padre[vertice] != vertice) {
                padre[vertice] = buscar(padre[vertice]);
            }

            return padre[vertice];
        }

        //Une dos conjuntos utilizando union por rango.
        private boolean unir(int a, int b) {

            int raizA = buscar(a);
            int raizB = buscar(b);

            //Si tienen la misma raiz, la arista forma un ciclo.
            if (raizA == raizB) {
                return false;
            }

            if (rango[raizA] < rango[raizB]) {

                padre[raizA] = raizB;

            } else if (rango[raizA] > rango[raizB]) {

                padre[raizB] = raizA;

            } else {

                padre[raizB] = raizA;
                rango[raizA]++;
            }

            componentes--;
            return true;
        }

        //Devuelve el numero de componentes restantes.
        private int componentes() {
            return componentes;
        }
    }
}