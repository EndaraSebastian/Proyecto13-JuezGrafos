package ec.edu.epn.eda2.juez.core.parser;

import ec.edu.epn.eda2.juez.core.modelo.GrafoCargado;

//Respuestas de referencia de un caso (formato REFERENCIA_V1) ya
//cargadas en memoria. Es un contenedor de datos: la logica para
//comparar contra la respuesta del estudiante vive en el validador.
public final class ReferenciaCaso {

    //Resultado del orden topologico segun la referencia.
    public enum EstadoTopologico {
        ACICLICO, CICLO, NO_APLICA
    }

    private final String caso;
    private final int inicio;
    private final int[] bfsOrden;
    private final int[] bfsDistancias;
    private final int[] dfsOrden;
    private final EstadoTopologico topoEstado;
    private final int[] topoOrden;
    private final boolean mstAplica;
    private final long mstPeso;
    private final int mstComponentes;

    //Crea la referencia. Los arreglos se copian para que sea inmutable.
    public ReferenciaCaso(
            String caso,
            int inicio,
            int[] bfsOrden,
            int[] bfsDistancias,
            int[] dfsOrden,
            EstadoTopologico topoEstado,
            int[] topoOrden,
            boolean mstAplica,
            long mstPeso,
            int mstComponentes) {

        this.caso = caso;
        this.inicio = inicio;
        this.bfsOrden = bfsOrden.clone();
        this.bfsDistancias = bfsDistancias.clone();
        this.dfsOrden = dfsOrden.clone();
        this.topoEstado = topoEstado;
        this.topoOrden = topoOrden.clone();
        this.mstAplica = mstAplica;
        this.mstPeso = mstPeso;
        this.mstComponentes = mstComponentes;
    }

    //Nombre del caso (igual al nombre de los archivos).
    public String caso() {
        return caso;
    }

    //Vertice desde el que se ejecutan DFS y BFS.
    public int inicio() {
        return inicio;
    }

    //Orden de visita BFS de referencia.
    public int[] bfsOrden() {
        return bfsOrden.clone();
    }

    //Distancias BFS desde el inicio; -1 si es inalcanzable.
    public int[] bfsDistancias() {
        return bfsDistancias.clone();
    }

    //Orden de visita DFS de referencia.
    public int[] dfsOrden() {
        return dfsOrden.clone();
    }

    //ACICLICO, CICLO o NO_APLICA (grafos no dirigidos).
    public EstadoTopologico topoEstado() {
        return topoEstado;
    }

    //Un orden topologico valido de referencia. Es un arreglo vacio
    //cuando hay ciclo o no aplica. Existen otros ordenes validos.
    public int[] topoOrden() {
        return topoOrden.clone();
    }

    //Indica si el MST aplica (solo grafos no dirigidos).
    public boolean mstAplica() {
        return mstAplica;
    }

    //Peso minimo del arbol o bosque de expansion minima.
    public long mstPeso() {

        if (!mstAplica) {
            throw new IllegalStateException(
                "El MST no aplica a este caso."
            );
        }

        return mstPeso;
    }

    //Cantidad de componentes conexos (V - aristas del bosque).
    public int mstComponentes() {

        if (!mstAplica) {
            throw new IllegalStateException(
                "El MST no aplica a este caso."
            );
        }

        return mstComponentes;
    }

    //Comprueba que la referencia sea coherente con el grafo cargado.
    //Detecta referencias desactualizadas o de otro caso.
    public void verificarCoherencia(GrafoCargado grafo) {

        int n = grafo.numVertices();

        if (!caso.equals(grafo.nombre())) {
            incoherente("la referencia es del caso '" + caso
                + "' y el grafo es '" + grafo.nombre() + "'");
        }

        if (inicio < 0 || inicio >= n) {
            incoherente("INICIO=" + inicio + " fuera de rango");
        }

        if (bfsDistancias.length != n) {
            incoherente("BFS_DISTANCIAS tiene "
                + bfsDistancias.length + " valores y el grafo " + n
                + " vertices");
        }

        revisarOrden("BFS_ORDEN", bfsOrden, n);
        revisarOrden("DFS_ORDEN", dfsOrden, n);

        boolean dirigido = grafo.esDirigido();

        if (dirigido == (topoEstado == EstadoTopologico.NO_APLICA)) {
            incoherente("TOPO_ESTADO=" + topoEstado
                + " no corresponde al tipo de grafo");
        }

        if (dirigido == mstAplica) {
            incoherente("MST_PESO no corresponde al tipo de grafo");
        }

        if (mstAplica && (mstComponentes < 1 || mstComponentes > n)) {
            incoherente("MST_COMPONENTES=" + mstComponentes
                + " fuera de rango");
        }
    }

    //Un orden de visita no puede repetir ni salirse de rango.
    private static void revisarOrden(
            String clave, int[] orden, int n) {

        if (orden.length > n) {
            incoherente(clave + " tiene mas vertices que el grafo");
        }

        boolean[] visto = new boolean[n];

        for (int v : orden) {

            if (v < 0 || v >= n || visto[v]) {
                incoherente(clave + " contiene un vertice invalido o"
                    + " repetido (" + v + ")");
            }

            visto[v] = true;
        }
    }

    //Informa una incoherencia entre referencia y grafo.
    private static void incoherente(String motivo) {
        throw new IllegalArgumentException(
            "Referencia incoherente: " + motivo + "."
        );
    }
}
