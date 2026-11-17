package ec.edu.epn.eda2.juez.validador;

import ec.edu.epn.eda2.juez.core.modelo.Arista;
import ec.edu.epn.eda2.juez.core.modelo.GrafoCargado;
import ec.edu.epn.eda2.juez.core.parser.ReferenciaCaso;
import ec.edu.epn.eda2.juez.core.parser.ReferenciaCaso.EstadoTopologico;
import ec.edu.epn.eda2.juez.submission.ResultadoBFS;
import ec.edu.epn.eda2.juez.submission.ResultadoMST;
import ec.edu.epn.eda2.juez.submission.ResultadoTopologico;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

//Validador real del juez (Estudiante 2).
//
//- DFS y BFS son deterministas (vecinos ascendentes): se comparan
//  contra la referencia indicando la primera diferencia.
//- Orden topologico y MST admiten varias respuestas validas: se
//  comprueban sus PROPIEDADES, no la igualdad con la referencia.
//- Nunca lanza excepciones: cualquier contenido absurdo produce
//  un veredicto INCORRECTO con el motivo.
public final class ValidadorGrafos implements ValidadorResultado {

    //Constructor publico sin argumentos (exigido para la carga).
    public ValidadorGrafos() {
    }

    @Override
    public Veredicto validarDfs(
            GrafoCargado grafo, ReferenciaCaso referencia, int[] orden) {

        try {
            String error = revisarOrdenVisita(
                "DFS", orden, referencia.dfsOrden(), grafo.numVertices());

            return (error == null)
                ? Veredicto.correcto() : Veredicto.incorrecto(error);

        } catch (RuntimeException e) {
            return Veredicto.incorrecto("DFS no validable: " + e);
        }
    }

    @Override
    public Veredicto validarBfs(
            GrafoCargado grafo,
            ReferenciaCaso referencia,
            ResultadoBFS resultado) {

        try {
            int n = grafo.numVertices();

            String error = revisarOrdenVisita(
                "BFS", resultado.orden(), referencia.bfsOrden(), n);

            if (error != null) {
                return Veredicto.incorrecto(error);
            }

            int[] dist = resultado.distancias();
            int[] esperada = referencia.bfsDistancias();

            if (dist.length != n) {
                return Veredicto.incorrecto(
                    "BFS: distancias tiene " + dist.length
                    + " valores y el grafo " + n + " vertices");
            }

            for (int v = 0; v < n; v++) {
                if (dist[v] != esperada[v]) {
                    return Veredicto.incorrecto(
                        "BFS: distancia del vertice " + v + " es "
                        + dist[v] + " y se esperaba " + esperada[v]);
                }
            }

            return Veredicto.correcto();

        } catch (RuntimeException e) {
            return Veredicto.incorrecto("BFS no validable: " + e);
        }
    }

    @Override
    public Veredicto validarTopologico(
            GrafoCargado grafo,
            ReferenciaCaso referencia,
            ResultadoTopologico resultado) {

        try {
            int n = grafo.numVertices();

            if (referencia.topoEstado() == EstadoTopologico.CICLO) {
                return resultado.hayCiclo()
                    ? Veredicto.correcto()
                    : Veredicto.incorrecto(
                        "Topologico: el grafo tiene un ciclo y la "
                        + "solucion devolvio un orden");
            }

            if (resultado.hayCiclo()) {
                return Veredicto.incorrecto(
                    "Topologico: el grafo es aciclico pero la solucion "
                    + "reporto un ciclo");
            }

            int[] orden = resultado.orden();

            if (orden.length != n) {
                return Veredicto.incorrecto(
                    "Topologico: el orden tiene " + orden.length
                    + " vertices y el grafo " + n);
            }

            int[] posicion = new int[n];
            Arrays.fill(posicion, -1);

            for (int i = 0; i < n; i++) {
                int v = orden[i];

                if (v < 0 || v >= n) {
                    return Veredicto.incorrecto(
                        "Topologico: vertice fuera de rango (" + v + ")");
                }

                if (posicion[v] != -1) {
                    return Veredicto.incorrecto(
                        "Topologico: vertice repetido (" + v + ")");
                }

                posicion[v] = i;
            }

            for (Arista a : grafo.aristas()) {
                if (posicion[a.origen()] > posicion[a.destino()]) {
                    return Veredicto.incorrecto(
                        "Topologico: la arista " + a.origen() + "->"
                        + a.destino() + " no se respeta");
                }
            }

            return Veredicto.correcto();

        } catch (RuntimeException e) {
            return Veredicto.incorrecto("Topologico no validable: " + e);
        }
    }

