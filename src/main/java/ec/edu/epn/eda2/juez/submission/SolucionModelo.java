package ec.edu.epn.eda2.juez.submission;

import ec.edu.epn.eda2.juez.core.modelo.Arista;
import ec.edu.epn.eda2.juez.core.modelo.GrafoCargado;
import ec.edu.epn.eda2.juez.core.modelo.Vecino;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

//SUBMISSION DE EJEMPLO CORRECTA.
//Implementa DFS (iterativo, mismo orden que el recursivo), BFS con
//distancias, orden topologico (Kahn) y MST (Kruskal, bosque minimo).
//Sin estado entre llamadas: el juez repite cada metodo varias veces.
public class SolucionModelo implements SolucionEstudiante {

    @Override
    public int[] dfs(GrafoCargado grafo, int inicio) {

        int n = grafo.numVertices();
        boolean[] visitado = new boolean[n];
        int[] siguiente = new int[n];   //proximo vecino a explorar
        int[] orden = new int[n];
        int cantidad = 0;

        int[] pila = new int[n];
        int tope = 0;

        pila[tope++] = inicio;
        visitado[inicio] = true;
        orden[cantidad++] = inicio;

        while (tope > 0) {
            int u = pila[tope - 1];
            List<Vecino> vecinos = grafo.vecinos(u);

            if (siguiente[u] < vecinos.size()) {
                int v = vecinos.get(siguiente[u]++).destino();

                if (!visitado[v]) {
                    visitado[v] = true;
                    orden[cantidad++] = v;
                    pila[tope++] = v;
                }
            } else {
                tope--;
            }
        }

        return Arrays.copyOf(orden, cantidad);
    }

    @Override
    public ResultadoBFS bfs(GrafoCargado grafo, int inicio) {

        int n = grafo.numVertices();
        int[] distancias = new int[n];
        Arrays.fill(distancias, -1);

        int[] orden = new int[n];
        int cantidad = 0;

        ArrayDeque<Integer> cola = new ArrayDeque<>();
        cola.add(inicio);
        distancias[inicio] = 0;

        while (!cola.isEmpty()) {
            int u = cola.poll();
            orden[cantidad++] = u;

            for (Vecino vecino : grafo.vecinos(u)) {
                int v = vecino.destino();

                if (distancias[v] == -1) {
                    distancias[v] = distancias[u] + 1;
                    cola.add(v);
                }
            }
        }

        return new ResultadoBFS(Arrays.copyOf(orden, cantidad), distancias);
    }

    @Override
    public ResultadoTopologico ordenTopologico(GrafoCargado grafo) {

        int n = grafo.numVertices();
        int[] gradoEntrada = new int[n];

        for (int u = 0; u < n; u++) {
            for (Vecino v : grafo.vecinos(u)) {
                gradoEntrada[v.destino()]++;
            }
        }

        ArrayDeque<Integer> cola = new ArrayDeque<>();

        for (int v = 0; v < n; v++) {
            if (gradoEntrada[v] == 0) {
                cola.add(v);
            }
        }

        int[] orden = new int[n];
        int cantidad = 0;

        while (!cola.isEmpty()) {
            int u = cola.poll();
            orden[cantidad++] = u;

            for (Vecino v : grafo.vecinos(u)) {
                if (--gradoEntrada[v.destino()] == 0) {
                    cola.add(v.destino());
                }
            }
        }

        return (cantidad == n)
            ? ResultadoTopologico.aciclico(orden)
            : ResultadoTopologico.conCiclo();
    }

    @Override
    public ResultadoMST mst(GrafoCargado grafo) {

        List<Arista> ordenadas = new ArrayList<>(grafo.aristas());
        ordenadas.sort(Comparator.comparingInt(Arista::peso));

        int n = grafo.numVertices();
        int[] padre = new int[n];

        for (int i = 0; i < n; i++) {
            padre[i] = i;
        }

        List<Arista> elegidas = new ArrayList<>();

        for (Arista a : ordenadas) {
            int ra = raiz(padre, a.origen());
            int rb = raiz(padre, a.destino());

            if (ra != rb) {
                padre[ra] = rb;
                elegidas.add(a);
            }
        }

        return new ResultadoMST(elegidas);
    }

    private static int raiz(int[] padre, int x) {
        while (padre[x] != x) {
            padre[x] = padre[padre[x]];
            x = padre[x];
        }
        return x;
    }
}
