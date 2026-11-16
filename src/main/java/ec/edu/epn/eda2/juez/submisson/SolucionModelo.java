package ec.edu.epn.eda2.juez.submission;

import ec.edu.epn.eda2.juez.core.modelo.Arista;
import ec.edu.epn.eda2.juez.core.modelo.CasoGrafo;
import ec.edu.epn.eda2.juez.core.modelo.TipoGrafo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class SolucionModelo implements SolucionEstudiante {

    private List<List<Arista>> construirAdyacencia(CasoGrafo g) {
        List<List<Arista>> adyacencia = new ArrayList<>();
        for (int i = 0; i < g.vertices(); i++) {
            adyacencia.add(new ArrayList<>());
        }

        for (Arista a : g.aristas()) {
            adyacencia.get(a.origen()).add(new Arista(a.origen(), a.destino(), a.peso()));
            if (g.tipo() == TipoGrafo.NO_DIRIGIDO) {
                adyacencia.get(a.destino()).add(new Arista(a.destino(), a.origen(), a.peso()));
            }
        }

        for (List<Arista> vecinos : adyacencia) {
            Collections.sort(vecinos, Comparator.comparingInt(Arista::destino));
        }

        return adyacencia;
    }

    @Override
    public List<Integer> dfs(CasoGrafo g, int inicio) {
        List<List<Arista>> adyacencia = construirAdyacencia(g);
        List<Integer> recorrido = new ArrayList<>();
        boolean[] visitados = new boolean[g.vertices()];
        dfsRecursivo(adyacencia, inicio, visitados, recorrido);
        return recorrido;
    }

    private void dfsRecursivo(List<List<Arista>> adyacencia, int actual, boolean[] visitados, List<Integer> recorrido) {
        visitados[actual] = true;
        recorrido.add(actual);

        for (Arista vecino : adyacencia.get(actual)) {
            if (!visitados[vecino.destino()]) {
                dfsRecursivo(adyacencia, vecino.destino(), visitados, recorrido);
            }
        }
    }

    @Override
    public List<Integer> bfs(CasoGrafo g, int inicio) {
        List<List<Arista>> adyacencia = construirAdyacencia(g);
        List<Integer> recorrido = new ArrayList<>();
        boolean[] visitados = new boolean[g.vertices()];
        Queue<Integer> cola = new LinkedList<>();

        cola.add(inicio);
        visitados[inicio] = true;

        while (!cola.isEmpty()) {
            int actual = cola.poll();
            recorrido.add(actual);

            for (Arista vecino : adyacencia.get(actual)) {
                if (!visitados[vecino.destino()]) {
                    visitados[vecino.destino()] = true;
                    cola.add(vecino.destino());
                }
            }
        }

        return recorrido;
    }

    @Override
    public List<Integer> ordenTopologico(CasoGrafo g) {
        List<List<Arista>> adyacencia = construirAdyacencia(g);
        int[] gradoEntrada = new int[g.vertices()];
        List<Integer> orden = new ArrayList<>();

        for (List<Arista> vertice : adyacencia) {
            for (Arista vecino : vertice) {
                gradoEntrada[vecino.destino()]++;
            }
        }

        Queue<Integer> cola = new LinkedList<>();

        for (int i = 0; i < g.vertices(); i++) {
            if (gradoEntrada[i] == 0) {
                cola.add(i);
            }
        }

        while (!cola.isEmpty()) {
            int actual = cola.poll();
            orden.add(actual);

            for (Arista vecino : adyacencia.get(actual)) {
                gradoEntrada[vecino.destino()]--;
                if (gradoEntrada[vecino.destino()] == 0) {
                    cola.add(vecino.destino());
                }
            }
        }

        return (orden.size() != g.vertices()) ? new ArrayList<>() : orden;
    }

    @Override
    public int mst(CasoGrafo g) {
        List<List<Arista>> adyacencia = construirAdyacencia(g);
        List<int[]> aristas = new ArrayList<>();

        for (int i = 0; i < adyacencia.size(); i++) {
            for (Arista vecinos : adyacencia.get(i)) {
                if (i < vecinos.destino()) {
                    int[] temp = {i, vecinos.destino(), vecinos.peso()};
                    aristas.add(temp);
                }
            }
        }

        aristas.sort((a, b) -> Integer.compare(a[2], b[2]));

        int[] padre = new int[g.vertices()];

        for (int i = 0; i < padre.length; i++) {
            padre[i] = i;
        }

        int pesoTotal = 0;
        for (int[] arista : aristas) {
            int raizU = find(padre, arista[0]);
            int raizV = find(padre, arista[1]);

            if (raizU != raizV) {
                padre[raizU] = raizV;
                pesoTotal += arista[2];
            }
        }

        return pesoTotal;
    }

    private int find(int[] padre, int x) {
        while (padre[x] != x) {
            padre[x] = padre[padre[x]];
            x = padre[x];
        }
        return x;
    }
}