    @Override
    public Veredicto validarMst(
            GrafoCargado grafo,
            ReferenciaCaso referencia,
            ResultadoMST resultado) {

        try {
            int n = grafo.numVertices();

            //Pesos reales de cada arista no dirigida (clave unica).
            Map<Long, Integer> pesos = new HashMap<>();

            for (Arista a : grafo.aristas()) {
                pesos.put(clave(a.origen(), a.destino(), n), a.peso());
            }

            int[] padre = new int[n];

            for (int i = 0; i < n; i++) {
                padre[i] = i;
            }

            int componentes = n;
            long total = 0;

            for (Arista a : resultado.aristas()) {

                if (a.origen() >= n || a.destino() >= n) {
                    return Veredicto.incorrecto(
                        "MST: vertice fuera de rango en la arista "
                        + a.origen() + "-" + a.destino());
                }

                Integer real = pesos.get(clave(a.origen(), a.destino(), n));

                if (real == null) {
                    return Veredicto.incorrecto(
                        "MST: la arista " + a.origen() + "-"
                        + a.destino() + " no existe en el grafo");
                }

                if (real != a.peso()) {
                    return Veredicto.incorrecto(
                        "MST: la arista " + a.origen() + "-"
                        + a.destino() + " tiene peso " + a.peso()
                        + " y en el grafo es " + real);
                }

                int ra = raiz(padre, a.origen());
                int rb = raiz(padre, a.destino());

                if (ra == rb) {
                    return Veredicto.incorrecto(
                        "MST: la arista " + a.origen() + "-"
                        + a.destino() + " forma un ciclo o esta repetida");
                }

                padre[ra] = rb;
                componentes--;
                total += a.peso();
            }

            if (componentes != referencia.mstComponentes()) {
                return Veredicto.incorrecto(
                    "MST: no cubre cada componente (quedan "
                    + componentes + " componentes y se esperaban "
                    + referencia.mstComponentes() + ")");
            }

            if (total != referencia.mstPeso()) {
                return Veredicto.incorrecto(
                    "MST: peso total " + total + " y el minimo es "
                    + referencia.mstPeso());
            }

            return Veredicto.correcto();

        } catch (RuntimeException e) {
            return Veredicto.incorrecto("MST no validable: " + e);
        }
    }

    @Override
    public String nombre() {
        return "ValidadorGrafos";
    }

    //Compara un orden de visita contra el esperado; null si coincide.
    private static String revisarOrdenVisita(
            String algoritmo, int[] orden, int[] esperado, int n) {

        boolean[] visto = new boolean[n];

        for (int v : orden) {
            if (v < 0 || v >= n) {
                return algoritmo + ": vertice fuera de rango (" + v + ")";
            }
            if (visto[v]) {
                return algoritmo + ": vertice repetido (" + v + ")";
            }
            visto[v] = true;
        }

        int comunes = Math.min(orden.length, esperado.length);

        for (int i = 0; i < comunes; i++) {
            if (orden[i] != esperado[i]) {
                return algoritmo + ": en la posicion " + i
                    + " se devolvio " + orden[i] + " y se esperaba "
                    + esperado[i];
            }
        }

        if (orden.length != esperado.length) {
            return algoritmo + ": se devolvieron " + orden.length
                + " vertices y se esperaban " + esperado.length;
        }

        return null;
    }

    //Clave unica de una arista no dirigida.
    private static long clave(int a, int b, int n) {
        return (long) Math.min(a, b) * n + Math.max(a, b);
    }

    //Union-Find con compresion de caminos (iterativo).
    private static int raiz(int[] padre, int x) {
        while (padre[x] != x) {
            padre[x] = padre[padre[x]];
            x = padre[x];
        }
        return x;
    }
}
