package ec.edu.epn.eda2.juez.submission;

import ec.edu.epn.eda2.juez.core.modelo.GrafoCargado;

/**
 * Contrato que debe implementar cada solucion de estudiante.
 *
 * <p>El juez carga el grafo desde el TXT, llama a estos metodos,
 * mide el tiempo SOLO de la llamada y luego valida el resultado.
 *
 * <p>Reglas generales:
 * <ul>
 *   <li>Los vertices se numeran de 0 a V-1.</li>
 *   <li>El grafo es de solo lectura: no lo modifique. Si necesita
 *       estructuras propias, construyalas dentro del metodo (ese
 *       tiempo tambien se mide).</li>
 *   <li>El juez no llama a {@link #ordenTopologico} en grafos no
 *       dirigidos ni a {@link #mst} en grafos dirigidos.</li>
 *   <li>La clase debe ser publica y tener un constructor publico
 *       sin argumentos. No debe guardar estado entre llamadas: el
 *       juez repite cada metodo varias veces para medir tiempos.</li>
 *   <li>Lanzar una excepcion o devolver null cuenta como error de
 *       ejecucion. Superar el tiempo limite cuenta como TIMEOUT.</li>
 * </ul>
 */
public interface SolucionEstudiante {

    /**
     * Recorrido en profundidad (DFS) desde {@code inicio}.
     *
     * <p>Se visitan los vecinos en orden ascendente de numero de
     * vertice. Se devuelve el orden de descubrimiento (preorden) y
     * solo se incluyen los vertices alcanzables desde {@code inicio}.
     * Puede ser recursivo o iterativo, pero el orden debe ser el
     * mismo que el del DFS recursivo.
     *
     * @param grafo  grafo cargado (dirigido o no dirigido)
     * @param inicio vertice de partida
     * @return vertices en el orden en que fueron descubiertos
     */
    int[] dfs(GrafoCargado grafo, int inicio);

    /**
     * Recorrido en anchura (BFS) desde {@code inicio}.
     *
     * <p>Se usa una cola FIFO y se visitan los vecinos en orden
     * ascendente. Se devuelve el orden de visita y un arreglo de
     * distancias de longitud V (en numero de aristas, 0 para el
     * inicio, -1 para los vertices inalcanzables).
     *
     * @param grafo  grafo cargado (dirigido o no dirigido)
     * @param inicio vertice de partida
     * @return orden de visita y distancias
     */
    ResultadoBFS bfs(GrafoCargado grafo, int inicio);

    /**
     * Orden topologico de un grafo DIRIGIDO.
     *
     * <p>Si el grafo es aciclico se devuelve cualquier orden
     * topologico valido con los V vertices
     * ({@link ResultadoTopologico#aciclico}). Si tiene al menos un
     * ciclo se debe reportar {@link ResultadoTopologico#conCiclo()}.
     *
     * @param grafo grafo dirigido
     * @return orden valido o aviso de ciclo
     */
    ResultadoTopologico ordenTopologico(GrafoCargado grafo);

    /**
     * Arbol (o bosque) de expansion minima de un grafo NO DIRIGIDO.
     *
     * <p>Se devuelven las aristas elegidas, tal como existen en el
     * grafo. Si el grafo es desconectado se devuelve un bosque
     * minimo (un arbol por componente). Se acepta cualquier
     * conjunto de aristas optimo cuando hay empates.
     *
     * @param grafo grafo no dirigido
     * @return aristas del arbol o bosque de expansion minima
     */
    ResultadoMST mst(GrafoCargado grafo);

    /** Nombre que aparece en el reporte del juez. */
    default String nombre() {
        return getClass().getSimpleName();
    }
}
